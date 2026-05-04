package com.khai.em.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.khai.em.entity.UserDevice;

@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {
    boolean existsByUserIdAndIpAddressAndTrustedTrue(Long userId, String ipAddress);

    Optional<UserDevice> findByUserIdAndIpAddress(Long userId, String ipAddress);
} 