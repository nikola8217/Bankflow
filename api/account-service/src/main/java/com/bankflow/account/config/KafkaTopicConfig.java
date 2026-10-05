package com.bankflow.account.config;

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
                topic(Topics.ACCOUNT_CREATED, partitions, replicas)
        );
    }

    private static NewTopic topic(String name, int partitions, int replicas) {
        return TopicBuilder.name(name).partitions(partitions).replicas(replicas).build();
    }
}