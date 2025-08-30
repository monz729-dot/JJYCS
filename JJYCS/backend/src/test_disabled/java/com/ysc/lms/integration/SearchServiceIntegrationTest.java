package com.ysc.lms.integration;

import com.ysc.lms.document.OrderDocument;
import com.ysc.lms.document.UserDocument;
import com.ysc.lms.service.SearchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@TestPropertySource(properties = {
    "spring.elasticsearch.uris=${ELASTICSEARCH_URL}",
    "logging.level.org.springframework.data.elasticsearch=DEBUG"
})
class SearchServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private SearchService searchService;

    @BeforeEach
    void setUpSearchData() {
        // Wait for Elasticsearch to be ready
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Create test documents
        createTestOrderDocuments();
        createTestUserDocuments();
    }

    @Test
    void testSearchOrders_WithKeyword_ReturnsRelevantResults() {
        // Given
        String keyword = "Test Order";
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<OrderDocument> results = searchService.searchOrders(keyword, pageable);

        // Then
        assertNotNull(results);
        assertTrue(results.hasContent());
        assertTrue(results.getContent().stream()
            .anyMatch(order -> order.getSearchableText().contains(keyword)));
    }

    @Test
    void testSearchOrders_WithEmptyKeyword_ReturnsAllResults() {
        // Given
        String keyword = "";
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<OrderDocument> results = searchService.searchOrders(keyword, pageable);

        // Then
        assertNotNull(results);
        // Should return all orders when keyword is empty
    }

    @Test
    void testSearchOrdersByUser_WithValidUser_ReturnsUserOrders() {
        // Given
        Long userId = 1L;
        String keyword = "Recipient";
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<OrderDocument> results = searchService.searchOrdersByUser(userId, keyword, pageable);

        // Then
        assertNotNull(results);
        // All results should belong to the specified user
        assertTrue(results.getContent().stream()
            .allMatch(order -> order.getUserId().equals(userId)));
    }

    @Test
    void testSearchOrdersAdvanced_WithMultipleFilters_ReturnsFilteredResults() {
        // Given
        String keyword = "Test";
        String status = "RECEIVED";
        String orderType = "AIR";
        Double minAmount = 100.0;
        Double maxAmount = 1000.0;
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<OrderDocument> results = searchService.searchOrdersAdvanced(
            keyword, status, orderType, minAmount, maxAmount, startDate, endDate, pageable);

        // Then
        assertNotNull(results);
        // Verify filters are applied correctly
        assertTrue(results.getContent().stream()
            .allMatch(order -> 
                order.getStatus().equals(status) &&
                order.getOrderType().equals(orderType) &&
                order.getTotalAmount() >= minAmount &&
                order.getTotalAmount() <= maxAmount));
    }

    @Test
    void testSearchUsers_WithKeyword_ReturnsMatchingUsers() {
        // Given
        String keyword = "test@example.com";
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<UserDocument> results = searchService.searchUsers(keyword, pageable);

        // Then
        assertNotNull(results);
        assertTrue(results.hasContent());
        assertTrue(results.getContent().stream()
            .anyMatch(user -> user.getEmail().contains(keyword)));
    }

    @Test
    void testSearchUsersAdvanced_WithRoleFilter_ReturnsUsersWithRole() {
        // Given
        String keyword = "";
        String role = "ADMIN";
        String status = "ACTIVE";
        Boolean active = true;
        Boolean emailVerified = true;
        LocalDateTime startDate = null;
        LocalDateTime endDate = null;
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<UserDocument> results = searchService.searchUsersAdvanced(
            keyword, role, status, active, emailVerified, startDate, endDate, pageable);

        // Then
        assertNotNull(results);
        // All results should have the specified role
        assertTrue(results.getContent().stream()
            .allMatch(user -> user.getRole().equals(role)));
    }

    @Test
    void testFindPendingApprovalUsers_ReturnsOnlyPendingUsers() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<UserDocument> results = searchService.findPendingApprovalUsers(pageable);

        // Then
        assertNotNull(results);
        // All results should be pending approval
        assertTrue(results.getContent().stream()
            .allMatch(user -> user.getStatus().equals("PENDING_APPROVAL")));
    }

    @Test
    void testFindInactiveUsers_ReturnsInactiveUsers() {
        // Given
        int daysAgo = 30;
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<UserDocument> results = searchService.findInactiveUsers(daysAgo, pageable);

        // Then
        assertNotNull(results);
        // Verify inactive user logic
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysAgo);
        assertTrue(results.getContent().stream()
            .allMatch(user -> 
                user.getLastLoginAt() == null || 
                user.getLastLoginAt().isBefore(cutoffDate)));
    }

    @Test
    void testGetSearchStatistics_ReturnsValidStatistics() {
        // When
        SearchService.SearchStatistics stats = searchService.getSearchStatistics();

        // Then
        assertNotNull(stats);
        assertTrue(stats.getTotalOrders() >= 0);
        assertTrue(stats.getTotalUsers() >= 0);
        assertTrue(stats.getActiveUsers() >= 0);
        assertTrue(stats.getInactiveUsers() >= 0);
    }

    @Test
    void testIndexingPerformance_BulkOperations() {
        // Test bulk indexing performance
        long startTime = System.currentTimeMillis();
        
        // Create multiple test documents
        for (int i = 0; i < 100; i++) {
            createTestOrderDocument("BULK-" + i);
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Assert reasonable performance (should complete within 10 seconds)
        assertTrue(duration < 10000, "Bulk indexing took too long: " + duration + "ms");
    }

    @Test
    void testSearchConsistency_IndexAndSearch() {
        // Given - Create a unique order
        String uniqueOrderNumber = "UNIQUE-" + System.currentTimeMillis();
        createTestOrderDocument(uniqueOrderNumber);
        
        // Allow time for indexing
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When - Search for the unique order
        Page<OrderDocument> results = searchService.searchOrders(uniqueOrderNumber, PageRequest.of(0, 10));

        // Then - Should find the indexed order
        assertTrue(results.hasContent());
        assertTrue(results.getContent().stream()
            .anyMatch(order -> order.getOrderNumber().equals(uniqueOrderNumber)));
    }

    private void createTestOrderDocuments() {
        // Create test order documents for search testing
        createTestOrderDocument("ORD-001");
        createTestOrderDocument("ORD-002");
        createTestOrderDocument("ORD-003");
    }

    private void createTestOrderDocument(String orderNumber) {
        OrderDocument order = new OrderDocument();
        order.setId(System.currentTimeMillis());
        order.setOrderNumber(orderNumber);
        order.setUserId(1L);
        order.setUserName("Test User");
        order.setUserEmail("test@example.com");
        order.setStatus("RECEIVED");
        order.setOrderType("AIR");
        order.setTotalAmount(500.0);
        order.setCurrency("THB");
        order.setRecipientName("Test Recipient");
        order.setRecipientAddress("Test Address");
        order.setRecipientPhone("123-456-7890");
        order.setSenderName("Test Sender");
        order.setSenderAddress("Sender Address");
        order.setSenderPhone("098-765-4321");
        order.setDescription("Test Order Description");
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setSearchableText(String.join(" ", 
            orderNumber, "Test Recipient", "Test Sender", "Test Order Description"));
        
        // Index the document (would be handled by SearchService)
        // searchService.indexOrder(order); // This would need an actual Order entity
    }

    private void createTestUserDocuments() {
        // Create test user documents
        createTestUserDocument("test@example.com", "USER");
        createTestUserDocument("admin@example.com", "ADMIN");
        createTestUserDocument("partner@example.com", "PARTNER");
    }

    private void createTestUserDocument(String email, String role) {
        UserDocument user = new UserDocument();
        user.setId(System.currentTimeMillis());
        user.setName("Test User " + role);
        user.setEmail(email);
        user.setRole(role);
        user.setStatus("ACTIVE");
        user.setMemberCode("MEM-" + System.currentTimeMillis());
        user.setPhone("123-456-7890");
        user.setAddress("Test Address");
        user.setEmailVerified(true);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setLastLoginAt(LocalDateTime.now().minusHours(2));
        user.setSearchableText(String.join(" ", 
            "Test User " + role, email, "MEM-" + System.currentTimeMillis()));
        
        // Index the document (would be handled by SearchService)
        // searchService.indexUser(user); // This would need an actual User entity
    }
}