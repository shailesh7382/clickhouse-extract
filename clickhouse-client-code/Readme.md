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

In ClickHouse, the MergeTree engine requires an ORDER BY clause, which defines the primary key for the table. This primary key is used for indexing and organizing the data. The ORDER BY clause in your CREATE TABLE statement already specifies timestamp as the primary key.

However, depending on your query patterns and performance requirements, you might want to consider adding additional columns to the ORDER BY clause to optimize query performance. 