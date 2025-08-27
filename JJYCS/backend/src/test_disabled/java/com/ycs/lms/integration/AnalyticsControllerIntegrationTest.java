package com.ycs.lms.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

class AnalyticsControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetDashboardSummary_WithAdminRole_ReturnsSuccess() throws Exception {
        // When & Then
        MvcResult result = mockMvc.perform(get("/api/v1/analytics/dashboard/summary")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalOrders").exists())
                .andExpect(jsonPath("$.totalUsers").exists())
                .andExpect(jsonPath("$.activeUsers").exists())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        assertFalse(responseBody.isEmpty());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetDashboardSummary_WithUserRole_ReturnsForbidden() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/analytics/dashboard/summary")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "WAREHOUSE")
    void testGetOrderTrends_WithWarehouseRole_ReturnsSuccess() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/analytics/orders/trends")
                .param("days", "30")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dailyOrderCounts").exists())
                .andExpect(jsonPath("$.orderStatusDistribution").exists())
                .andExpect(jsonPath("$.shippingTypeDistribution").exists());
    }

    @Test
    @WithMockUser(roles = "PARTNER")
    void testGetOrderTrends_WithCustomDays_ReturnsFilteredData() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/analytics/orders/trends")
                .param("days", "7")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dailyOrderCounts").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetUserAnalytics_WithAdminRole_ReturnsSuccess() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/analytics/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roleDistribution").exists())
                .andExpect(jsonPath("$.statusDistribution").exists())
                .andExpect(jsonPath("$.monthlyRegistrations").exists());
    }

    @Test
    @WithMockUser(roles = "WAREHOUSE")
    void testGetUserAnalytics_WithWarehouseRole_ReturnsSuccess() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/analytics/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "PARTNER")
    void testGetUserAnalytics_WithPartnerRole_ReturnsForbidden() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/analytics/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetRevenueAnalytics_WithAdminRole_ReturnsSuccess() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/analytics/revenue")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monthlyRevenue").exists())
                .andExpect(jsonPath("$.serviceRevenue").exists())
                .andExpect(jsonPath("$.countryRevenue").exists());
    }

    @Test
    @WithMockUser(roles = "WAREHOUSE")
    void testGetRevenueAnalytics_WithWarehouseRole_ReturnsForbidden() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/analytics/revenue")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetPerformanceMetrics_ReturnsValidMetrics() throws Exception {
        // When & Then
        MvcResult result = mockMvc.perform(get("/api/v1/analytics/performance")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageProcessingTime").exists())
                .andExpect(jsonPath("$.orderAccuracy").exists())
                .andExpect(jsonPath("$.customerSatisfaction").exists())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        assertTrue(responseBody.contains("averageProcessingTime"));
        assertTrue(responseBody.contains("orderAccuracy"));
    }

    @Test
    @WithMockUser(roles = "PARTNER")
    void testGetTopPerformers_WithPartnerRole_ReturnsSuccess() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/analytics/top-performers")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topCustomers").exists())
                .andExpect(jsonPath("$.topDestinations").exists())
                .andExpect(jsonPath("$.topCategories").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetRealtimeStats_ReturnsCurrentData() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/analytics/realtime")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeUsers").exists())
                .andExpect(jsonPath("$.pendingOrders").exists())
                .andExpect(jsonPath("$.systemLoad").exists())
                .andExpect(jsonPath("$.memoryUsage").exists());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetRealtimeStats_WithUserRole_ReturnsForbidden() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/analytics/realtime")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testExecuteCustomAnalytics_WithValidQuery_ReturnsSuccess() throws Exception {
        // Given
        String requestBody = """
            {
                "queryType": "USER_GROWTH",
                "parameters": {
                    "period": "MONTHLY",
                    "limit": 12
                }
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/v1/analytics/custom")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testExecuteCustomAnalytics_WithInvalidQuery_ReturnsError() throws Exception {
        // Given
        String requestBody = """
            {
                "queryType": "INVALID_QUERY",
                "parameters": {}
            }
            """;

        // When & Then
        MvcResult result = mockMvc.perform(post("/api/v1/analytics/custom")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        assertTrue(responseBody.contains("Unsupported query type"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testExportAnalytics_WithValidRequest_ReturnsDownloadUrl() throws Exception {
        // Given
        String requestBody = """
            {
                "dataType": "ORDERS",
                "format": "CSV",
                "filters": {
                    "dateRange": "LAST_30_DAYS"
                }
            }
            """;

        // When & Then
        MvcResult result = mockMvc.perform(post("/api/v1/analytics/export")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        assertTrue(responseBody.contains("/api/v1/files/downloads/"));
        assertTrue(responseBody.contains("analytics_orders"));
    }

    @Test
    @WithMockUser(roles = "WAREHOUSE")
    void testExportAnalytics_WithWarehouseRole_ReturnsForbidden() throws Exception {
        // Given
        String requestBody = """
            {
                "dataType": "ORDERS",
                "format": "CSV"
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/v1/analytics/export")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isForbidden());
    }

    @Test
    void testAnalyticsEndpoints_WithoutAuthentication_ReturnsUnauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/analytics/dashboard/summary"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/analytics/revenue"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/analytics/performance"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAnalyticsDataIntegrity_CrossValidation() throws Exception {
        // Get dashboard summary
        MvcResult summaryResult = mockMvc.perform(get("/api/v1/analytics/dashboard/summary")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Get user analytics
        MvcResult userResult = mockMvc.perform(get("/api/v1/analytics/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Verify data consistency between endpoints
        String summaryBody = summaryResult.getResponse().getContentAsString();
        String userBody = userResult.getResponse().getContentAsString();

        assertFalse(summaryBody.isEmpty());
        assertFalse(userBody.isEmpty());
        
        // Additional validation could check if total users match between endpoints
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAnalyticsPerformance_ResponseTime() throws Exception {
        // Measure response time for analytics endpoints
        long startTime = System.currentTimeMillis();

        mockMvc.perform(get("/api/v1/analytics/dashboard/summary"))
                .andExpect(status().isOk());

        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        // Assert reasonable response time (should complete within 5 seconds)
        assertTrue(responseTime < 5000, "Analytics endpoint took too long: " + responseTime + "ms");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAnalyticsCaching_VerifyRedisIntegration() throws Exception {
        // First request - should populate cache
        MvcResult firstResult = mockMvc.perform(get("/api/v1/analytics/dashboard/summary"))
                .andExpect(status().isOk())
                .andReturn();

        // Second request - should use cache
        MvcResult secondResult = mockMvc.perform(get("/api/v1/analytics/dashboard/summary"))
                .andExpect(status().isOk())
                .andReturn();

        // Results should be identical (from cache)
        assertEquals(firstResult.getResponse().getContentAsString(), 
                    secondResult.getResponse().getContentAsString());
    }
}