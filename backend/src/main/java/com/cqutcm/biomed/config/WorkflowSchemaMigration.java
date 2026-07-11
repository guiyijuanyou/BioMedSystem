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
                new ClassPathResource("db/migration/V4__professional_improvement_loop.sql"));
        migration.setContinueOnError(false);
        migration.execute(dataSource);
    }
}
