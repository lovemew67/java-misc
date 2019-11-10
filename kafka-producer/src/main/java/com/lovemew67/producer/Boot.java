package com.lovemew67.producer;

import com.lovemew67.customEvent;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.*;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Boot {

  private static final Executor executor = Executors.newFixedThreadPool(100);

  public static void main(String[] args) throws InterruptedException {
    final Config config = ConfigFactory.load();
    List configList = config.getConfigList("pubsub.kafka.producer.topics");
    Iterator<Config> confItr = configList.iterator();
    while (confItr.hasNext()) {
      Config currentConf = confItr.next();
      executor.execute(new produce(currentConf));
    }
  }

  public static KafkaProducer<String, byte[]> createProducer(Config config) {
    final Properties props = new Properties();
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, config.getString("key.serializer"));
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, config.getString("value.serializer"));
    props.put(ProducerConfig.ACKS_CONFIG, "1");
    final String brokers = config.getString("bootstrap.servers");
    if (brokers != null) {
      props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
    }
    return new KafkaProducer<>(props);
  }

  public static class produce implements Runnable {
    private Config config;

    public produce(Config config) {
      this.config = config;
    }

    public void run() {
      String topic = config.getString("topic");
      String broker = config.getString("bootstrap.servers");

      System.out.println();
      System.out.println("Start to produce topic: [" + topic + "] to broker: [" + broker + "]");

      try {
        KafkaProducer producer = createProducer(config);
        ProducerRecord<String, byte[]> producerRecord = null;

        // while (true) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        try {
          customEvent value = new customEvent();
          value.setRequestId(broker + "-" + topic + "-" + System.currentTimeMillis());

          out = new ObjectOutputStream(bos);
          out.writeObject(value);
          out.flush();
          byte[] bytes = bos.toByteArray();

          producerRecord = new ProducerRecord<>(topic, null, bytes);
          producer.send(producerRecord);

          Thread.sleep(2500);
        } finally {
          try {
            bos.close();
          } catch (IOException ex) {}
        }
        // }

      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }
}
