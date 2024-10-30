#!/bin/bash

# Path to ClickHouse binary
CLICKHOUSE_BINARY="/usr/local/bin/clickhouse"

# Path to ClickHouse configuration file
CONFIG_FILE="/Users/shailesh/IdeaProjects/clickhouse-extract/system-config/system-config.xml"

# Check if ClickHouse is already running
CLICKHOUSE_PID=$(pgrep -x "clickhouse")
if [ -n "$CLICKHOUSE_PID" ]; then
    echo "ClickHouse server is already running. PID: $CLICKHOUSE_PID"
    exit 0
fi

# Path to ClickHouse log files
LOG_DIR="/Users/shailesh/IdeaProjects/clickhouse-extract/log"
LOG_FILE="$LOG_DIR/clickhouse-server.log"
ERROR_LOG_FILE="$LOG_DIR/clickhouse-server.err.log"

# Ensure the log directory exists
mkdir -p $LOG_DIR

# Start ClickHouse server and redirect logs
$CLICKHOUSE_BINARY server --config-file=$CONFIG_FILE > $LOG_FILE 2> $ERROR_LOG_FILE &

# Capture the PID of the ClickHouse server process
CLICKHOUSE_PID=$!

# Check if the server started successfully
if [ $? -eq 0 ]; then
    echo "ClickHouse server started successfully. PID: $CLICKHOUSE_PID"
    echo "Logs can be found at:"
    echo "Standard Log: $LOG_FILE"
    echo "Error Log: $ERROR_LOG_FILE"
else
    echo "Failed to start ClickHouse server. Check logs for details:"
    echo "Standard Log: $LOG_FILE"
    echo "Error Log: $ERROR_LOG_FILE"
    exit 1
fi