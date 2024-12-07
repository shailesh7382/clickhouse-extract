package com.example;

import com.example.marketdata.MarketData;
import com.example.marketdata.MarketDataGenerator;
import com.example.marketdata.MarketDataUpdateListener;
import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueue;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChronicleQueueWriter implements MarketDataUpdateListener, AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(ChronicleQueueWriter.class);
    private @NotNull SingleChronicleQueue queue;
    private @NotNull ExcerptAppender appender;

    public static void main(String[] args) throws Exception {

        try (final ChronicleQueueWriter writer = ChronicleQueueWriter.createQueue()) {
            MarketDataGenerator generator = new MarketDataGenerator(writer);
            generator.generateMarketData(1000);
        }
    }

    static ChronicleQueueWriter createQueue() {
        String queuePath = System.getenv("QUEUE_PATH");
        if (queuePath == null) {
            queuePath = "path/to/queue";
        }
        ChronicleQueueWriter writer = new ChronicleQueueWriter();
        writer.queue = SingleChronicleQueueBuilder.binary(queuePath).build();
        writer.appender = writer.queue.acquireAppender();
        return writer;
    }


    @Override
    public void onMarketDataUpdate(MarketData marketData) {
            log.info("md={}",marketData);
            appender.writeDocument(w -> w.write("marketData").typedMarshallable(marketData));
    }

    @Override
    public void close() throws Exception {
        queue.close();
    }
}