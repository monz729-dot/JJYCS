package com.ycs.lms.repository;

import com.ycs.lms.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    
    List<BankAccount> findByIsActiveTrue();
    
    @Query("SELECT b FROM BankAccount b WHERE b.isActive = true ORDER BY b.displayOrder ASC, b.id ASC")
    List<BankAccount> findActiveAccountsOrderByDisplayOrder();
    
    Optional<BankAccount> findByIsDefaultTrue();
}