package com.anddev741.BitData.e2e;

import com.anddev741.BitData.domain.model.UnconfirmedTransaction.UnconfirmedTransaction;
import com.anddev741.BitData.infrastructure.web.BlockchainWebSocketConnector;
import com.anddev741.BitData.utils.ValidJson;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binding.BindingService;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
public class UnconfirmedTransactionE2eTest {

    String validJson = ValidJson.getValidJson();

    @Autowired
    private BlockchainWebSocketConnector webSocket;

    @Autowired
    BindingService bindingService;

    @Container
    static KafkaContainer kafka = new KafkaContainer("apache/kafka-native:3.8.0");

    @AfterEach
    void stopBindings() {
       webSocket.close();
    }

    @DynamicPropertySource
    static void setupProps(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.stream.kafka.binder.brokers", kafka::getBootstrapServers);

        registry.add("spring.cloud.stream.kafka.binder.autoCreateTopics", () -> true);
    }

    @Test
    void shouldSendMessageToKafkaWhenWebSocketReceivesData() throws Exception {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList("processTransactions"));

        ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(5));

        //Received transactions
        assertThat(records.count()).isGreaterThan(0);

        String json = records.iterator().next().value();
        UnconfirmedTransaction tx = new ObjectMapper().readValue(json, UnconfirmedTransaction.class);

        //Transactions are valid
        assertThat(tx.getX().getHash()).isNotEmpty();

        consumer.close();
    }
}
