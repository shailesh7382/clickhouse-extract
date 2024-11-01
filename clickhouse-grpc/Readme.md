# Introduction

ClickHouse integrates with gRPC by providing a gRPC server that can handle remote procedure calls. 
This allows clients to interact with ClickHouse using gRPC, which can be beneficial for performance and interoperability with other systems.

- To enable gRPC in ClickHouse, you need to configure the grpc section in the ClickHouse configuration file (config/system-config.xml)

```sh
curl -o src/main/proto/clickhouse_grpc.proto https://raw.githubusercontent.com/ClickHouse/ClickHouse/master/grpc/proto/clickhouse_grpc.proto
```

Have made several changes to the Client.Builder configuration in the Stream2DbWriter class. 
Here is a brief explanation of the changes:
- Compression: Enabled compression for both server responses and client requests.
- Connection Pool: Enabled connection pooling and set the maximum number of connections.
- Timeout: Set the connection request timeout to 30 seconds.