package com.khai.em.repository;

import com.khai.em.entity.PasswordResetOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtp, Long> {

    Optional<PasswordResetOtp> findTopByUser_IdAndConsumedAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(
            Long userId,
            LocalDateTime now);

    long countByUser_IdAndCreatedAtAfter(Long userId, LocalDateTime since);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update PasswordResetOtp o set o.consumedAt = :consumedAt where o.id = :id and o.consumedAt is null")
    int markConsumed(@Param("id") Long id, @Param("consumedAt") LocalDateTime consumedAt);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update PasswordResetOtp o set o.consumedAt = :consumedAt where o.user.id = :userId and o.consumedAt is null")
    int markAllActiveConsumedForUser(@Param("userId") Long userId, @Param("consumedAt") LocalDateTime consumedAt);

    long deleteByUser_Id(Long userId);

    long deleteByUser_IdAndExpiresAtBefore(Long userId, LocalDateTime now);

    long deleteByExpiresAtBefore(LocalDateTime now);
} 
