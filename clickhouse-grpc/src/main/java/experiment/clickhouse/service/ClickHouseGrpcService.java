package experiment.clickhouse.service;

import clickhouse.grpc.ClickHouseServiceGrpc;
import clickhouse.grpc.ClickhouseGrpc;
import experiment.clickhouse.utility.PortChecker;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Service
public class ClickHouseGrpcService {

    private static final Logger logger = LoggerFactory.getLogger(ClickHouseGrpcService.class);

    private ManagedChannel channel;
    private ClickHouseServiceGrpc.ClickHouseServiceBlockingStub blockingStub;

    @PostConstruct
    public void init() {
        logger.info("Initializing gRPC channel and stub is open -> {} ", PortChecker.isPortOpen("localhost", 9100));
        channel = ManagedChannelBuilder.forAddress("localhost", 9100)
                                       .usePlaintext()
                                       .build();
        blockingStub = ClickHouseServiceGrpc.newBlockingStub(channel);
        logger.info("gRPC channel and stub initialized");
    }

    @PreDestroy
    public void shutdown() {
        if (channel != null) {
            logger.info("Shutting down gRPC channel");
            channel.shutdown();
            logger.info("gRPC channel shut down");
        }
    }

    public String getStatus() {
        logger.info("Executing getStatus query");
        ClickhouseGrpc.QueryInfo request = ClickhouseGrpc.QueryInfo.newBuilder().setQuery("SELECT 1").build();
        ClickhouseGrpc.Result response = blockingStub.executeQuery(request);
        String result = response.getOutput().toStringUtf8();
        logger.info("Query executed successfully, result: {}", result);
        return result;
    }
}