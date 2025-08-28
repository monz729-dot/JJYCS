package com.ycs.lms.repository;

import com.ycs.lms.entity.Order;
import com.ycs.lms.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    @EntityGraph(attributePaths = {"user"})
    Optional<Order> findByOrderNumber(String orderNumber);
    
    @EntityGraph(attributePaths = {"user"})
    List<Order> findByUser(User user);
    
    @EntityGraph(attributePaths = {"user"})
    Page<Order> findByUser(User user, Pageable pageable);
    
    @EntityGraph(attributePaths = {"user"})
    List<Order> findAll();
    
    @EntityGraph(attributePaths = {"user"})
    Page<Order> findAll(Pageable pageable);
    
    List<Order> findByStatus(Order.OrderStatus status);
    
    Page<Order> findByStatus(Order.OrderStatus status, Pageable pageable);
    
    @Query(value = "SELECT o FROM Order o JOIN FETCH o.user WHERE o.user.id = :userId ORDER BY o.createdAt DESC",
           countQuery = "SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId")
    Page<Order> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);
    
    @Query("SELECT o FROM Order o JOIN FETCH o.user WHERE o.storageLocation IS NOT NULL AND o.status IN ('ARRIVED', 'REPACKING')")
    List<Order> findOrdersInWarehouse();
    
    @Query("SELECT o FROM Order o JOIN FETCH o.user WHERE o.user.id = :userId AND o.storageLocation IS NOT NULL AND o.status IN ('ARRIVED', 'REPACKING')")
    List<Order> findWarehouseOrdersByUserId(@Param("userId") Long userId);
    
    @Query("SELECT o FROM Order o JOIN FETCH o.user WHERE o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findOrdersBetweenDatesWithUser(@Param("startDate") LocalDateTime startDate, 
                                              @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT o FROM Order o JOIN FETCH o.user WHERE o.country = :country")
    List<Order> findByCountryWithUser(@Param("country") String country);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId AND o.status = :status")
    Long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Order.OrderStatus status);
    
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findOrdersBetweenDates(@Param("startDate") LocalDateTime startDate, 
                                      @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT o FROM Order o WHERE o.country = :country")
    List<Order> findByCountry(@Param("country") String country);
    
    // 오늘의 주문 번호 생성을 위한 쿼리
    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderNumber LIKE :pattern")
    Long countByOrderNumberPattern(@Param("pattern") String pattern);
    
    // 고급 검색 기능
    @Query(value = "SELECT o FROM Order o JOIN FETCH o.user u WHERE " +
           "(:orderNumber IS NULL OR o.orderNumber LIKE %:orderNumber%) AND " +
           "(:recipientName IS NULL OR o.recipientName LIKE %:recipientName%) AND " +
           "(:status IS NULL OR o.status = :status) AND " +
           "(:country IS NULL OR o.country = :country) AND " +
           "(:userId IS NULL OR u.id = :userId) AND " +
           "(:userEmail IS NULL OR u.email LIKE %:userEmail%) AND " +
           "(:startDate IS NULL OR o.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR o.createdAt <= :endDate) AND " +
           "(:shippingType IS NULL OR o.shippingType = :shippingType) AND " +
           "(:trackingNumber IS NULL OR o.trackingNumber LIKE %:trackingNumber%) " +
           "ORDER BY o.createdAt DESC",
           countQuery = "SELECT COUNT(o) FROM Order o JOIN o.user u WHERE " +
           "(:orderNumber IS NULL OR o.orderNumber LIKE %:orderNumber%) AND " +
           "(:recipientName IS NULL OR o.recipientName LIKE %:recipientName%) AND " +
           "(:status IS NULL OR o.status = :status) AND " +
           "(:country IS NULL OR o.country = :country) AND " +
           "(:userId IS NULL OR u.id = :userId) AND " +
           "(:userEmail IS NULL OR u.email LIKE %:userEmail%) AND " +
           "(:startDate IS NULL OR o.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR o.createdAt <= :endDate) AND " +
           "(:shippingType IS NULL OR o.shippingType = :shippingType) AND " +
           "(:trackingNumber IS NULL OR o.trackingNumber LIKE %:trackingNumber%)")
    Page<Order> searchOrders(@Param("orderNumber") String orderNumber,
                            @Param("recipientName") String recipientName,
                            @Param("status") Order.OrderStatus status,
                            @Param("country") String country,
                            @Param("userId") Long userId,
                            @Param("userEmail") String userEmail,
                            @Param("startDate") LocalDateTime startDate,
                            @Param("endDate") LocalDateTime endDate,
                            @Param("shippingType") Order.ShippingType shippingType,
                            @Param("trackingNumber") String trackingNumber,
                            Pageable pageable);

    // 전체 텍스트 검색
    @Query(value = "SELECT o FROM Order o JOIN FETCH o.user u WHERE " +
           "o.orderNumber LIKE %:keyword% OR " +
           "o.recipientName LIKE %:keyword% OR " +
           "o.recipientAddress LIKE %:keyword% OR " +
           "o.trackingNumber LIKE %:keyword% OR " +
           "u.email LIKE %:keyword% OR " +
           "u.name LIKE %:keyword% OR " +
           "o.specialRequests LIKE %:keyword% " +
           "ORDER BY o.createdAt DESC",
           countQuery = "SELECT COUNT(o) FROM Order o JOIN o.user u WHERE " +
           "o.orderNumber LIKE %:keyword% OR " +
           "o.recipientName LIKE %:keyword% OR " +
           "o.recipientAddress LIKE %:keyword% OR " +
           "o.trackingNumber LIKE %:keyword% OR " +
           "u.email LIKE %:keyword% OR " +
           "u.name LIKE %:keyword% OR " +
           "o.specialRequests LIKE %:keyword%")
    Page<Order> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // 상태별 통계 조회
    @Query("SELECT o.status, COUNT(o) FROM Order o WHERE " +
           "(:userId IS NULL OR o.user.id = :userId) AND " +
           "(:startDate IS NULL OR o.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR o.createdAt <= :endDate) " +
           "GROUP BY o.status")
    List<Object[]> getOrderStatusStats(@Param("userId") Long userId,
                                      @Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);

    // 국가별 주문 통계
    @Query("SELECT o.country, COUNT(o) FROM Order o WHERE " +
           "(:userId IS NULL OR o.user.id = :userId) AND " +
           "(:startDate IS NULL OR o.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR o.createdAt <= :endDate) " +
           "GROUP BY o.country ORDER BY COUNT(o) DESC")
    List<Object[]> getOrderCountryStats(@Param("userId") Long userId,
                                       @Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    // 배송 타입별 통계
    @Query("SELECT o.shippingType, COUNT(o) FROM Order o WHERE " +
           "(:userId IS NULL OR o.user.id = :userId) AND " +
           "(:startDate IS NULL OR o.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR o.createdAt <= :endDate) " +
           "GROUP BY o.shippingType")
    List<Object[]> getShippingTypeStats(@Param("userId") Long userId,
                                       @Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    // 최근 주문 (사용자별)
    @Query(value = "SELECT o FROM Order o JOIN FETCH o.user WHERE o.user.id = :userId ORDER BY o.createdAt DESC",
           countQuery = "SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId")
    Page<Order> findRecentOrdersByUserId(@Param("userId") Long userId, Pageable pageable);

    // 긴급 처리 필요한 주문들
    @Query(value = "SELECT o FROM Order o JOIN FETCH o.user WHERE " +
           "o.requiresExtraRecipient = true OR " +
           "o.noMemberCode = true OR " +
           "o.totalCbm > 29.0 " +
           "ORDER BY o.createdAt DESC",
           countQuery = "SELECT COUNT(o) FROM Order o WHERE " +
           "o.requiresExtraRecipient = true OR " +
           "o.noMemberCode = true OR " +
           "o.totalCbm > 29.0")
    Page<Order> findOrdersNeedingAttention(Pageable pageable);

    // CBM 범위로 검색
    @Query(value = "SELECT o FROM Order o JOIN FETCH o.user WHERE " +
           "(:minCbm IS NULL OR o.totalCbm >= :minCbm) AND " +
           "(:maxCbm IS NULL OR o.totalCbm <= :maxCbm) " +
           "ORDER BY o.totalCbm DESC",
           countQuery = "SELECT COUNT(o) FROM Order o WHERE " +
           "(:minCbm IS NULL OR o.totalCbm >= :minCbm) AND " +
           "(:maxCbm IS NULL OR o.totalCbm <= :maxCbm)")
    Page<Order> findOrdersByCbmRange(@Param("minCbm") Double minCbm,
                                    @Param("maxCbm") Double maxCbm,
                                    Pageable pageable);
    
    // 사용자별 주문 조회 (OrderService용)
    @Query("SELECT o FROM Order o JOIN FETCH o.user WHERE o.user.id = :userId ORDER BY o.createdAt DESC")
    List<Order> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
    
    // 사용자별, 상태별 주문 조회 (DashboardService용)
    @Query(value = "SELECT o FROM Order o JOIN FETCH o.user WHERE o.user.id = :userId AND o.status = :status ORDER BY o.createdAt DESC",
           countQuery = "SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId AND o.status = :status")
    Page<Order> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Order.OrderStatus status, Pageable pageable);
    
    // 사용자별 전체 주문 조회 (DashboardService용)
    @Query(value = "SELECT o FROM Order o JOIN FETCH o.user WHERE o.user.id = :userId ORDER BY o.createdAt DESC",
           countQuery = "SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId")
    Page<Order> findByUserId(@Param("userId") Long userId, Pageable pageable);
    
    // Admin order management methods
    @EntityGraph(attributePaths = {"user"})
    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    @EntityGraph(attributePaths = {"user"})
    Page<Order> findByStatusOrderByCreatedAtDesc(Order.OrderStatus status, Pageable pageable);
    
    @EntityGraph(attributePaths = {"user"})
    Page<Order> findByOrderNumberContainingIgnoreCaseOrderByCreatedAtDesc(String orderNumber, Pageable pageable);
    
    @EntityGraph(attributePaths = {"user"})
    Page<Order> findByRecipientNameContainingIgnoreCaseOrderByCreatedAtDesc(String recipientName, Pageable pageable);
    
    @EntityGraph(attributePaths = {"user"})
    Page<Order> findByOrderNumberContainingIgnoreCaseOrRecipientNameContainingIgnoreCaseOrderByCreatedAtDesc(
        String orderNumber, String recipientName, Pageable pageable);
    
    // Status count methods for admin statistics
    Long countByStatus(Order.OrderStatus status);
    
    // 전체 주문 조회 (날짜순)
    @EntityGraph(attributePaths = {"user", "items"})
    List<Order> findAllByOrderByCreatedAtDesc();
    
    // WarehouseService 메소드들
    List<Order> findByArrivedAtIsNullAndStatusNot(Order.OrderStatus status);
    List<Order> findByArrivedAtIsNotNullAndShippedAtIsNullAndStatusNot(Order.OrderStatus status);
    List<Order> findByCreatedAtAfter(LocalDateTime startTime);
}