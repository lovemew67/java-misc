package com.lovemew67.consumer;

import com.lovemew67.customEvent;
import com.lovemew67.consumer.api.action;
import com.lovemew67.consumer.api.request;
import com.lovemew67.consumer.event.event;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Boot {

  private static final Executor executor = Executors.newFixedThreadPool(100);

  public static void main(String[] args) {
    final Config config = ConfigFactory.load();

    /// env var test
    String url = config.getString("local.config.url");
    System.out.println(url);

    /// enum test
    System.out.println();
    System.out.println(action.TURN_LEFT);
    System.out.println(action.TURN_RIGHT);
    System.out.println(action.SHOOT);

    /// sub-proj test
    event kafkaEvent = new event();
    kafkaEvent.setRequestId("req-id");
    kafkaEvent.setResponseId("res-id");
    System.out.println();
    System.out.println(kafkaEvent);

    List configList = config.getConfigList("pubsub.kafka.consumer.topics");
    Iterator<Config> confItr = configList.iterator();
    while (confItr.hasNext()) {
      Config currentConf = confItr.next();
      executor.execute(new consume(currentConf));
    }
  }

  private static KafkaConsumer<String, byte[]> createConsumer(Config config) {
    final Properties props = new Properties();
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, config.getString("key.deserializer"));
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, config.getString("value.deserializer"));
    props.put("group.id", "consumer-group");
    final String brokers = config.getString("bootstrap.servers");
    if (brokers != null) {
      props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
    }
    return new KafkaConsumer<>(props);
  }

  private static class consume<T> implements Runnable {
    private Config config;

    private consume(Config config) {
      this.config = config;
    }

    public void run() {
      String topic = config.getString("topic");
      String broker = config.getString("bootstrap.servers");

      System.out.println();
      System.out.println("Start to consume topic: [" + topic + "] to broker: [" + broker + "]");

      try {
        KafkaConsumer consumer = createConsumer(config);
        Collection<String> topics = Arrays.asList(topic);
        consumer.subscribe(topics);

        while (true) {
          ConsumerRecords<String, byte[]> consumerRecords = consumer.poll(1000);
          for (ConsumerRecord<String, byte[]> consumerRecord : consumerRecords) {
            long offset = consumerRecord.offset();
            int partition = consumerRecord.partition();
            String key = consumerRecord.key();
            byte[] value = consumerRecord.value();

            ByteArrayInputStream bis = new ByteArrayInputStream(value);
            ObjectInput in = null;
            try {
              in = new ObjectInputStream(bis);
              Object o = in.readObject(); 
              customEvent ce = (customEvent) o;

              System.out.format(">> broker:[%s] topic:[%s] partition:[%d] offset:[%d] key:[%s] value:\n%s\n", broker, topic, partition, offset, key, ce);
            } finally {
              try {
                if (in != null) {
                  in.close();
                }
              } catch (IOException ex) {}
            }
          }
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }
}
