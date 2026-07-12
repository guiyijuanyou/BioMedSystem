package com.cqutcm.biomed.config;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class WorkflowSchemaMigration implements org.springframework.boot.CommandLineRunner {
    private final DataSource dataSource;

    public WorkflowSchemaMigration(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) {
        ResourceDatabasePopulator migration = new ResourceDatabasePopulator(
                new ClassPathResource("db/migration/V2__workflow_review_and_version.sql"),
                new ClassPathResource("db/migration/V3__herb_batch_and_lab_sample.sql"),
                new ClassPathResource("db/migration/V4__professional_improvement_loop.sql"),
                new ClassPathResource("db/migration/V5_migration_profile.sql"),
                new ClassPathResource("db/migration/V5__quality_metric_and_auto_analysis.sql"),
                new ClassPathResource("db/migration/V6__multi_metric_evaluation.sql"),
                new ClassPathResource("db/migration/V7__improvement_recommendation.sql"),
                new ClassPathResource("db/migration/V8__achievement_quantification.sql"),
                new ClassPathResource("db/migration/V9__mobile_collection_device.sql"));
        migration.setContinueOnError(false);
        migration.execute(dataSource);
    }
}
