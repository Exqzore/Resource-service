package com.exqzore.resourceservice.config;

import lombok.AllArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Map;
import java.util.stream.Stream;

@Configuration
@AllArgsConstructor
public class KafkaTopicConfig {
  private final KafkaAdmin kafkaAdmin;

  @PostConstruct
  public void createTopics() {
    NewTopic[] topics =
        Stream.of("topic")
            .map(
                topic ->
                    new NewTopic(topic, 1, (short) 1)
                        .configs(
                            Map.of(
                                TopicConfig.RETENTION_MS_CONFIG,
                                String.valueOf(Duration.ofMinutes(15).toMillis()))))
            .toArray(NewTopic[]::new);
    kafkaAdmin.createOrModifyTopics(topics);
  }
}
