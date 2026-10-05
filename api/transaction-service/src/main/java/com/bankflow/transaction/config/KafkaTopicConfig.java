package com.bankflow.transaction.config;

import com.bankflow.shared.events.Topics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public KafkaAdmin.NewTopics kafkaTopics(@Value("${bankflow.kafka.partitions:3}") int partitions,
                                            @Value("${bankflow.kafka.replicas:1}") int replicas) {
        return new KafkaAdmin.NewTopics(
                topic(Topics.TRANSACTION_CREATED, partitions, replicas),
                topic(Topics.TRANSACTION_APPROVED, partitions, replicas),
                topic(Topics.TRANSACTION_DECLINED, partitions, replicas),
                topic(Topics.TRANSACTION_APPROVED + Topics.DLT_SUFFIX, partitions, replicas),
                topic(Topics.TRANSACTION_DECLINED + Topics.DLT_SUFFIX, partitions, replicas)
        );
    }

    private static NewTopic topic(String name, int partitions, int replicas) {
        return TopicBuilder.name(name).partitions(partitions).replicas(replicas).build();
    }
}