package com.ycs.lms.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@Testcontainers
@ActiveProfiles("integration-test")
@Transactional
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    // Test containers
    @Container
    static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("ycs_lms_test")
            .withUsername("test_user")
            .withPassword("test_password")
            .withReuse(true);

    @Container
    static GenericContainer<?> redisContainer = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379)
            .withReuse(true);

    @Container
    static ElasticsearchContainer elasticsearchContainer = new ElasticsearchContainer(
            DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch:8.8.0"))
            .withEnv("discovery.type", "single-node")
            .withEnv("xpack.security.enabled", "false")
            .withReuse(true);

    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.4.0"))
            .withReuse(true);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // MySQL configuration
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        
        // JPA configuration for MySQL
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.MySQL8Dialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        
        // Redis configuration
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(6379));
        
        // Elasticsearch configuration
        registry.add("spring.elasticsearch.uris", () -> 
            "http://" + elasticsearchContainer.getHost() + ":" + elasticsearchContainer.getMappedPort(9200));
        
        // Kafka configuration
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
        
        // Test specific configurations
        registry.add("spring.mail.host", () -> "localhost");
        registry.add("spring.mail.port", () -> "3025"); // Mock SMTP server port
        
        // Disable external API calls in tests
        registry.add("ycs.external-api.ems.enabled", () -> "false");
        registry.add("ycs.external-api.hs-code.enabled", () -> "false");
        registry.add("ycs.external-api.exchange-rate.enabled", () -> "false");
    }

    @BeforeEach
    void setUp() {
        // Common test setup
        cleanupTestData();
        setupTestData();
    }

    protected void cleanupTestData() {
        // Override in subclasses if needed
    }

    protected void setupTestData() {
        // Override in subclasses if needed
    }

    // Helper methods for common test operations
    protected String asJsonString(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert object to JSON", e);
        }
    }

    protected <T> T fromJsonString(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON", e);
        }
    }

    // Test data builders
    protected TestDataBuilder testDataBuilder() {
        return new TestDataBuilder();
    }

    public static class TestDataBuilder {
        
        public UserTestData createUser() {
            return new UserTestData();
        }
        
        public OrderTestData createOrder() {
            return new OrderTestData();
        }
        
        public static class UserTestData {
            private String email = "test@example.com";
            private String name = "Test User";
            private String role = "USER";
            private String status = "ACTIVE";
            private boolean active = true;
            
            public UserTestData withEmail(String email) {
                this.email = email;
                return this;
            }
            
            public UserTestData withName(String name) {
                this.name = name;
                return this;
            }
            
            public UserTestData withRole(String role) {
                this.role = role;
                return this;
            }
            
            public UserTestData withStatus(String status) {
                this.status = status;
                return this;
            }
            
            public UserTestData withActive(boolean active) {
                this.active = active;
                return this;
            }
            
            // Build method would create actual User entity
            public Object build() {
                return new Object(); // Placeholder - would return actual User entity
            }
        }
        
        public static class OrderTestData {
            private String orderNumber = "ORD-" + System.currentTimeMillis();
            private String recipientName = "Test Recipient";
            private String senderName = "Test Sender";
            private String status = "RECEIVED";
            private String orderType = "AIR";
            
            public OrderTestData withOrderNumber(String orderNumber) {
                this.orderNumber = orderNumber;
                return this;
            }
            
            public OrderTestData withRecipientName(String recipientName) {
                this.recipientName = recipientName;
                return this;
            }
            
            public OrderTestData withSenderName(String senderName) {
                this.senderName = senderName;
                return this;
            }
            
            public OrderTestData withStatus(String status) {
                this.status = status;
                return this;
            }
            
            public OrderTestData withOrderType(String orderType) {
                this.orderType = orderType;
                return this;
            }
            
            // Build method would create actual Order entity
            public Object build() {
                return new Object(); // Placeholder - would return actual Order entity
            }
        }
    }
}