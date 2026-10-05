package com.bankflow.ledger.messaging;

import com.bankflow.ledger.AbstractIntegrationTest;
import com.bankflow.shared.events.Topics;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.TopicDescription;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaTopicsTest extends AbstractIntegrationTest {

    @Test
    void declaredTopicsExistWithThreePartitions() throws Exception {
        List<String> expected = List.of(
                Topics.TRANSACTION_APPROVED,
                Topics.TRANSACTION_DECLINED,
                Topics.TRANSACTION_CREATED,
                Topics.ACCOUNT_CREATED,
                Topics.TRANSACTION_CREATED + Topics.DLT_SUFFIX,
                Topics.ACCOUNT_CREATED + Topics.DLT_SUFFIX);

        try (AdminClient admin = AdminClient.create(
                Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers()))) {

            Map<String, TopicDescription> topics =
                    admin.describeTopics(expected).allTopicNames().get(10, TimeUnit.SECONDS);

            assertThat(topics).containsOnlyKeys(expected);
            assertThat(topics.values()).allSatisfy(topic -> assertThat(topic.partitions()).hasSize(3));
        }
    }
}