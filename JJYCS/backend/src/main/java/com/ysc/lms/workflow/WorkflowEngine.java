package com.ysc.lms.workflow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowEngine {

    private final Map<String, WorkflowDefinition> workflowDefinitions = new ConcurrentHashMap<>();
    private final Map<String, WorkflowInstance> workflowInstances = new ConcurrentHashMap<>();
    private final Map<String, WorkflowTask> taskInstances = new ConcurrentHashMap<>();

    public void initializeDefaultWorkflows() {
        log.info("Initializing default workflows");
        
        // Order Processing Workflow
        registerWorkflow(WorkflowDefinition.builder()
            .workflowId("ORDER_PROCESSING")
            .name("주문 처리 워크플로우")
            .version("1.0")
            .tenantId("default")
            .startNode("ORDER_CREATED")
            .nodes(createOrderProcessingNodes())
            .build());
            
        // User Registration Approval Workflow
        registerWorkflow(WorkflowDefinition.builder()
            .workflowId("USER_APPROVAL")
            .name("사용자 승인 워크플로우")
            .version("1.0")
            .tenantId("default")
            .startNode("REGISTRATION_SUBMITTED")
            .nodes(createUserApprovalNodes())
            .build());
            
        // Shipment Processing Workflow
        registerWorkflow(WorkflowDefinition.builder()
            .workflowId("SHIPMENT_PROCESSING")
            .name("배송 처리 워크플로우")
            .version("1.0")
            .tenantId("default")
            .startNode("SHIPMENT_CREATED")
            .nodes(createShipmentProcessingNodes())
            .build());
    }

    public String startWorkflow(String workflowId, String tenantId, Map<String, Object> context, String startedBy) {
        log.info("Starting workflow: {} for tenant: {} by user: {}", workflowId, tenantId, startedBy);
        
        WorkflowDefinition definition = getWorkflowDefinition(workflowId, tenantId);
        if (definition == null) {
            throw new IllegalArgumentException("Workflow definition not found: " + workflowId);
        }
        
        String instanceId = UUID.randomUUID().toString();
        WorkflowInstance instance = WorkflowInstance.builder()
            .instanceId(instanceId)
            .workflowId(workflowId)
            .tenantId(tenantId)
            .status(WorkflowStatus.RUNNING)
            .context(new HashMap<>(context))
            .currentNode(definition.getStartNode())
            .startedBy(startedBy)
            .startedAt(LocalDateTime.now())
            .lastUpdated(LocalDateTime.now())
            .build();
            
        workflowInstances.put(instanceId, instance);
        
        // Execute first node
        executeNode(instanceId, definition.getStartNode());
        
        return instanceId;
    }

    @Async
    public CompletableFuture<Void> executeNode(String instanceId, String nodeId) {
        return CompletableFuture.runAsync(() -> {
            try {
                WorkflowInstance instance = workflowInstances.get(instanceId);
                if (instance == null) {
                    log.error("Workflow instance not found: {}", instanceId);
                    return;
                }
                
                WorkflowDefinition definition = getWorkflowDefinition(instance.getWorkflowId(), instance.getTenantId());
                WorkflowNode node = definition.getNodes().get(nodeId);
                
                if (node == null) {
                    log.error("Workflow node not found: {} in workflow: {}", nodeId, instance.getWorkflowId());
                    return;
                }
                
                log.debug("Executing node: {} in workflow instance: {}", nodeId, instanceId);
                
                // Update instance current node
                instance.setCurrentNode(nodeId);
                instance.setLastUpdated(LocalDateTime.now());
                
                // Execute node logic
                NodeExecutionResult result = executeNodeLogic(instance, node);
                
                // Update context with result data
                if (result.getData() != null) {
                    instance.getContext().putAll(result.getData());
                }
                
                // Handle result
                switch (result.getStatus()) {
                    case COMPLETED:
                        handleNodeCompletion(instance, node, result);
                        break;
                    case FAILED:
                        handleNodeFailure(instance, node, result);
                        break;
                    case WAITING:
                        handleNodeWaiting(instance, node, result);
                        break;
                }
                
            } catch (Exception e) {
                log.error("Error executing workflow node: {} in instance: {}", nodeId, instanceId, e);
                handleWorkflowError(instanceId, e);
            }
        });
    }

    public void completeTask(String taskId, Map<String, Object> taskResult, String completedBy) {
        log.info("Completing task: {} by user: {}", taskId, completedBy);
        
        WorkflowTask task = taskInstances.get(taskId);
        if (task == null) {
            throw new IllegalArgumentException("Task not found: " + taskId);
        }
        
        if (task.getStatus() != TaskStatus.ASSIGNED) {
            throw new IllegalStateException("Task is not in assignable state: " + task.getStatus());
        }
        
        task.setStatus(TaskStatus.COMPLETED);
        task.setCompletedBy(completedBy);
        task.setCompletedAt(LocalDateTime.now());
        task.setResult(taskResult);
        
        // Continue workflow execution
        WorkflowInstance instance = workflowInstances.get(task.getWorkflowInstanceId());
        if (instance != null) {
            instance.getContext().putAll(taskResult);
            
            // Find next node based on task completion
            WorkflowDefinition definition = getWorkflowDefinition(instance.getWorkflowId(), instance.getTenantId());
            WorkflowNode currentNode = definition.getNodes().get(instance.getCurrentNode());
            
            if (currentNode != null && currentNode.getType() == NodeType.USER_TASK) {
                // Move to next node
                executeNextNodes(instance, currentNode, taskResult);
            }
        }
    }

    public List<WorkflowTask> getUserTasks(String userId, String tenantId, TaskStatus status) {
        return taskInstances.values().stream()
            .filter(task -> tenantId.equals(task.getTenantId()))
            .filter(task -> userId.equals(task.getAssignedTo()) || 
                           (task.getCandidateUsers() != null && task.getCandidateUsers().contains(userId)))
            .filter(task -> status == null || status.equals(task.getStatus()))
            .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
            .collect(Collectors.toList());
    }

    public WorkflowInstance getWorkflowInstance(String instanceId) {
        return workflowInstances.get(instanceId);
    }

    public List<WorkflowInstance> getWorkflowInstances(String tenantId, String workflowId, WorkflowStatus status) {
        return workflowInstances.values().stream()
            .filter(instance -> tenantId.equals(instance.getTenantId()))
            .filter(instance -> workflowId == null || workflowId.equals(instance.getWorkflowId()))
            .filter(instance -> status == null || status.equals(instance.getStatus()))
            .sorted((a, b) -> b.getStartedAt().compareTo(a.getStartedAt()))
            .collect(Collectors.toList());
    }

    private void registerWorkflow(WorkflowDefinition definition) {
        String key = definition.getTenantId() + ":" + definition.getWorkflowId();
        workflowDefinitions.put(key, definition);
        log.debug("Registered workflow: {} for tenant: {}", definition.getWorkflowId(), definition.getTenantId());
    }

    private WorkflowDefinition getWorkflowDefinition(String workflowId, String tenantId) {
        String key = tenantId + ":" + workflowId;
        WorkflowDefinition definition = workflowDefinitions.get(key);
        
        if (definition == null) {
            // Fallback to default tenant
            key = "default:" + workflowId;
            definition = workflowDefinitions.get(key);
        }
        
        return definition;
    }

    private NodeExecutionResult executeNodeLogic(WorkflowInstance instance, WorkflowNode node) {
        switch (node.getType()) {
            case START:
                return NodeExecutionResult.completed(Map.of("started", true));
                
            case END:
                instance.setStatus(WorkflowStatus.COMPLETED);
                instance.setCompletedAt(LocalDateTime.now());
                return NodeExecutionResult.completed(Map.of("completed", true));
                
            case SERVICE_TASK:
                return executeServiceTask(instance, node);
                
            case USER_TASK:
                return executeUserTask(instance, node);
                
            case DECISION:
                return executeDecisionNode(instance, node);
                
            case PARALLEL:
                return executeParallelNode(instance, node);
                
            case TIMER:
                return executeTimerNode(instance, node);
                
            default:
                return NodeExecutionResult.failed("Unknown node type: " + node.getType());
        }
    }

    private NodeExecutionResult executeServiceTask(WorkflowInstance instance, WorkflowNode node) {
        try {
            // Execute service task logic based on node configuration
            String serviceClass = node.getProperties().get("serviceClass");
            String methodName = node.getProperties().get("methodName");
            
            log.debug("Executing service task: {} -> {}", serviceClass, methodName);
            
            // Mock service execution - in real implementation would use reflection or service registry
            Map<String, Object> result = mockServiceExecution(serviceClass, methodName, instance.getContext());
            
            return NodeExecutionResult.completed(result);
            
        } catch (Exception e) {
            log.error("Service task execution failed", e);
            return NodeExecutionResult.failed(e.getMessage());
        }
    }

    private NodeExecutionResult executeUserTask(WorkflowInstance instance, WorkflowNode node) {
        String taskId = UUID.randomUUID().toString();
        
        WorkflowTask task = WorkflowTask.builder()
            .taskId(taskId)
            .workflowInstanceId(instance.getInstanceId())
            .tenantId(instance.getTenantId())
            .nodeId(node.getNodeId())
            .name(node.getName())
            .description(node.getProperties().get("description"))
            .assignedTo(node.getProperties().get("assignedTo"))
            .candidateUsers(parseCandidateUsers(node.getProperties().get("candidateUsers")))
            .dueDate(calculateDueDate(node.getProperties().get("dueDays")))
            .status(TaskStatus.CREATED)
            .createdAt(LocalDateTime.now())
            .build();
            
        taskInstances.put(taskId, task);
        
        // Auto-assign if single candidate
        if (task.getCandidateUsers() != null && task.getCandidateUsers().size() == 1) {
            task.setAssignedTo(task.getCandidateUsers().get(0));
            task.setStatus(TaskStatus.ASSIGNED);
        }
        
        log.info("Created user task: {} for workflow instance: {}", taskId, instance.getInstanceId());
        
        return NodeExecutionResult.waiting(Map.of("taskId", taskId));
    }

    private NodeExecutionResult executeDecisionNode(WorkflowInstance instance, WorkflowNode node) {
        String condition = node.getProperties().get("condition");
        boolean result = evaluateCondition(condition, instance.getContext());
        
        return NodeExecutionResult.completed(Map.of("decision", result));
    }

    private NodeExecutionResult executeParallelNode(WorkflowInstance instance, WorkflowNode node) {
        // Start parallel execution of all outgoing flows
        List<String> parallelBranches = node.getOutgoingFlows();
        
        for (String branchNodeId : parallelBranches) {
            executeNode(instance.getInstanceId(), branchNodeId);
        }
        
        return NodeExecutionResult.completed(Map.of("parallelBranches", parallelBranches.size()));
    }

    private NodeExecutionResult executeTimerNode(WorkflowInstance instance, WorkflowNode node) {
        String duration = node.getProperties().get("duration");
        // In real implementation, would schedule timer execution
        log.debug("Timer node scheduled for duration: {}", duration);
        
        return NodeExecutionResult.waiting(Map.of("timerDuration", duration));
    }

    private void handleNodeCompletion(WorkflowInstance instance, WorkflowNode node, NodeExecutionResult result) {
        executeNextNodes(instance, node, result.getData());
    }

    private void handleNodeFailure(WorkflowInstance instance, WorkflowNode node, NodeExecutionResult result) {
        instance.setStatus(WorkflowStatus.FAILED);
        instance.setErrorMessage(result.getErrorMessage());
        instance.setLastUpdated(LocalDateTime.now());
        
        log.error("Workflow instance failed: {} at node: {}", instance.getInstanceId(), node.getNodeId());
    }

    private void handleNodeWaiting(WorkflowInstance instance, WorkflowNode node, NodeExecutionResult result) {
        instance.setStatus(WorkflowStatus.WAITING);
        instance.setLastUpdated(LocalDateTime.now());
        
        log.debug("Workflow instance waiting: {} at node: {}", instance.getInstanceId(), node.getNodeId());
    }

    private void executeNextNodes(WorkflowInstance instance, WorkflowNode currentNode, Map<String, Object> data) {
        List<String> nextNodes = determineNextNodes(currentNode, data);
        
        for (String nextNodeId : nextNodes) {
            executeNode(instance.getInstanceId(), nextNodeId);
        }
        
        if (nextNodes.isEmpty()) {
            // No next nodes - workflow completed
            instance.setStatus(WorkflowStatus.COMPLETED);
            instance.setCompletedAt(LocalDateTime.now());
        }
    }

    private List<String> determineNextNodes(WorkflowNode node, Map<String, Object> data) {
        if (node.getType() == NodeType.DECISION && node.getOutgoingFlows().size() > 1) {
            // Decision logic to determine which path to take
            boolean decision = (Boolean) data.getOrDefault("decision", false);
            return decision ? List.of(node.getOutgoingFlows().get(0)) : List.of(node.getOutgoingFlows().get(1));
        }
        
        return node.getOutgoingFlows();
    }

    private void handleWorkflowError(String instanceId, Exception error) {
        WorkflowInstance instance = workflowInstances.get(instanceId);
        if (instance != null) {
            instance.setStatus(WorkflowStatus.FAILED);
            instance.setErrorMessage(error.getMessage());
            instance.setLastUpdated(LocalDateTime.now());
        }
    }

    private Map<String, WorkflowNode> createOrderProcessingNodes() {
        Map<String, WorkflowNode> nodes = new HashMap<>();
        
        nodes.put("ORDER_CREATED", WorkflowNode.builder()
            .nodeId("ORDER_CREATED")
            .name("주문 생성됨")
            .type(NodeType.START)
            .outgoingFlows(List.of("VALIDATE_ORDER"))
            .build());
            
        nodes.put("VALIDATE_ORDER", WorkflowNode.builder()
            .nodeId("VALIDATE_ORDER")
            .name("주문 검증")
            .type(NodeType.SERVICE_TASK)
            .properties(Map.of(
                "serviceClass", "OrderValidationService",
                "methodName", "validateOrder"
            ))
            .outgoingFlows(List.of("VALIDATION_DECISION"))
            .build());
            
        nodes.put("VALIDATION_DECISION", WorkflowNode.builder()
            .nodeId("VALIDATION_DECISION")
            .name("검증 결과 판단")
            .type(NodeType.DECISION)
            .properties(Map.of("condition", "validation.success == true"))
            .outgoingFlows(List.of("PROCESS_PAYMENT", "MANUAL_REVIEW"))
            .build());
            
        nodes.put("PROCESS_PAYMENT", WorkflowNode.builder()
            .nodeId("PROCESS_PAYMENT")
            .name("결제 처리")
            .type(NodeType.SERVICE_TASK)
            .properties(Map.of(
                "serviceClass", "PaymentService",
                "methodName", "processPayment"
            ))
            .outgoingFlows(List.of("PREPARE_SHIPMENT"))
            .build());
            
        nodes.put("MANUAL_REVIEW", WorkflowNode.builder()
            .nodeId("MANUAL_REVIEW")
            .name("수동 검토")
            .type(NodeType.USER_TASK)
            .properties(Map.of(
                "description", "주문 내용을 수동으로 검토하세요",
                "candidateUsers", "admin,manager",
                "dueDays", "1"
            ))
            .outgoingFlows(List.of("PREPARE_SHIPMENT"))
            .build());
            
        nodes.put("PREPARE_SHIPMENT", WorkflowNode.builder()
            .nodeId("PREPARE_SHIPMENT")
            .name("배송 준비")
            .type(NodeType.SERVICE_TASK)
            .properties(Map.of(
                "serviceClass", "ShipmentService",
                "methodName", "prepareShipment"
            ))
            .outgoingFlows(List.of("ORDER_COMPLETED"))
            .build());
            
        nodes.put("ORDER_COMPLETED", WorkflowNode.builder()
            .nodeId("ORDER_COMPLETED")
            .name("주문 완료")
            .type(NodeType.END)
            .build());
            
        return nodes;
    }

    private Map<String, WorkflowNode> createUserApprovalNodes() {
        Map<String, WorkflowNode> nodes = new HashMap<>();
        
        nodes.put("REGISTRATION_SUBMITTED", WorkflowNode.builder()
            .nodeId("REGISTRATION_SUBMITTED")
            .name("등록 신청됨")
            .type(NodeType.START)
            .outgoingFlows(List.of("AUTO_CHECK"))
            .build());
            
        nodes.put("AUTO_CHECK", WorkflowNode.builder()
            .nodeId("AUTO_CHECK")
            .name("자동 검증")
            .type(NodeType.SERVICE_TASK)
            .properties(Map.of(
                "serviceClass", "UserValidationService",
                "methodName", "performAutoCheck"
            ))
            .outgoingFlows(List.of("CHECK_DECISION"))
            .build());
            
        nodes.put("CHECK_DECISION", WorkflowNode.builder()
            .nodeId("CHECK_DECISION")
            .name("검증 결과 판단")
            .type(NodeType.DECISION)
            .properties(Map.of("condition", "autoCheck.approved == true"))
            .outgoingFlows(List.of("AUTO_APPROVE", "MANUAL_APPROVAL"))
            .build());
            
        nodes.put("AUTO_APPROVE", WorkflowNode.builder()
            .nodeId("AUTO_APPROVE")
            .name("자동 승인")
            .type(NodeType.SERVICE_TASK)
            .properties(Map.of(
                "serviceClass", "UserService",
                "methodName", "approveUser"
            ))
            .outgoingFlows(List.of("APPROVAL_COMPLETED"))
            .build());
            
        nodes.put("MANUAL_APPROVAL", WorkflowNode.builder()
            .nodeId("MANUAL_APPROVAL")
            .name("수동 승인")
            .type(NodeType.USER_TASK)
            .properties(Map.of(
                "description", "사용자 등록을 검토하고 승인하세요",
                "candidateUsers", "admin,hr_manager",
                "dueDays", "2"
            ))
            .outgoingFlows(List.of("APPROVAL_COMPLETED"))
            .build());
            
        nodes.put("APPROVAL_COMPLETED", WorkflowNode.builder()
            .nodeId("APPROVAL_COMPLETED")
            .name("승인 완료")
            .type(NodeType.END)
            .build());
            
        return nodes;
    }

    private Map<String, WorkflowNode> createShipmentProcessingNodes() {
        Map<String, WorkflowNode> nodes = new HashMap<>();
        
        nodes.put("SHIPMENT_CREATED", WorkflowNode.builder()
            .nodeId("SHIPMENT_CREATED")
            .name("배송 생성됨")
            .type(NodeType.START)
            .outgoingFlows(List.of("PICK_ITEMS"))
            .build());
            
        nodes.put("PICK_ITEMS", WorkflowNode.builder()
            .nodeId("PICK_ITEMS")
            .name("상품 피킹")
            .type(NodeType.USER_TASK)
            .properties(Map.of(
                "description", "주문 상품을 피킹하세요",
                "candidateUsers", "warehouse_staff",
                "dueDays", "1"
            ))
            .outgoingFlows(List.of("PACK_ITEMS"))
            .build());
            
        nodes.put("PACK_ITEMS", WorkflowNode.builder()
            .nodeId("PACK_ITEMS")
            .name("상품 포장")
            .type(NodeType.USER_TASK)
            .properties(Map.of(
                "description", "상품을 포장하세요",
                "candidateUsers", "warehouse_staff",
                "dueDays", "1"
            ))
            .outgoingFlows(List.of("GENERATE_LABEL"))
            .build());
            
        nodes.put("GENERATE_LABEL", WorkflowNode.builder()
            .nodeId("GENERATE_LABEL")
            .name("라벨 생성")
            .type(NodeType.SERVICE_TASK)
            .properties(Map.of(
                "serviceClass", "LabelService",
                "methodName", "generateShippingLabel"
            ))
            .outgoingFlows(List.of("DISPATCH"))
            .build());
            
        nodes.put("DISPATCH", WorkflowNode.builder()
            .nodeId("DISPATCH")
            .name("발송")
            .type(NodeType.SERVICE_TASK)
            .properties(Map.of(
                "serviceClass", "ShipmentService",
                "methodName", "dispatch"
            ))
            .outgoingFlows(List.of("SHIPMENT_COMPLETED"))
            .build());
            
        nodes.put("SHIPMENT_COMPLETED", WorkflowNode.builder()
            .nodeId("SHIPMENT_COMPLETED")
            .name("배송 완료")
            .type(NodeType.END)
            .build());
            
        return nodes;
    }

    private Map<String, Object> mockServiceExecution(String serviceClass, String methodName, Map<String, Object> context) {
        // Mock implementation - in real system would execute actual service
        return Map.of(
            "serviceExecuted", true,
            "serviceClass", serviceClass,
            "methodName", methodName,
            "timestamp", LocalDateTime.now()
        );
    }

    private List<String> parseCandidateUsers(String candidateUsersStr) {
        if (candidateUsersStr == null || candidateUsersStr.trim().isEmpty()) {
            return null;
        }
        return Arrays.asList(candidateUsersStr.split(","));
    }

    private LocalDateTime calculateDueDate(String dueDaysStr) {
        if (dueDaysStr == null) {
            return null;
        }
        try {
            int days = Integer.parseInt(dueDaysStr);
            return LocalDateTime.now().plusDays(days);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean evaluateCondition(String condition, Map<String, Object> context) {
        // Simple condition evaluation - in real implementation would use expression engine
        if (condition.contains("validation.success == true")) {
            return (Boolean) context.getOrDefault("validationSuccess", false);
        }
        if (condition.contains("autoCheck.approved == true")) {
            return (Boolean) context.getOrDefault("autoCheckApproved", false);
        }
        return false;
    }

    // DTO Classes
    @lombok.Data
    @lombok.Builder
    public static class WorkflowDefinition {
        private String workflowId;
        private String name;
        private String description;
        private String version;
        private String tenantId;
        private String startNode;
        private Map<String, WorkflowNode> nodes;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private boolean active;
    }

    @lombok.Data
    @lombok.Builder
    public static class WorkflowNode {
        private String nodeId;
        private String name;
        private String description;
        private NodeType type;
        private Map<String, String> properties;
        private List<String> outgoingFlows;
        private List<String> incomingFlows;
    }

    @lombok.Data
    @lombok.Builder
    public static class WorkflowInstance {
        private String instanceId;
        private String workflowId;
        private String tenantId;
        private WorkflowStatus status;
        private String currentNode;
        private Map<String, Object> context;
        private String startedBy;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private LocalDateTime lastUpdated;
        private String errorMessage;
    }

    @lombok.Data
    @lombok.Builder
    public static class WorkflowTask {
        private String taskId;
        private String workflowInstanceId;
        private String tenantId;
        private String nodeId;
        private String name;
        private String description;
        private String assignedTo;
        private List<String> candidateUsers;
        private TaskStatus status;
        private LocalDateTime createdAt;
        private LocalDateTime dueDate;
        private String completedBy;
        private LocalDateTime completedAt;
        private Map<String, Object> result;
    }

    @lombok.Data
    @lombok.Builder
    public static class NodeExecutionResult {
        private ExecutionStatus status;
        private Map<String, Object> data;
        private String errorMessage;

        public static NodeExecutionResult completed(Map<String, Object> data) {
            return NodeExecutionResult.builder()
                .status(ExecutionStatus.COMPLETED)
                .data(data)
                .build();
        }

        public static NodeExecutionResult failed(String errorMessage) {
            return NodeExecutionResult.builder()
                .status(ExecutionStatus.FAILED)
                .errorMessage(errorMessage)
                .build();
        }

        public static NodeExecutionResult waiting(Map<String, Object> data) {
            return NodeExecutionResult.builder()
                .status(ExecutionStatus.WAITING)
                .data(data)
                .build();
        }
    }

    public enum NodeType {
        START, END, SERVICE_TASK, USER_TASK, DECISION, PARALLEL, TIMER, SCRIPT_TASK
    }

    public enum WorkflowStatus {
        RUNNING, COMPLETED, FAILED, SUSPENDED, WAITING
    }

    public enum TaskStatus {
        CREATED, ASSIGNED, IN_PROGRESS, COMPLETED, CANCELLED
    }

    public enum ExecutionStatus {
        COMPLETED, FAILED, WAITING
    }
}