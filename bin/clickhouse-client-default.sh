#!/bin/bash

# Path to ClickHouse client binary
CLICKHOUSE_CLIENT_BINARY="/usr/local/bin/clickhouse"

# ClickHouse server host and port
CLICKHOUSE_HOST="localhost"
CLICKHOUSE_PORT="9000"

# Optional: ClickHouse user and password
CLICKHOUSE_USER="shailesh"
CLICKHOUSE_PASSWORD="password123"

# Run ClickHouse client in multiline mode
$CLICKHOUSE_CLIENT_BINARY client --host=$CLICKHOUSE_HOST --port=$CLICKHOUSE_PORT --user=$CLICKHOUSE_USER --password=$CLICKHOUSE_PASSWORD --multiline