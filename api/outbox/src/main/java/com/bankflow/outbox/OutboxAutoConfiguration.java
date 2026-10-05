package com.bankflow.outbox;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.json.JsonMapper;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Picked up automatically by every service that depends on this module
 * (META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports).
 * The service only provides the outbox table (Flyway) and calls {@link OutboxWriter}.
 */
@AutoConfiguration
@EnableScheduling
public class OutboxAutoConfiguration {

    @Bean
    public OutboxWriter outboxWriter(DataSource dataSource, JsonMapper jsonMapper) {
        return new OutboxWriter(new JdbcTemplate(dataSource), jsonMapper);
    }

    @Bean
    public OutboxRelay outboxRelay(DataSource dataSource,
                                   PlatformTransactionManager transactionManager,
                                   @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
                                   @Value("${bankflow.outbox.batch-size:50}") int batchSize,
                                   @Value("${bankflow.outbox.retention-days:7}") int retentionDays) {

        KafkaTemplate<String, String> kafkaTemplate = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(
                Map.of(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers),
                new StringSerializer(),
                new StringSerializer()));

        return new OutboxRelay(
                new JdbcTemplate(dataSource),
                new TransactionTemplate(transactionManager),
                kafkaTemplate,
                batchSize,
                retentionDays);
    }
}