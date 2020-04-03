package com.ma.canal.demo.kafkademo;

import com.alibaba.otter.canal.client.kafka.KafkaCanalConnector;
import com.alibaba.otter.canal.protocol.FlatMessage;
import com.ma.canal.demo.util.PropUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Kafka client example
 *
 * @author machengyuan @ 2018-6-12
 * @version 1.0.0
 */
public class CanalKafkaClientExample {

    protected final static Logger logger  = LoggerFactory.getLogger(CanalKafkaClientExample.class);

    private KafkaCanalConnector connector;

    private static volatile boolean         running = false;

    private Thread                          thread  = null;

    private Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler() {

                                                        public void uncaughtException(Thread t, Throwable e) {
                                                            logger.error("parse events has an error", e);
                                                        }
                                                    };

    public CanalKafkaClientExample(String zkServers, String servers, String topic, Integer partition, String groupId){
        connector = new KafkaCanalConnector(servers, topic, partition, groupId, null, true);
    }

    public static void main(String[] args) {
        try {

            PropUtil.getProp("config.properties");
            final CanalKafkaClientExample kafkaCanalClientExample = new CanalKafkaClientExample(
                    "",
                "172.25.61.82:9092,172.25.61.87:9092,172.25.61.93:9092",
                "example",
                null,
                "g4");
            logger.info("## start the kafka consumer: {}-{}", "example", "g4");
            kafkaCanalClientExample.start();
            logger.info("## the canal kafka consumer is running now ......");
            Runtime.getRuntime().addShutdownHook(new Thread() {

                public void run() {
                    try {
                        logger.info("## stop the kafka consumer");
                        kafkaCanalClientExample.stop();
                    } catch (Throwable e) {
                        logger.warn("##something goes wrong when stopping kafka consumer:", e);
                    } finally {
                        logger.info("## kafka consumer is down.");
                    }
                }

            });
            while (running)
                ;
        } catch (Throwable e) {
            logger.error("## Something goes wrong when starting up the kafka consumer:", e);
            System.exit(0);
        }
    }

    public void start() {
        Assert.notNull(connector, "connector is null");
        thread = new Thread(new Runnable() {

            public void run() {
                process();
            }
        });
        thread.setUncaughtExceptionHandler(handler);
        thread.start();
        running = true;
    }

    public void stop() {
        if (!running) {
            return;
        }
        running = false;
        if (thread != null) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }

    private void process() {
        while (!running) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }

        while (running) {
            try {
                connector.connect();
                connector.subscribe();
                while (running) {
                    try {
                        List< FlatMessage > messages = connector. getFlatListWithoutAck(100L, TimeUnit.MILLISECONDS); // 获取message
                        if (messages == null||messages.size() == 0) {
                            continue;
                        }
//                        for (Message message : messages) {
//                            long batchId = message.getId();
//                            int size = message.getEntries().size();
//                            if (batchId == -1 || size == 0) {
//                                // try {
//                                // Thread.sleep(1000);
//                                // } catch (InterruptedException e) {
//                                // }
//                            } else {
//                                // printSummary(message, batchId, size);
//                                // printEntry(message.getEntries());
//                                logger.info(message.toString());
//                            }
//                        }
                        for (FlatMessage flatMessage : messages) {
                            long batchId = flatMessage.getId();
//                            int size = flatMessage.get.getEntries().size();
//                            if (batchId == -1 || size == 0) {
//                                // try {
//                                // Thread.sleep(1000);
//                                // } catch (InterruptedException e) {
//                                // }
//                            } else {
//                                // printSummary(message, batchId, size);
//                                // printEntry(message.getEntries());
//                                logger.info(message.toString());
//                            }
                            logger.info(flatMessage.toString());
                        }

                        connector.ack(); // 提交确认
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        connector.unsubscribe();
        connector.disconnect();
    }
}
