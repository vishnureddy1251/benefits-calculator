package com.paylocity.benefits_calculator.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Configuration class to enable JPA Auditing.
 *
 * This enables automatic population of @CreatedDate and @LastModifiedDate fields
 * in entities that extend BaseEntity.
 *
 * @author Benefits Calculator Team
 * @version 1.0
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
    // JPA Auditing is enabled through the @EnableJpaAuditing annotation
    // No additional configuration needed
}