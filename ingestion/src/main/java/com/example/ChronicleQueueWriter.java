package com.example;

import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ChronicleQueueWriter {
    private static final Logger log = LoggerFactory.getLogger(ChronicleQueueWriter.class);

    public static void main(String[] args) {
        String queuePath = System.getenv("QUEUE_PATH");
        if (queuePath == null) {
            queuePath = "path/to/queue";
        }

        List<String> ccyPairs = Arrays.asList("EURUSD", "GBPUSD", "USDJPY");
        LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        Random random = new Random();

        // Set up Chronicle Queue
        try (ChronicleQueue queue = ChronicleQueue.singleBuilder(queuePath).build()) {
            ExcerptAppender appender = queue.acquireAppender();

            // Write market data to Chronicle Queue
            for (String ccyPair : ccyPairs) {
                LocalDateTime currentDate = startDate;
                for (int i = 0; i < 1; i++) {
                    currentDate = currentDate.plusSeconds(random.nextInt(60)); // Increment by random seconds

                    MarketData md = new MarketData(
                            currentDate,
                            ccyPair,
                            Arrays.asList(1.1000 + i * 0.0001, 1.0995 + i * 0.0001),
                            Arrays.asList(1.1005 + i * 0.0001, 1.1010 + i * 0.0001),
                            Arrays.asList(1000.0 + i * 10, 2000.0 + i * 10),
                            Arrays.asList(0.0001 * i, 0.0002 * i),
                            Arrays.asList(0.0003 * i, 0.0004 * i),
                            "quoteReq_" + i,
                            "quote_" + i,
                            "LP_" + i,
                            "status_" + i,
                            "tenor_" + i
                    );

                    appender.writeMessage("marketData", md);
                }
            }

            log.info("Market data written to Chronicle Queue successfully.");

        } catch (Exception e) {
            log.error("Failed to write to Chronicle Queue", e);
        }
    }
}