package com.ycs.authbackend.repository;

import com.ycs.authbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByMemberCode(String memberCode);
    boolean existsByEmail(String email);
}