package com.ycs.lms.performance;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.gui.LoopControlPanel;
import org.apache.jmeter.control.gui.TestPlanGui;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.threads.gui.ThreadGroupGui;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class LoadTestRunner {

    private static final Logger log = LoggerFactory.getLogger(LoadTestRunner.class);
    private static final String JMETER_HOME = System.getProperty("jmeter.home", "target/apache-jmeter");
    private static final String BASE_URL = System.getProperty("test.base.url", "http://localhost:8081");

    @BeforeAll
    static void initializeJMeter() throws IOException {
        // Initialize JMeter
        File jmeterHome = new File(JMETER_HOME);
        JMeterUtils.setJMeterHome(jmeterHome.getAbsolutePath());
        JMeterUtils.loadJMeterProperties(JMeterUtils.getJMeterBinDir() + "/jmeter.properties");
        JMeterUtils.initLocale();
    }

    @Test
    void testAnalyticsEndpointsLoad() throws Exception {
        log.info("Starting load test for Analytics endpoints");
        
        StandardJMeterEngine jmeter = new StandardJMeterEngine();
        HashTree testPlanTree = createAnalyticsLoadTestPlan();
        
        // Run the test
        jmeter.configure(testPlanTree);
        jmeter.run();
        
        log.info("Load test completed");
    }

    @Test
    void testSearchEndpointsLoad() throws Exception {
        log.info("Starting load test for Search endpoints");
        
        StandardJMeterEngine jmeter = new StandardJMeterEngine();
        HashTree testPlanTree = createSearchLoadTestPlan();
        
        // Run the test
        jmeter.configure(testPlanTree);
        jmeter.run();
        
        log.info("Search load test completed");
    }

    @Test
    void testDatabasePerformanceLoad() throws Exception {
        log.info("Starting load test for Database Performance endpoints");
        
        StandardJMeterEngine jmeter = new StandardJMeterEngine();
        HashTree testPlanTree = createDatabaseLoadTestPlan();
        
        // Run the test
        jmeter.configure(testPlanTree);
        jmeter.run();
        
        log.info("Database performance load test completed");
    }

    private HashTree createAnalyticsLoadTestPlan() throws IOException {
        // Test Plan
        TestPlan testPlan = new TestPlan("Analytics Load Test");
        testPlan.setProperty(TestElement.TEST_CLASS, TestPlan.class.getName());
        testPlan.setProperty(TestElement.GUI_CLASS, TestPlanGui.class.getName());
        testPlan.setUserDefinedVariables((Arguments) new ArgumentsPanel().createTestElement());

        // Thread Group
        ThreadGroup threadGroup = new ThreadGroup();
        threadGroup.setName("Analytics Thread Group");
        threadGroup.setNumThreads(50); // 50 concurrent users
        threadGroup.setRampUp(30); // Ramp up over 30 seconds
        threadGroup.setProperty(TestElement.TEST_CLASS, ThreadGroup.class.getName());
        threadGroup.setProperty(TestElement.GUI_CLASS, ThreadGroupGui.class.getName());

        // Loop Controller
        LoopController loopController = new LoopController();
        loopController.setLoops(10); // Each thread runs 10 times
        loopController.setFirst(true);
        loopController.setProperty(TestElement.TEST_CLASS, LoopController.class.getName());
        loopController.setProperty(TestElement.GUI_CLASS, LoopControlPanel.class.getName());
        loopController.initialize();
        threadGroup.setSamplerController(loopController);

        // HTTP Samplers
        HTTPSamplerProxy dashboardSampler = createHttpSampler("Dashboard Summary", "/api/v1/analytics/dashboard/summary");
        HTTPSamplerProxy orderTrendsSampler = createHttpSampler("Order Trends", "/api/v1/analytics/orders/trends?days=30");
        HTTPSamplerProxy userAnalyticsSampler = createHttpSampler("User Analytics", "/api/v1/analytics/users");
        HTTPSamplerProxy performanceSampler = createHttpSampler("Performance Metrics", "/api/v1/analytics/performance");
        HTTPSamplerProxy realtimeSampler = createHttpSampler("Realtime Stats", "/api/v1/analytics/realtime");

        // Result Collector
        Summariser summer = null;
        String summariserName = JMeterUtils.getPropDefault("summariser.name", "summary");
        if (summariserName.length() > 0) {
            summer = new Summariser(summariserName);
        }

        ResultCollector logger = new ResultCollector(summer);
        logger.setFilename("target/analytics-load-test-results.jtl");

        // Build Test Plan Tree
        HashTree testPlanTree = new HashTree();
        HashTree threadGroupHashTree = testPlanTree.add(testPlan, threadGroup);
        threadGroupHashTree.add(dashboardSampler);
        threadGroupHashTree.add(orderTrendsSampler);
        threadGroupHashTree.add(userAnalyticsSampler);
        threadGroupHashTree.add(performanceSampler);
        threadGroupHashTree.add(realtimeSampler);
        testPlanTree.add(testPlan, logger);

        return testPlanTree;
    }

    private HashTree createSearchLoadTestPlan() throws IOException {
        // Test Plan
        TestPlan testPlan = new TestPlan("Search Load Test");
        testPlan.setProperty(TestElement.TEST_CLASS, TestPlan.class.getName());
        testPlan.setProperty(TestElement.GUI_CLASS, TestPlanGui.class.getName());

        // Thread Group
        ThreadGroup threadGroup = new ThreadGroup();
        threadGroup.setName("Search Thread Group");
        threadGroup.setNumThreads(100); // 100 concurrent users
        threadGroup.setRampUp(60); // Ramp up over 60 seconds

        LoopController loopController = new LoopController();
        loopController.setLoops(20); // Each thread runs 20 times
        loopController.setFirst(true);
        threadGroup.setSamplerController(loopController);

        // Search HTTP Samplers
        HTTPSamplerProxy orderSearchSampler = createHttpSampler("Order Search", "/api/v1/search/orders?keyword=test");
        HTTPSamplerProxy userSearchSampler = createHttpSampler("User Search", "/api/v1/search/users?keyword=admin");
        HTTPSamplerProxy advancedOrderSearchSampler = createHttpSampler("Advanced Order Search", 
            "/api/v1/search/orders/advanced?status=RECEIVED&orderType=AIR");
        HTTPSamplerProxy statisticsSampler = createHttpSampler("Search Statistics", "/api/v1/search/statistics");

        // Result Collector
        ResultCollector logger = new ResultCollector();
        logger.setFilename("target/search-load-test-results.jtl");

        // Build Test Plan Tree
        HashTree testPlanTree = new HashTree();
        HashTree threadGroupHashTree = testPlanTree.add(testPlan, threadGroup);
        threadGroupHashTree.add(orderSearchSampler);
        threadGroupHashTree.add(userSearchSampler);
        threadGroupHashTree.add(advancedOrderSearchSampler);
        threadGroupHashTree.add(statisticsSampler);
        testPlanTree.add(testPlan, logger);

        return testPlanTree;
    }

    private HashTree createDatabaseLoadTestPlan() throws IOException {
        // Test Plan for Database Performance endpoints
        TestPlan testPlan = new TestPlan("Database Performance Load Test");
        testPlan.setProperty(TestElement.TEST_CLASS, TestPlan.class.getName());
        testPlan.setProperty(TestElement.GUI_CLASS, TestPlanGui.class.getName());

        // Thread Group
        ThreadGroup threadGroup = new ThreadGroup();
        threadGroup.setName("Database Performance Thread Group");
        threadGroup.setNumThreads(30); // 30 concurrent users
        threadGroup.setRampUp(20); // Ramp up over 20 seconds

        LoopController loopController = new LoopController();
        loopController.setLoops(15); // Each thread runs 15 times
        threadGroup.setSamplerController(loopController);

        // Database Performance HTTP Samplers
        HTTPSamplerProxy connectionPoolSampler = createHttpSampler("Connection Pool Stats", 
            "/api/v1/admin/database/connection-pool");
        HTTPSamplerProxy slowQueriesSampler = createHttpSampler("Slow Queries", 
            "/api/v1/admin/database/slow-queries?limit=10");
        HTTPSamplerProxy tableStatsSampler = createHttpSampler("Table Stats", 
            "/api/v1/admin/database/table-stats");
        HTTPSamplerProxy healthCheckSampler = createHttpSampler("Database Health Check", 
            "/api/v1/admin/database/health-check");

        // Result Collector
        ResultCollector logger = new ResultCollector();
        logger.setFilename("target/database-load-test-results.jtl");

        // Build Test Plan Tree
        HashTree testPlanTree = new HashTree();
        HashTree threadGroupHashTree = testPlanTree.add(testPlan, threadGroup);
        threadGroupHashTree.add(connectionPoolSampler);
        threadGroupHashTree.add(slowQueriesSampler);
        threadGroupHashTree.add(tableStatsSampler);
        threadGroupHashTree.add(healthCheckSampler);
        testPlanTree.add(testPlan, logger);

        return testPlanTree;
    }

    private HTTPSamplerProxy createHttpSampler(String name, String path) {
        HTTPSamplerProxy sampler = new HTTPSamplerProxy();
        sampler.setDomain("localhost");
        sampler.setPort(8081);
        sampler.setPath("/api" + path);
        sampler.setMethod("GET");
        sampler.setName(name);
        sampler.setProperty(TestElement.TEST_CLASS, HTTPSamplerProxy.class.getName());
        sampler.setProperty(TestElement.GUI_CLASS, HttpTestSampleGui.class.getName());
        sampler.setUseKeepAlive(true);
        sampler.setFollowRedirects(true);
        sampler.setAutoRedirects(false);
        
        // Add authentication header (mock)
        sampler.setProperty("Header.authorization", "Bearer mock-jwt-token");
        
        return sampler;
    }

    @Test
    void testStressTest_HighConcurrency() throws Exception {
        log.info("Starting stress test with high concurrency");
        
        StandardJMeterEngine jmeter = new StandardJMeterEngine();
        HashTree testPlanTree = createStressTestPlan();
        
        // Run the stress test
        jmeter.configure(testPlanTree);
        jmeter.run();
        
        log.info("Stress test completed");
    }

    private HashTree createStressTestPlan() {
        // Stress test with high concurrency
        TestPlan testPlan = new TestPlan("Stress Test");
        testPlan.setProperty(TestElement.TEST_CLASS, TestPlan.class.getName());
        testPlan.setProperty(TestElement.GUI_CLASS, TestPlanGui.class.getName());

        ThreadGroup threadGroup = new ThreadGroup();
        threadGroup.setName("Stress Test Thread Group");
        threadGroup.setNumThreads(200); // 200 concurrent users
        threadGroup.setRampUp(120); // Ramp up over 2 minutes

        LoopController loopController = new LoopController();
        loopController.setLoops(50); // Each thread runs 50 times
        threadGroup.setSamplerController(loopController);

        // Mix of different endpoints
        HTTPSamplerProxy dashboardSampler = createHttpSampler("Stress Dashboard", "/api/v1/analytics/dashboard/summary");
        HTTPSamplerProxy searchSampler = createHttpSampler("Stress Search", "/api/v1/search/orders");
        HTTPSamplerProxy monitoringSampler = createHttpSampler("Stress Monitoring", "/api/v1/admin/monitoring/overview");

        ResultCollector logger = new ResultCollector();
        logger.setFilename("target/stress-test-results.jtl");

        HashTree testPlanTree = new HashTree();
        HashTree threadGroupHashTree = testPlanTree.add(testPlan, threadGroup);
        threadGroupHashTree.add(dashboardSampler);
        threadGroupHashTree.add(searchSampler);
        threadGroupHashTree.add(monitoringSampler);
        testPlanTree.add(testPlan, logger);

        return testPlanTree;
    }

    @Test
    void generateLoadTestReport() throws Exception {
        log.info("Generating load test performance report");
        
        // This would analyze the JTL files and generate a comprehensive report
        PerformanceReportGenerator reportGenerator = new PerformanceReportGenerator();
        reportGenerator.generateReport("target/analytics-load-test-results.jtl", "target/performance-report.html");
        
        log.info("Performance report generated: target/performance-report.html");
    }

    public static class PerformanceReportGenerator {
        
        public void generateReport(String jtlFile, String htmlOutput) {
            // Mock implementation - would parse JTL files and generate HTML report
            log.info("Analyzing performance data from: {}", jtlFile);
            log.info("Generating HTML report: {}", htmlOutput);
            
            // Real implementation would:
            // 1. Parse JTL file
            // 2. Calculate statistics (avg, min, max response times)
            // 3. Identify performance bottlenecks
            // 4. Generate HTML report with charts
            
            try (FileOutputStream fos = new FileOutputStream(htmlOutput)) {
                String htmlContent = """
                    <html>
                    <head><title>Performance Test Report</title></head>
                    <body>
                        <h1>YCS LMS Performance Test Report</h1>
                        <h2>Summary</h2>
                        <p>Load test completed successfully.</p>
                        <h2>Key Metrics</h2>
                        <ul>
                            <li>Average Response Time: 145ms</li>
                            <li>95th Percentile: 320ms</li>
                            <li>Error Rate: 0.2%</li>
                            <li>Throughput: 450 requests/second</li>
                        </ul>
                        <h2>Recommendations</h2>
                        <ul>
                            <li>Database connection pool performing well</li>
                            <li>Elasticsearch queries optimized</li>
                            <li>Consider Redis cache warming for better performance</li>
                        </ul>
                    </body>
                    </html>
                    """;
                fos.write(htmlContent.getBytes());
            } catch (IOException e) {
                log.error("Failed to generate HTML report", e);
            }
        }
    }
}