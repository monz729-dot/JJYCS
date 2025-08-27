package com.ycs.lms.reporting;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportingService {

    private final Map<String, ReportTemplate> reportTemplates = new ConcurrentHashMap<>();
    private final Map<String, CustomReport> customReports = new ConcurrentHashMap<>();
    private final Map<String, DashboardConfiguration> dashboards = new ConcurrentHashMap<>();
    private final List<ReportExecution> executionHistory = Collections.synchronizedList(new ArrayList<>());

    public void initializeDefaultReports() {
        log.info("Initializing default reports and dashboards");
        
        // Initialize report templates
        createDefaultReportTemplates();
        
        // Initialize dashboards
        createDefaultDashboards();
    }

    public String createCustomReport(String tenantId, CreateReportRequest request, String createdBy) {
        log.info("Creating custom report: {} for tenant: {} by user: {}", 
            request.getName(), tenantId, createdBy);
        
        String reportId = UUID.randomUUID().toString();
        
        CustomReport report = CustomReport.builder()
            .reportId(reportId)
            .tenantId(tenantId)
            .name(request.getName())
            .description(request.getDescription())
            .reportType(request.getReportType())
            .dataSource(request.getDataSource())
            .query(request.getQuery())
            .parameters(request.getParameters())
            .visualization(request.getVisualization())
            .filters(request.getFilters())
            .groupBy(request.getGroupBy())
            .sortBy(request.getSortBy())
            .status(ReportStatus.ACTIVE)
            .isScheduled(request.isScheduled())
            .scheduleExpression(request.getScheduleExpression())
            .outputFormat(request.getOutputFormat())
            .createdBy(createdBy)
            .createdAt(LocalDateTime.now())
            .build();
        
        customReports.put(reportId, report);
        
        log.info("Custom report created: {} ({})", reportId, request.getName());
        return reportId;
    }

    @Async
    public CompletableFuture<ReportResult> executeReport(String reportId, String tenantId, Map<String, Object> parameters, String executedBy) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Executing report: {} for tenant: {} by user: {}", reportId, tenantId, executedBy);
                
                CustomReport report = customReports.get(reportId);
                if (report == null) {
                    throw new IllegalArgumentException("Report not found: " + reportId);
                }
                
                if (!tenantId.equals(report.getTenantId())) {
                    throw new IllegalAccessException("Access denied to report: " + reportId);
                }
                
                ReportExecution execution = ReportExecution.builder()
                    .executionId(UUID.randomUUID().toString())
                    .reportId(reportId)
                    .tenantId(tenantId)
                    .executedBy(executedBy)
                    .startTime(LocalDateTime.now())
                    .parameters(parameters)
                    .status(ExecutionStatus.RUNNING)
                    .build();
                
                executionHistory.add(execution);
                
                // Execute report logic
                ReportResult result = executeReportLogic(report, parameters);
                
                execution.setEndTime(LocalDateTime.now());
                execution.setStatus(ExecutionStatus.COMPLETED);
                execution.setRecordCount(result.getData().size());
                execution.setFilePath(result.getFilePath());
                
                log.info("Report executed successfully: {} with {} records", reportId, result.getData().size());
                return result;
                
            } catch (Exception e) {
                log.error("Report execution failed: {}", reportId, e);
                throw new RuntimeException("Report execution failed: " + e.getMessage(), e);
            }
        });
    }

    public DashboardData getDashboardData(String dashboardId, String tenantId, Map<String, Object> filters) {
        log.debug("Getting dashboard data: {} for tenant: {}", dashboardId, tenantId);
        
        DashboardConfiguration dashboard = dashboards.get(dashboardId);
        if (dashboard == null) {
            throw new IllegalArgumentException("Dashboard not found: " + dashboardId);
        }
        
        List<DashboardWidget> widgets = new ArrayList<>();
        
        for (WidgetConfiguration widgetConfig : dashboard.getWidgets()) {
            DashboardWidget widget = generateWidgetData(widgetConfig, tenantId, filters);
            widgets.add(widget);
        }
        
        return DashboardData.builder()
            .dashboardId(dashboardId)
            .tenantId(tenantId)
            .widgets(widgets)
            .lastUpdated(LocalDateTime.now())
            .refreshInterval(dashboard.getRefreshInterval())
            .build();
    }

    public List<ReportSummary> getReportsList(String tenantId, ReportType type) {
        return customReports.values().stream()
            .filter(report -> tenantId.equals(report.getTenantId()))
            .filter(report -> type == null || type.equals(report.getReportType()))
            .filter(report -> report.getStatus() == ReportStatus.ACTIVE)
            .map(this::convertToSummary)
            .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
            .collect(Collectors.toList());
    }

    public List<DashboardSummary> getDashboardsList(String tenantId) {
        return dashboards.values().stream()
            .filter(dashboard -> tenantId.equals(dashboard.getTenantId()) || "default".equals(dashboard.getTenantId()))
            .filter(DashboardConfiguration::isActive)
            .map(this::convertToDashboardSummary)
            .sorted((a, b) -> a.getDisplayOrder().compareTo(b.getDisplayOrder()))
            .collect(Collectors.toList());
    }

    @Scheduled(fixedRate = 3600000) // Every hour
    public void executeScheduledReports() {
        log.debug("Executing scheduled reports");
        
        List<CustomReport> scheduledReports = customReports.values().stream()
            .filter(CustomReport::isScheduled)
            .filter(report -> report.getStatus() == ReportStatus.ACTIVE)
            .filter(this::isReportDue)
            .collect(Collectors.toList());
        
        for (CustomReport report : scheduledReports) {
            try {
                executeReport(report.getReportId(), report.getTenantId(), Map.of(), "system");
            } catch (Exception e) {
                log.error("Failed to execute scheduled report: {}", report.getReportId(), e);
            }
        }
    }

    private ReportResult executeReportLogic(CustomReport report, Map<String, Object> parameters) {
        // Mock report execution - in real implementation would query actual data
        List<Map<String, Object>> data = generateMockReportData(report, parameters);
        
        String fileName = String.format("%s_%s.%s", 
            report.getName().replaceAll("\\s+", "_"), 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")),
            report.getOutputFormat().toLowerCase());
        
        String filePath = saveReportToFile(data, report.getOutputFormat(), fileName);
        
        return ReportResult.builder()
            .reportId(report.getReportId())
            .reportName(report.getName())
            .executionTime(LocalDateTime.now())
            .data(data)
            .recordCount(data.size())
            .outputFormat(report.getOutputFormat())
            .filePath(filePath)
            .build();
    }

    private List<Map<String, Object>> generateMockReportData(CustomReport report, Map<String, Object> parameters) {
        List<Map<String, Object>> data = new ArrayList<>();
        
        switch (report.getReportType()) {
            case ORDER_SUMMARY:
                data = generateOrderSummaryData(parameters);
                break;
            case FINANCIAL_REPORT:
                data = generateFinancialReportData(parameters);
                break;
            case INVENTORY_REPORT:
                data = generateInventoryReportData(parameters);
                break;
            case USER_ACTIVITY:
                data = generateUserActivityData(parameters);
                break;
            case PERFORMANCE_METRICS:
                data = generatePerformanceMetricsData(parameters);
                break;
            default:
                data = generateDefaultReportData(parameters);
        }
        
        return data;
    }

    private List<Map<String, Object>> generateOrderSummaryData(Map<String, Object> parameters) {
        List<Map<String, Object>> data = new ArrayList<>();
        
        for (int i = 1; i <= 100; i++) {
            Map<String, Object> record = new HashMap<>();
            record.put("orderId", "ORD" + String.format("%06d", i));
            record.put("orderDate", LocalDate.now().minusDays(i % 30));
            record.put("customerName", "Customer " + i);
            record.put("orderValue", 100.0 + (Math.random() * 1000));
            record.put("status", i % 5 == 0 ? "COMPLETED" : "PROCESSING");
            record.put("shippingMethod", i % 3 == 0 ? "AIR" : "SEA");
            data.add(record);
        }
        
        return data;
    }

    private List<Map<String, Object>> generateFinancialReportData(Map<String, Object> parameters) {
        List<Map<String, Object>> data = new ArrayList<>();
        
        for (int i = 1; i <= 12; i++) {
            Map<String, Object> record = new HashMap<>();
            record.put("month", LocalDate.now().minusMonths(i).getMonth().toString());
            record.put("revenue", 10000.0 + (Math.random() * 50000));
            record.put("expenses", 5000.0 + (Math.random() * 20000));
            record.put("profit", (Double) record.get("revenue") - (Double) record.get("expenses"));
            record.put("orderCount", 50 + (int) (Math.random() * 200));
            data.add(record);
        }
        
        return data;
    }

    private List<Map<String, Object>> generateInventoryReportData(Map<String, Object> parameters) {
        List<Map<String, Object>> data = new ArrayList<>();
        
        String[] categories = {"Electronics", "Clothing", "Books", "Home", "Sports"};
        
        for (int i = 1; i <= 50; i++) {
            Map<String, Object> record = new HashMap<>();
            record.put("itemId", "ITEM" + String.format("%04d", i));
            record.put("itemName", "Item " + i);
            record.put("category", categories[i % categories.length]);
            record.put("currentStock", (int) (Math.random() * 100));
            record.put("reservedStock", (int) (Math.random() * 20));
            record.put("availableStock", (Integer) record.get("currentStock") - (Integer) record.get("reservedStock"));
            record.put("lastUpdated", LocalDateTime.now().minusHours(i % 24));
            data.add(record);
        }
        
        return data;
    }

    private List<Map<String, Object>> generateUserActivityData(Map<String, Object> parameters) {
        List<Map<String, Object>> data = new ArrayList<>();
        
        for (int i = 1; i <= 30; i++) {
            Map<String, Object> record = new HashMap<>();
            record.put("date", LocalDate.now().minusDays(i));
            record.put("totalUsers", 100 + (int) (Math.random() * 50));
            record.put("activeUsers", 50 + (int) (Math.random() * 30));
            record.put("newRegistrations", (int) (Math.random() * 10));
            record.put("loginCount", 200 + (int) (Math.random() * 100));
            data.add(record);
        }
        
        return data;
    }

    private List<Map<String, Object>> generatePerformanceMetricsData(Map<String, Object> parameters) {
        List<Map<String, Object>> data = new ArrayList<>();
        
        for (int i = 0; i < 24; i++) {
            Map<String, Object> record = new HashMap<>();
            record.put("hour", i);
            record.put("responseTime", 100 + (Math.random() * 500));
            record.put("throughput", 10 + (Math.random() * 40));
            record.put("errorRate", Math.random() * 5);
            record.put("cpuUsage", 20 + (Math.random() * 60));
            record.put("memoryUsage", 30 + (Math.random() * 50));
            data.add(record);
        }
        
        return data;
    }

    private List<Map<String, Object>> generateDefaultReportData(Map<String, Object> parameters) {
        List<Map<String, Object>> data = new ArrayList<>();
        
        for (int i = 1; i <= 10; i++) {
            Map<String, Object> record = new HashMap<>();
            record.put("id", i);
            record.put("name", "Record " + i);
            record.put("value", Math.random() * 100);
            record.put("timestamp", LocalDateTime.now().minusHours(i));
            data.add(record);
        }
        
        return data;
    }

    private DashboardWidget generateWidgetData(WidgetConfiguration config, String tenantId, Map<String, Object> filters) {
        Map<String, Object> data = new HashMap<>();
        
        switch (config.getWidgetType()) {
            case KPI:
                data = generateKpiData(config);
                break;
            case CHART:
                data = generateChartData(config);
                break;
            case TABLE:
                data = generateTableData(config);
                break;
            case GAUGE:
                data = generateGaugeData(config);
                break;
        }
        
        return DashboardWidget.builder()
            .widgetId(config.getWidgetId())
            .title(config.getTitle())
            .type(config.getWidgetType())
            .data(data)
            .position(config.getPosition())
            .size(config.getSize())
            .lastUpdated(LocalDateTime.now())
            .build();
    }

    private Map<String, Object> generateKpiData(WidgetConfiguration config) {
        Map<String, Object> data = new HashMap<>();
        data.put("value", 1234);
        data.put("change", "+5.2%");
        data.put("trend", "up");
        return data;
    }

    private Map<String, Object> generateChartData(WidgetConfiguration config) {
        Map<String, Object> data = new HashMap<>();
        
        List<String> labels = List.of("Jan", "Feb", "Mar", "Apr", "May", "Jun");
        List<Integer> values = List.of(100, 120, 110, 150, 130, 160);
        
        data.put("labels", labels);
        data.put("datasets", List.of(Map.of("data", values, "label", config.getTitle())));
        
        return data;
    }

    private Map<String, Object> generateTableData(WidgetConfiguration config) {
        Map<String, Object> data = new HashMap<>();
        
        List<String> columns = List.of("ID", "Name", "Value", "Status");
        List<List<Object>> rows = List.of(
            List.of("1", "Item A", 100, "Active"),
            List.of("2", "Item B", 200, "Active"),
            List.of("3", "Item C", 150, "Inactive")
        );
        
        data.put("columns", columns);
        data.put("rows", rows);
        
        return data;
    }

    private Map<String, Object> generateGaugeData(WidgetConfiguration config) {
        Map<String, Object> data = new HashMap<>();
        data.put("value", 75);
        data.put("min", 0);
        data.put("max", 100);
        data.put("unit", "%");
        return data;
    }

    private String saveReportToFile(List<Map<String, Object>> data, String format, String fileName) {
        // Mock file save - in real implementation would save to storage
        String filePath = "/reports/" + fileName;
        log.debug("Saving report to file: {}", filePath);
        return filePath;
    }

    private boolean isReportDue(CustomReport report) {
        // Simple schedule check - in real implementation would use cron expression parser
        return Math.random() > 0.9; // 10% chance for demo
    }

    private ReportSummary convertToSummary(CustomReport report) {
        return ReportSummary.builder()
            .reportId(report.getReportId())
            .name(report.getName())
            .description(report.getDescription())
            .reportType(report.getReportType())
            .status(report.getStatus())
            .isScheduled(report.isScheduled())
            .createdAt(report.getCreatedAt())
            .createdBy(report.getCreatedBy())
            .build();
    }

    private DashboardSummary convertToDashboardSummary(DashboardConfiguration dashboard) {
        return DashboardSummary.builder()
            .dashboardId(dashboard.getDashboardId())
            .name(dashboard.getName())
            .description(dashboard.getDescription())
            .widgetCount(dashboard.getWidgets().size())
            .isPublic(dashboard.isPublic())
            .displayOrder(dashboard.getDisplayOrder())
            .build();
    }

    private void createDefaultReportTemplates() {
        // Order Summary Template
        reportTemplates.put("ORDER_SUMMARY_TEMPLATE", ReportTemplate.builder()
            .templateId("ORDER_SUMMARY_TEMPLATE")
            .name("주문 요약 리포트")
            .description("주문 현황 요약 리포트")
            .reportType(ReportType.ORDER_SUMMARY)
            .dataSource("orders")
            .defaultQuery("SELECT * FROM orders WHERE created_at >= ?")
            .parameters(List.of(
                ReportParameter.builder().name("startDate").type("DATE").required(true).build(),
                ReportParameter.builder().name("endDate").type("DATE").required(true).build()
            ))
            .build());

        // Financial Report Template
        reportTemplates.put("FINANCIAL_TEMPLATE", ReportTemplate.builder()
            .templateId("FINANCIAL_TEMPLATE")
            .name("재무 리포트")
            .description("월별 재무 현황 리포트")
            .reportType(ReportType.FINANCIAL_REPORT)
            .dataSource("financial_data")
            .defaultQuery("SELECT * FROM financial_summary WHERE report_month >= ?")
            .parameters(List.of(
                ReportParameter.builder().name("fromMonth").type("STRING").required(true).build(),
                ReportParameter.builder().name("toMonth").type("STRING").required(true).build()
            ))
            .build());
    }

    private void createDefaultDashboards() {
        // Executive Dashboard
        dashboards.put("EXECUTIVE_DASHBOARD", DashboardConfiguration.builder()
            .dashboardId("EXECUTIVE_DASHBOARD")
            .tenantId("default")
            .name("경영진 대시보드")
            .description("주요 경영 지표 대시보드")
            .widgets(List.of(
                WidgetConfiguration.builder()
                    .widgetId("monthly_revenue")
                    .title("월간 매출")
                    .widgetType(WidgetType.KPI)
                    .dataSource("financial_data")
                    .position(Map.of("x", 0, "y", 0))
                    .size(Map.of("width", 6, "height", 4))
                    .build(),
                WidgetConfiguration.builder()
                    .widgetId("order_trend")
                    .title("주문 추이")
                    .widgetType(WidgetType.CHART)
                    .dataSource("orders")
                    .position(Map.of("x", 6, "y", 0))
                    .size(Map.of("width", 6, "height", 4))
                    .build(),
                WidgetConfiguration.builder()
                    .widgetId("top_customers")
                    .title("주요 고객")
                    .widgetType(WidgetType.TABLE)
                    .dataSource("customers")
                    .position(Map.of("x", 0, "y", 4))
                    .size(Map.of("width", 12, "height", 4))
                    .build()
            ))
            .refreshInterval(300) // 5 minutes
            .isPublic(false)
            .active(true)
            .displayOrder(1)
            .build());

        // Operations Dashboard
        dashboards.put("OPERATIONS_DASHBOARD", DashboardConfiguration.builder()
            .dashboardId("OPERATIONS_DASHBOARD")
            .tenantId("default")
            .name("운영 대시보드")
            .description("일일 운영 현황 대시보드")
            .widgets(List.of(
                WidgetConfiguration.builder()
                    .widgetId("daily_orders")
                    .title("일일 주문량")
                    .widgetType(WidgetType.KPI)
                    .dataSource("orders")
                    .position(Map.of("x", 0, "y", 0))
                    .size(Map.of("width", 4, "height", 3))
                    .build(),
                WidgetConfiguration.builder()
                    .widgetId("inventory_status")
                    .title("재고 현황")
                    .widgetType(WidgetType.GAUGE)
                    .dataSource("inventory")
                    .position(Map.of("x", 4, "y", 0))
                    .size(Map.of("width", 4, "height", 3))
                    .build(),
                WidgetConfiguration.builder()
                    .widgetId("system_performance")
                    .title("시스템 성능")
                    .widgetType(WidgetType.CHART)
                    .dataSource("metrics")
                    .position(Map.of("x", 8, "y", 0))
                    .size(Map.of("width", 4, "height", 3))
                    .build()
            ))
            .refreshInterval(60) // 1 minute
            .isPublic(true)
            .active(true)
            .displayOrder(2)
            .build());
    }

    // DTO Classes
    @lombok.Data
    @lombok.Builder
    public static class CustomReport {
        private String reportId;
        private String tenantId;
        private String name;
        private String description;
        private ReportType reportType;
        private String dataSource;
        private String query;
        private List<ReportParameter> parameters;
        private VisualizationType visualization;
        private List<ReportFilter> filters;
        private List<String> groupBy;
        private List<ReportSort> sortBy;
        private ReportStatus status;
        private boolean isScheduled;
        private String scheduleExpression;
        private String outputFormat;
        private String createdBy;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @lombok.Data
    @lombok.Builder
    public static class ReportTemplate {
        private String templateId;
        private String name;
        private String description;
        private ReportType reportType;
        private String dataSource;
        private String defaultQuery;
        private List<ReportParameter> parameters;
    }

    @lombok.Data
    @lombok.Builder
    public static class ReportParameter {
        private String name;
        private String type;
        private String label;
        private String description;
        private Object defaultValue;
        private boolean required;
        private List<String> allowedValues;
    }

    @lombok.Data
    @lombok.Builder
    public static class ReportFilter {
        private String field;
        private String operator;
        private Object value;
    }

    @lombok.Data
    @lombok.Builder
    public static class ReportSort {
        private String field;
        private SortDirection direction;
    }

    @lombok.Data
    @lombok.Builder
    public static class ReportResult {
        private String reportId;
        private String reportName;
        private LocalDateTime executionTime;
        private List<Map<String, Object>> data;
        private int recordCount;
        private String outputFormat;
        private String filePath;
    }

    @lombok.Data
    @lombok.Builder
    public static class ReportExecution {
        private String executionId;
        private String reportId;
        private String tenantId;
        private String executedBy;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Map<String, Object> parameters;
        private ExecutionStatus status;
        private int recordCount;
        private String filePath;
        private String errorMessage;
    }

    @lombok.Data
    @lombok.Builder
    public static class DashboardConfiguration {
        private String dashboardId;
        private String tenantId;
        private String name;
        private String description;
        private List<WidgetConfiguration> widgets;
        private int refreshInterval;
        private boolean isPublic;
        private boolean active;
        private Integer displayOrder;
        private String createdBy;
        private LocalDateTime createdAt;
    }

    @lombok.Data
    @lombok.Builder
    public static class WidgetConfiguration {
        private String widgetId;
        private String title;
        private WidgetType widgetType;
        private String dataSource;
        private String query;
        private Map<String, Object> position;
        private Map<String, Object> size;
        private Map<String, Object> styling;
    }

    @lombok.Data
    @lombok.Builder
    public static class DashboardData {
        private String dashboardId;
        private String tenantId;
        private List<DashboardWidget> widgets;
        private LocalDateTime lastUpdated;
        private int refreshInterval;
    }

    @lombok.Data
    @lombok.Builder
    public static class DashboardWidget {
        private String widgetId;
        private String title;
        private WidgetType type;
        private Map<String, Object> data;
        private Map<String, Object> position;
        private Map<String, Object> size;
        private LocalDateTime lastUpdated;
    }

    @lombok.Data
    @lombok.Builder
    public static class ReportSummary {
        private String reportId;
        private String name;
        private String description;
        private ReportType reportType;
        private ReportStatus status;
        private boolean isScheduled;
        private LocalDateTime createdAt;
        private String createdBy;
    }

    @lombok.Data
    @lombok.Builder
    public static class DashboardSummary {
        private String dashboardId;
        private String name;
        private String description;
        private int widgetCount;
        private boolean isPublic;
        private Integer displayOrder;
    }

    public static class CreateReportRequest {
        private String name;
        private String description;
        private ReportType reportType;
        private String dataSource;
        private String query;
        private List<ReportParameter> parameters;
        private VisualizationType visualization;
        private List<ReportFilter> filters;
        private List<String> groupBy;
        private List<ReportSort> sortBy;
        private boolean isScheduled;
        private String scheduleExpression;
        private String outputFormat;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public ReportType getReportType() { return reportType; }
        public void setReportType(ReportType reportType) { this.reportType = reportType; }

        public String getDataSource() { return dataSource; }
        public void setDataSource(String dataSource) { this.dataSource = dataSource; }

        public String getQuery() { return query; }
        public void setQuery(String query) { this.query = query; }

        public List<ReportParameter> getParameters() { return parameters; }
        public void setParameters(List<ReportParameter> parameters) { this.parameters = parameters; }

        public VisualizationType getVisualization() { return visualization; }
        public void setVisualization(VisualizationType visualization) { this.visualization = visualization; }

        public List<ReportFilter> getFilters() { return filters; }
        public void setFilters(List<ReportFilter> filters) { this.filters = filters; }

        public List<String> getGroupBy() { return groupBy; }
        public void setGroupBy(List<String> groupBy) { this.groupBy = groupBy; }

        public List<ReportSort> getSortBy() { return sortBy; }
        public void setSortBy(List<ReportSort> sortBy) { this.sortBy = sortBy; }

        public boolean isScheduled() { return isScheduled; }
        public void setScheduled(boolean scheduled) { isScheduled = scheduled; }

        public String getScheduleExpression() { return scheduleExpression; }
        public void setScheduleExpression(String scheduleExpression) { this.scheduleExpression = scheduleExpression; }

        public String getOutputFormat() { return outputFormat; }
        public void setOutputFormat(String outputFormat) { this.outputFormat = outputFormat; }
    }

    public enum ReportType {
        ORDER_SUMMARY, FINANCIAL_REPORT, INVENTORY_REPORT, USER_ACTIVITY, PERFORMANCE_METRICS, CUSTOM
    }

    public enum ReportStatus {
        ACTIVE, INACTIVE, DRAFT
    }

    public enum VisualizationType {
        TABLE, CHART, GRAPH, PIVOT
    }

    public enum SortDirection {
        ASC, DESC
    }

    public enum ExecutionStatus {
        RUNNING, COMPLETED, FAILED, CANCELLED
    }

    public enum WidgetType {
        KPI, CHART, TABLE, GAUGE, MAP, TEXT
    }
}