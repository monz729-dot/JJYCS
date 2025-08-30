package com.ysc.lms.ml;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {

    private final Map<String, UserProfile> userProfiles = new ConcurrentHashMap<>();
    private final Map<String, ItemProfile> itemProfiles = new ConcurrentHashMap<>();
    private final List<UserBehavior> userBehaviors = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, List<Recommendation>> userRecommendations = new ConcurrentHashMap<>();
    private final Map<String, MLModel> models = new ConcurrentHashMap<>();

    public void initializeRecommendationSystem() {
        log.info("Initializing machine learning recommendation system");
        
        // Initialize ML models
        initializeModels();
        
        // Load sample data for training
        loadSampleData();
        
        // Train initial models
        trainModels();
    }

    public List<Recommendation> getRecommendationsForUser(String userId, String tenantId, RecommendationType type, int limit) {
        log.debug("Getting {} recommendations for user: {} (tenant: {})", type, userId, tenantId);
        
        String cacheKey = userId + ":" + type.name();
        List<Recommendation> cached = userRecommendations.get(cacheKey);
        
        if (cached != null && !cached.isEmpty()) {
            return cached.stream()
                .filter(r -> r.getTenantId().equals(tenantId))
                .limit(limit)
                .collect(Collectors.toList());
        }
        
        // Generate new recommendations
        List<Recommendation> recommendations = generateRecommendations(userId, tenantId, type, limit);
        userRecommendations.put(cacheKey, recommendations);
        
        return recommendations;
    }

    @Async
    public CompletableFuture<List<Recommendation>> getRecommendationsAsync(String userId, String tenantId, RecommendationType type, int limit) {
        return CompletableFuture.supplyAsync(() -> getRecommendationsForUser(userId, tenantId, type, limit));
    }

    public void recordUserBehavior(String userId, String tenantId, String itemId, BehaviorType behaviorType, Map<String, Object> context) {
        log.debug("Recording user behavior: {} -> {} ({})", userId, itemId, behaviorType);
        
        UserBehavior behavior = UserBehavior.builder()
            .behaviorId(UUID.randomUUID().toString())
            .userId(userId)
            .tenantId(tenantId)
            .itemId(itemId)
            .behaviorType(behaviorType)
            .context(context)
            .timestamp(LocalDateTime.now())
            .build();
        
        userBehaviors.add(behavior);
        
        // Update user profile
        updateUserProfile(userId, tenantId, behavior);
        
        // Update item profile
        updateItemProfile(itemId, tenantId, behavior);
        
        // Invalidate cached recommendations
        invalidateUserRecommendations(userId);
        
        // Trigger real-time recommendation update for high-value behaviors
        if (behaviorType == BehaviorType.PURCHASE || behaviorType == BehaviorType.ADD_TO_CART) {
            triggerRecommendationUpdate(userId, tenantId);
        }
    }

    public List<Recommendation> getProductRecommendations(String userId, String tenantId, String currentProductId, int limit) {
        log.debug("Getting product recommendations for user: {} based on product: {}", userId, currentProductId);
        
        // Content-based filtering
        List<Recommendation> contentBased = getContentBasedRecommendations(userId, tenantId, currentProductId, limit);
        
        // Collaborative filtering
        List<Recommendation> collaborative = getCollaborativeRecommendations(userId, tenantId, limit);
        
        // Hybrid approach - combine both methods
        return combineRecommendations(contentBased, collaborative, limit);
    }

    public List<Recommendation> getShippingRecommendations(String userId, String tenantId, Map<String, Object> orderData) {
        log.debug("Getting shipping recommendations for user: {}", userId);
        
        UserProfile profile = userProfiles.get(userId);
        if (profile == null) {
            return getDefaultShippingRecommendations(tenantId);
        }
        
        List<Recommendation> recommendations = new ArrayList<>();
        
        // Analyze user's shipping history
        Map<String, Integer> shippingHistory = profile.getShippingMethods();
        String preferredMethod = shippingHistory.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("SEA");
        
        // Consider order characteristics
        Double totalCbm = (Double) orderData.getOrDefault("totalCbm", 0.0);
        Double totalValue = (Double) orderData.getOrDefault("totalValue", 0.0);
        Boolean urgent = (Boolean) orderData.getOrDefault("urgent", false);
        
        // Generate shipping recommendations
        if (urgent || totalCbm > 29.0) {
            recommendations.add(createShippingRecommendation("AIR", 0.95, "긴급 배송 또는 대용량으로 항공 배송 권장", tenantId));
        } else if (preferredMethod.equals("SEA") && totalValue < 1000.0) {
            recommendations.add(createShippingRecommendation("SEA", 0.85, "과거 선호도와 비용 효율성을 고려한 해상 배송 권장", tenantId));
        } else {
            recommendations.add(createShippingRecommendation("AIR", 0.75, "배송 속도와 안정성을 고려한 항공 배송 권장", tenantId));
        }
        
        // Cost optimization recommendation
        if (totalValue > 500.0 && !urgent) {
            recommendations.add(createShippingRecommendation("SEA", 0.70, "비용 절약을 위한 해상 배송 권장", tenantId));
        }
        
        return recommendations.stream()
            .sorted((a, b) -> Double.compare(b.getConfidenceScore(), a.getConfidenceScore()))
            .collect(Collectors.toList());
    }

    public List<Recommendation> getPricingRecommendations(String userId, String tenantId, Map<String, Object> orderData) {
        log.debug("Getting pricing recommendations for user: {}", userId);
        
        UserProfile profile = userProfiles.get(userId);
        List<Recommendation> recommendations = new ArrayList<>();
        
        Double orderValue = (Double) orderData.getOrDefault("totalValue", 0.0);
        Integer itemCount = (Integer) orderData.getOrDefault("itemCount", 0);
        String customerTier = (String) orderData.getOrDefault("customerTier", "STANDARD");
        
        // Volume discount recommendation
        if (itemCount >= 10) {
            recommendations.add(Recommendation.builder()
                .recommendationId(UUID.randomUUID().toString())
                .userId(userId)
                .tenantId(tenantId)
                .type(RecommendationType.PRICING)
                .title("대량 주문 할인")
                .description("10개 이상 주문으로 5% 할인 적용 가능")
                .confidenceScore(0.90)
                .expectedBenefit("5% 비용 절약")
                .actionUrl("/api/v1/pricing/apply-volume-discount")
                .validUntil(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .build());
        }
        
        // Customer tier upgrade recommendation
        if ("STANDARD".equals(customerTier) && orderValue > 5000.0) {
            recommendations.add(Recommendation.builder()
                .recommendationId(UUID.randomUUID().toString())
                .userId(userId)
                .tenantId(tenantId)
                .type(RecommendationType.PRICING)
                .title("프리미엄 등급 업그레이드")
                .description("프리미엄 등급 업그레이드로 추가 할인 혜택 제공")
                .confidenceScore(0.80)
                .expectedBenefit("10% 추가 할인 + 무료 배송")
                .actionUrl("/api/v1/users/upgrade-tier")
                .validUntil(LocalDateTime.now().plusDays(30))
                .createdAt(LocalDateTime.now())
                .build());
        }
        
        // Seasonal promotion recommendation
        if (isSeasonalPromotionPeriod()) {
            recommendations.add(Recommendation.builder()
                .recommendationId(UUID.randomUUID().toString())
                .userId(userId)
                .tenantId(tenantId)
                .type(RecommendationType.PRICING)
                .title("시즌 특가 프로모션")
                .description("현재 진행 중인 시즌 특가로 최대 15% 할인")
                .confidenceScore(0.85)
                .expectedBenefit("최대 15% 할인")
                .actionUrl("/api/v1/promotions/seasonal")
                .validUntil(LocalDateTime.now().plusDays(14))
                .createdAt(LocalDateTime.now())
                .build());
        }
        
        return recommendations.stream()
            .sorted((a, b) -> Double.compare(b.getConfidenceScore(), a.getConfidenceScore()))
            .collect(Collectors.toList());
    }

    @Scheduled(cron = "0 0 2 * * ?") // Daily at 2 AM
    public void retrainModels() {
        log.info("Starting scheduled model retraining");
        
        try {
            trainModels();
            log.info("Model retraining completed successfully");
        } catch (Exception e) {
            log.error("Model retraining failed", e);
        }
    }

    @Scheduled(cron = "0 */30 * * * ?") // Every 30 minutes
    public void updateRecommendations() {
        log.debug("Updating cached recommendations");
        
        // Update recommendations for active users
        userProfiles.keySet().parallelStream()
            .filter(userId -> isUserActive(userId))
            .forEach(userId -> {
                try {
                    String tenantId = getUserTenantId(userId);
                    generateRecommendations(userId, tenantId, RecommendationType.PRODUCT, 10);
                } catch (Exception e) {
                    log.warn("Failed to update recommendations for user: {}", userId, e);
                }
            });
    }

    private List<Recommendation> generateRecommendations(String userId, String tenantId, RecommendationType type, int limit) {
        switch (type) {
            case PRODUCT:
                return generateProductRecommendations(userId, tenantId, limit);
            case SHIPPING:
                return generateShippingRecommendations(userId, tenantId, limit);
            case PRICING:
                return generatePricingRecommendations(userId, tenantId, limit);
            case SERVICE:
                return generateServiceRecommendations(userId, tenantId, limit);
            default:
                return new ArrayList<>();
        }
    }

    private List<Recommendation> generateProductRecommendations(String userId, String tenantId, int limit) {
        UserProfile profile = userProfiles.get(userId);
        if (profile == null) {
            return getPopularItemRecommendations(tenantId, limit);
        }
        
        List<Recommendation> recommendations = new ArrayList<>();
        
        // Based on user's purchase history
        for (String category : profile.getPreferredCategories()) {
            List<String> categoryItems = getItemsInCategory(category, tenantId);
            for (String itemId : categoryItems.subList(0, Math.min(categoryItems.size(), 2))) {
                recommendations.add(createProductRecommendation(itemId, 0.8, "선호 카테고리 기반 추천", tenantId));
            }
        }
        
        // Based on similar users (collaborative filtering)
        List<String> similarUsers = findSimilarUsers(userId, tenantId);
        for (String similarUserId : similarUsers.subList(0, Math.min(similarUsers.size(), 3))) {
            UserProfile similarProfile = userProfiles.get(similarUserId);
            if (similarProfile != null) {
                for (String itemId : similarProfile.getPurchasedItems()) {
                    if (!profile.getPurchasedItems().contains(itemId)) {
                        recommendations.add(createProductRecommendation(itemId, 0.7, "유사 사용자 기반 추천", tenantId));
                    }
                }
            }
        }
        
        return recommendations.stream()
            .distinct()
            .sorted((a, b) -> Double.compare(b.getConfidenceScore(), a.getConfidenceScore()))
            .limit(limit)
            .collect(Collectors.toList());
    }

    private List<Recommendation> generateShippingRecommendations(String userId, String tenantId, int limit) {
        // Generate shipping method recommendations based on user behavior
        return getDefaultShippingRecommendations(tenantId).stream()
            .limit(limit)
            .collect(Collectors.toList());
    }

    private List<Recommendation> generatePricingRecommendations(String userId, String tenantId, int limit) {
        // Generate pricing optimization recommendations
        List<Recommendation> recommendations = new ArrayList<>();
        
        recommendations.add(Recommendation.builder()
            .recommendationId(UUID.randomUUID().toString())
            .userId(userId)
            .tenantId(tenantId)
            .type(RecommendationType.PRICING)
            .title("비용 최적화")
            .description("배송비 절약을 위한 주문 통합 권장")
            .confidenceScore(0.75)
            .expectedBenefit("배송비 30% 절약")
            .actionUrl("/api/v1/orders/consolidate")
            .validUntil(LocalDateTime.now().plusDays(7))
            .createdAt(LocalDateTime.now())
            .build());
        
        return recommendations.stream()
            .limit(limit)
            .collect(Collectors.toList());
    }

    private List<Recommendation> generateServiceRecommendations(String userId, String tenantId, int limit) {
        // Generate service upgrade recommendations
        List<Recommendation> recommendations = new ArrayList<>();
        
        recommendations.add(Recommendation.builder()
            .recommendationId(UUID.randomUUID().toString())
            .userId(userId)
            .tenantId(tenantId)
            .type(RecommendationType.SERVICE)
            .title("프리미엄 서비스")
            .description("배송 추적 및 보험 서비스 추가")
            .confidenceScore(0.70)
            .expectedBenefit("배송 안전성 향상")
            .actionUrl("/api/v1/services/premium")
            .validUntil(LocalDateTime.now().plusDays(30))
            .createdAt(LocalDateTime.now())
            .build());
        
        return recommendations.stream()
            .limit(limit)
            .collect(Collectors.toList());
    }

    private List<Recommendation> getContentBasedRecommendations(String userId, String tenantId, String itemId, int limit) {
        ItemProfile itemProfile = itemProfiles.get(itemId);
        if (itemProfile == null) {
            return new ArrayList<>();
        }
        
        List<Recommendation> recommendations = new ArrayList<>();
        
        // Find similar items based on categories and attributes
        for (ItemProfile otherItem : itemProfiles.values()) {
            if (!otherItem.getItemId().equals(itemId) && 
                otherItem.getTenantId().equals(tenantId) &&
                hasCommonCategories(itemProfile, otherItem)) {
                
                double similarity = calculateItemSimilarity(itemProfile, otherItem);
                recommendations.add(createProductRecommendation(
                    otherItem.getItemId(), similarity, "유사 상품 추천", tenantId));
            }
        }
        
        return recommendations.stream()
            .sorted((a, b) -> Double.compare(b.getConfidenceScore(), a.getConfidenceScore()))
            .limit(limit)
            .collect(Collectors.toList());
    }

    private List<Recommendation> getCollaborativeRecommendations(String userId, String tenantId, int limit) {
        List<String> similarUsers = findSimilarUsers(userId, tenantId);
        List<Recommendation> recommendations = new ArrayList<>();
        
        for (String similarUserId : similarUsers) {
            UserProfile similarProfile = userProfiles.get(similarUserId);
            if (similarProfile != null) {
                UserProfile userProfile = userProfiles.get(userId);
                for (String itemId : similarProfile.getPurchasedItems()) {
                    if (userProfile == null || !userProfile.getPurchasedItems().contains(itemId)) {
                        recommendations.add(createProductRecommendation(itemId, 0.7, "협업 필터링 추천", tenantId));
                    }
                }
            }
        }
        
        return recommendations.stream()
            .distinct()
            .sorted((a, b) -> Double.compare(b.getConfidenceScore(), a.getConfidenceScore()))
            .limit(limit)
            .collect(Collectors.toList());
    }

    private List<Recommendation> combineRecommendations(List<Recommendation> contentBased, List<Recommendation> collaborative, int limit) {
        Map<String, Recommendation> combined = new HashMap<>();
        
        // Weight content-based recommendations (70%)
        for (Recommendation rec : contentBased) {
            rec.setConfidenceScore(rec.getConfidenceScore() * 0.7);
            combined.put(rec.getItemId(), rec);
        }
        
        // Weight collaborative recommendations (30%) and merge
        for (Recommendation rec : collaborative) {
            rec.setConfidenceScore(rec.getConfidenceScore() * 0.3);
            Recommendation existing = combined.get(rec.getItemId());
            if (existing != null) {
                existing.setConfidenceScore(existing.getConfidenceScore() + rec.getConfidenceScore());
            } else {
                combined.put(rec.getItemId(), rec);
            }
        }
        
        return combined.values().stream()
            .sorted((a, b) -> Double.compare(b.getConfidenceScore(), a.getConfidenceScore()))
            .limit(limit)
            .collect(Collectors.toList());
    }

    private void initializeModels() {
        // Initialize recommendation models
        models.put("USER_ITEM_CF", MLModel.builder()
            .modelId("USER_ITEM_CF")
            .modelType(ModelType.COLLABORATIVE_FILTERING)
            .name("User-Item Collaborative Filtering")
            .version("1.0")
            .status(ModelStatus.ACTIVE)
            .accuracy(0.85)
            .lastTrained(LocalDateTime.now())
            .build());
            
        models.put("CONTENT_BASED", MLModel.builder()
            .modelId("CONTENT_BASED")
            .modelType(ModelType.CONTENT_BASED)
            .name("Content-Based Filtering")
            .version("1.0")
            .status(ModelStatus.ACTIVE)
            .accuracy(0.78)
            .lastTrained(LocalDateTime.now())
            .build());
            
        models.put("HYBRID_RECOMMENDER", MLModel.builder()
            .modelId("HYBRID_RECOMMENDER")
            .modelType(ModelType.HYBRID)
            .name("Hybrid Recommendation System")
            .version("1.0")
            .status(ModelStatus.ACTIVE)
            .accuracy(0.88)
            .lastTrained(LocalDateTime.now())
            .build());
    }

    private void loadSampleData() {
        // Load sample user profiles and item profiles for training
        log.debug("Loading sample data for ML training");
        
        // Sample user profiles
        for (int i = 1; i <= 100; i++) {
            String userId = "user" + i;
            userProfiles.put(userId, UserProfile.builder()
                .userId(userId)
                .tenantId("default")
                .preferredCategories(generateRandomCategories())
                .purchasedItems(generateRandomItems())
                .shippingMethods(generateRandomShippingHistory())
                .lastActive(LocalDateTime.now().minusDays(i % 30))
                .totalOrders(i % 50)
                .averageOrderValue(100.0 + (i % 1000))
                .build());
        }
        
        // Sample item profiles
        String[] categories = {"Electronics", "Clothing", "Books", "Home", "Sports"};
        for (int i = 1; i <= 500; i++) {
            String itemId = "item" + i;
            itemProfiles.put(itemId, ItemProfile.builder()
                .itemId(itemId)
                .tenantId("default")
                .category(categories[i % categories.length])
                .price(10.0 + (i % 1000))
                .popularity(Math.random())
                .attributes(generateRandomAttributes())
                .viewCount(i % 1000)
                .purchaseCount(i % 100)
                .build());
        }
    }

    private void trainModels() {
        log.info("Training ML models");
        
        for (MLModel model : models.values()) {
            try {
                trainModel(model);
                model.setLastTrained(LocalDateTime.now());
                model.setStatus(ModelStatus.ACTIVE);
                log.debug("Model trained successfully: {}", model.getModelId());
            } catch (Exception e) {
                log.error("Failed to train model: {}", model.getModelId(), e);
                model.setStatus(ModelStatus.FAILED);
            }
        }
    }

    private void trainModel(MLModel model) {
        // Mock training implementation
        // In real implementation, this would use actual ML libraries like Weka, DL4J, or TensorFlow Java API
        log.debug("Training model: {} ({})", model.getName(), model.getModelType());
        
        // Simulate training time
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Update accuracy based on model type
        switch (model.getModelType()) {
            case COLLABORATIVE_FILTERING:
                model.setAccuracy(0.82 + Math.random() * 0.08);
                break;
            case CONTENT_BASED:
                model.setAccuracy(0.75 + Math.random() * 0.08);
                break;
            case HYBRID:
                model.setAccuracy(0.85 + Math.random() * 0.08);
                break;
        }
    }

    private void updateUserProfile(String userId, String tenantId, UserBehavior behavior) {
        UserProfile profile = userProfiles.computeIfAbsent(userId, k -> UserProfile.builder()
            .userId(userId)
            .tenantId(tenantId)
            .preferredCategories(new ArrayList<>())
            .purchasedItems(new ArrayList<>())
            .shippingMethods(new HashMap<>())
            .lastActive(LocalDateTime.now())
            .totalOrders(0)
            .averageOrderValue(0.0)
            .build());
            
        profile.setLastActive(LocalDateTime.now());
        
        if (behavior.getBehaviorType() == BehaviorType.PURCHASE) {
            profile.getPurchasedItems().add(behavior.getItemId());
            profile.setTotalOrders(profile.getTotalOrders() + 1);
            
            ItemProfile item = itemProfiles.get(behavior.getItemId());
            if (item != null && !profile.getPreferredCategories().contains(item.getCategory())) {
                profile.getPreferredCategories().add(item.getCategory());
            }
        }
    }

    private void updateItemProfile(String itemId, String tenantId, UserBehavior behavior) {
        ItemProfile profile = itemProfiles.get(itemId);
        if (profile != null) {
            switch (behavior.getBehaviorType()) {
                case VIEW:
                    profile.setViewCount(profile.getViewCount() + 1);
                    break;
                case PURCHASE:
                    profile.setPurchaseCount(profile.getPurchaseCount() + 1);
                    break;
            }
            
            // Update popularity score
            double popularity = (profile.getPurchaseCount() * 2.0 + profile.getViewCount()) / 
                               (profile.getViewCount() + profile.getPurchaseCount() + 1);
            profile.setPopularity(Math.min(popularity, 1.0));
        }
    }

    private void invalidateUserRecommendations(String userId) {
        userRecommendations.entrySet().removeIf(entry -> entry.getKey().startsWith(userId + ":"));
    }

    private void triggerRecommendationUpdate(String userId, String tenantId) {
        CompletableFuture.runAsync(() -> {
            generateRecommendations(userId, tenantId, RecommendationType.PRODUCT, 10);
        });
    }

    private List<String> findSimilarUsers(String userId, String tenantId) {
        UserProfile targetProfile = userProfiles.get(userId);
        if (targetProfile == null) {
            return new ArrayList<>();
        }
        
        return userProfiles.values().stream()
            .filter(profile -> !profile.getUserId().equals(userId))
            .filter(profile -> profile.getTenantId().equals(tenantId))
            .sorted((a, b) -> Double.compare(
                calculateUserSimilarity(targetProfile, b),
                calculateUserSimilarity(targetProfile, a)))
            .limit(10)
            .map(UserProfile::getUserId)
            .collect(Collectors.toList());
    }

    private double calculateUserSimilarity(UserProfile profile1, UserProfile profile2) {
        // Simple Jaccard similarity for purchased items
        Set<String> items1 = new HashSet<>(profile1.getPurchasedItems());
        Set<String> items2 = new HashSet<>(profile2.getPurchasedItems());
        
        Set<String> intersection = new HashSet<>(items1);
        intersection.retainAll(items2);
        
        Set<String> union = new HashSet<>(items1);
        union.addAll(items2);
        
        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }

    private boolean hasCommonCategories(ItemProfile item1, ItemProfile item2) {
        return item1.getCategory().equals(item2.getCategory());
    }

    private double calculateItemSimilarity(ItemProfile item1, ItemProfile item2) {
        double similarity = 0.0;
        
        // Category similarity
        if (item1.getCategory().equals(item2.getCategory())) {
            similarity += 0.5;
        }
        
        // Price similarity
        double priceDiff = Math.abs(item1.getPrice() - item2.getPrice());
        double maxPrice = Math.max(item1.getPrice(), item2.getPrice());
        similarity += 0.3 * (1.0 - (priceDiff / maxPrice));
        
        // Popularity similarity
        double popularityDiff = Math.abs(item1.getPopularity() - item2.getPopularity());
        similarity += 0.2 * (1.0 - popularityDiff);
        
        return Math.min(similarity, 1.0);
    }

    private List<String> getItemsInCategory(String category, String tenantId) {
        return itemProfiles.values().stream()
            .filter(item -> item.getCategory().equals(category))
            .filter(item -> item.getTenantId().equals(tenantId))
            .sorted((a, b) -> Double.compare(b.getPopularity(), a.getPopularity()))
            .map(ItemProfile::getItemId)
            .limit(10)
            .collect(Collectors.toList());
    }

    private List<Recommendation> getPopularItemRecommendations(String tenantId, int limit) {
        return itemProfiles.values().stream()
            .filter(item -> item.getTenantId().equals(tenantId))
            .sorted((a, b) -> Double.compare(b.getPopularity(), a.getPopularity()))
            .limit(limit)
            .map(item -> createProductRecommendation(item.getItemId(), 0.6, "인기 상품 추천", tenantId))
            .collect(Collectors.toList());
    }

    private List<Recommendation> getDefaultShippingRecommendations(String tenantId) {
        List<Recommendation> recommendations = new ArrayList<>();
        
        recommendations.add(createShippingRecommendation("SEA", 0.8, "비용 효율적인 해상 배송", tenantId));
        recommendations.add(createShippingRecommendation("AIR", 0.7, "빠른 항공 배송", tenantId));
        
        return recommendations;
    }

    private Recommendation createProductRecommendation(String itemId, double confidence, String reason, String tenantId) {
        return Recommendation.builder()
            .recommendationId(UUID.randomUUID().toString())
            .tenantId(tenantId)
            .type(RecommendationType.PRODUCT)
            .itemId(itemId)
            .title("상품 추천: " + itemId)
            .description(reason)
            .confidenceScore(confidence)
            .reason(reason)
            .actionUrl("/api/v1/products/" + itemId)
            .validUntil(LocalDateTime.now().plusDays(7))
            .createdAt(LocalDateTime.now())
            .build();
    }

    private Recommendation createShippingRecommendation(String method, double confidence, String reason, String tenantId) {
        return Recommendation.builder()
            .recommendationId(UUID.randomUUID().toString())
            .tenantId(tenantId)
            .type(RecommendationType.SHIPPING)
            .title(method + " 배송")
            .description(reason)
            .confidenceScore(confidence)
            .reason(reason)
            .expectedBenefit(method.equals("SEA") ? "비용 절약" : "빠른 배송")
            .actionUrl("/api/v1/shipping/" + method.toLowerCase())
            .validUntil(LocalDateTime.now().plusDays(30))
            .createdAt(LocalDateTime.now())
            .build();
    }

    private boolean isUserActive(String userId) {
        UserProfile profile = userProfiles.get(userId);
        return profile != null && profile.getLastActive().isAfter(LocalDateTime.now().minusDays(7));
    }

    private String getUserTenantId(String userId) {
        UserProfile profile = userProfiles.get(userId);
        return profile != null ? profile.getTenantId() : "default";
    }

    private boolean isSeasonalPromotionPeriod() {
        // Simple check for seasonal promotion (e.g., during certain months)
        int month = LocalDateTime.now().getMonthValue();
        return month == 12 || month == 1 || month == 6; // December, January, June
    }

    private List<String> generateRandomCategories() {
        String[] categories = {"Electronics", "Clothing", "Books", "Home", "Sports"};
        List<String> result = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            result.add(categories[(int) (Math.random() * categories.length)]);
        }
        return result;
    }

    private List<String> generateRandomItems() {
        List<String> items = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            items.add("item" + (int) (Math.random() * 500 + 1));
        }
        return items;
    }

    private Map<String, Integer> generateRandomShippingHistory() {
        Map<String, Integer> history = new HashMap<>();
        history.put("SEA", (int) (Math.random() * 10));
        history.put("AIR", (int) (Math.random() * 5));
        return history;
    }

    private Map<String, Object> generateRandomAttributes() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("brand", "Brand" + (int) (Math.random() * 10));
        attributes.put("weight", Math.random() * 10);
        attributes.put("color", "Color" + (int) (Math.random() * 5));
        return attributes;
    }

    // DTO Classes
    @lombok.Data
    @lombok.Builder
    public static class UserProfile {
        private String userId;
        private String tenantId;
        private List<String> preferredCategories;
        private List<String> purchasedItems;
        private Map<String, Integer> shippingMethods;
        private LocalDateTime lastActive;
        private int totalOrders;
        private double averageOrderValue;
    }

    @lombok.Data
    @lombok.Builder
    public static class ItemProfile {
        private String itemId;
        private String tenantId;
        private String category;
        private double price;
        private double popularity;
        private Map<String, Object> attributes;
        private int viewCount;
        private int purchaseCount;
    }

    @lombok.Data
    @lombok.Builder
    public static class UserBehavior {
        private String behaviorId;
        private String userId;
        private String tenantId;
        private String itemId;
        private BehaviorType behaviorType;
        private Map<String, Object> context;
        private LocalDateTime timestamp;
    }

    @lombok.Data
    @lombok.Builder
    public static class Recommendation {
        private String recommendationId;
        private String userId;
        private String tenantId;
        private RecommendationType type;
        private String itemId;
        private String title;
        private String description;
        private double confidenceScore;
        private String reason;
        private String expectedBenefit;
        private String actionUrl;
        private LocalDateTime validUntil;
        private LocalDateTime createdAt;
        private boolean clicked;
        private boolean converted;
    }

    @lombok.Data
    @lombok.Builder
    public static class MLModel {
        private String modelId;
        private ModelType modelType;
        private String name;
        private String version;
        private ModelStatus status;
        private double accuracy;
        private LocalDateTime lastTrained;
        private LocalDateTime nextTraining;
        private Map<String, Object> parameters;
    }

    public enum BehaviorType {
        VIEW, CLICK, ADD_TO_CART, PURCHASE, REMOVE_FROM_CART, SEARCH, FILTER, SHARE
    }

    public enum RecommendationType {
        PRODUCT, SHIPPING, PRICING, SERVICE, CROSS_SELL, UP_SELL
    }

    public enum ModelType {
        COLLABORATIVE_FILTERING, CONTENT_BASED, HYBRID, DEEP_LEARNING, MATRIX_FACTORIZATION
    }

    public enum ModelStatus {
        ACTIVE, TRAINING, FAILED, DEPRECATED
    }
}