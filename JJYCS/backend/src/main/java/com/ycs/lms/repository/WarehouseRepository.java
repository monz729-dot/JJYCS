package com.ycs.lms.repository;

import com.ycs.lms.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    
    /**
     * 창고명으로 조회
     */
    Optional<Warehouse> findByName(String name);
    
    /**
     * 창고 코드로 조회
     */
    Optional<Warehouse> findByWarehouseCode(String warehouseCode);
    
    /**
     * 활성 상태 창고 목록
     */
    List<Warehouse> findByActiveTrue();
    
    /**
     * 국가별 창고 목록
     */
    List<Warehouse> findByCountry(String country);
    
    /**
     * 도시별 창고 목록
     */
    List<Warehouse> findByCity(String city);
    
    /**
     * 국가와 활성 상태로 창고 조회
     */
    List<Warehouse> findByCountryAndActiveTrue(String country);
    
    /**
     * 창고 타입별 조회
     */
    @Query("SELECT w FROM Warehouse w WHERE w.warehouseType = :type AND w.active = true")
    List<Warehouse> findByTypeAndActive(@Param("type") Warehouse.WarehouseType type);
    
    /**
     * 특정 지역 반경 내 창고 검색 (향후 GPS 기능용)
     */
    @Query("SELECT w FROM Warehouse w WHERE w.active = true ORDER BY w.name")
    List<Warehouse> findAllActiveOrderByName();
    
    /**
     * 용량별 창고 검색
     */
    @Query("SELECT w FROM Warehouse w WHERE w.maxCapacityCbm >= :requiredCapacity AND w.active = true")
    List<Warehouse> findByMinCapacity(@Param("requiredCapacity") Double requiredCapacity);
    
    /**
     * 창고 코드 중복 확인
     */
    boolean existsByWarehouseCode(String warehouseCode);
    
    /**
     * 창고명 중복 확인
     */
    boolean existsByName(String name);
}