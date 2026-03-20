// package com.khai.em.config;

// import java.time.LocalDateTime;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
// import org.springframework.scheduling.annotation.Scheduled;
// import org.springframework.stereotype.Component;

// import com.khai.em.repository.PasswordResetOtpRepository;

// @Component
// @ConditionalOnProperty(prefix = "app.otp.cleanup", name = "enabled", havingValue = "true", matchIfMissing = true)
// public class PasswordResetOtpCleanupJob {

//     private static final Logger log = LoggerFactory.getLogger(PasswordResetOtpCleanupJob.class);

//     private final PasswordResetOtpRepository passwordResetOtpRepository;

//     public PasswordResetOtpCleanupJob(PasswordResetOtpRepository passwordResetOtpRepository) {
//         this.passwordResetOtpRepository = passwordResetOtpRepository;
//     }

//     @Scheduled(
//             fixedDelayString = "${app.otp.cleanup.fixedDelayMs:600000}",
//             initialDelayString = "${app.otp.cleanup.initialDelayMs:60000}"
//     )
//     public void cleanupExpiredOtps() {
//         long deleted = passwordResetOtpRepository.deleteByExpiresAtBefore(LocalDateTime.now());
//         if (deleted > 0) {
//             log.info("Deleted {} expired password reset OTP(s)", deleted);
//         }
//     }
// }
