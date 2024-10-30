#!/bin/bash

# Check if ClickHouse is running
CLICKHOUSE_PID=$(pgrep -x "clickhouse")
if [ -z "$CLICKHOUSE_PID" ]; then
    echo "ClickHouse server is not running."
    exit 0
fi

# Attempt to gracefully stop ClickHouse
kill -TERM $CLICKHOUSE_PID

# Wait for the process to terminate
WAIT_TIME=30
while [ $WAIT_TIME -gt 0 ]; do
    if ! pgrep -x "clickhouse" > /dev/null; then
        echo "ClickHouse server stopped successfully."
        exit 0
    fi
    sleep 1
    WAIT_TIME=$((WAIT_TIME - 1))
done

# If the process did not terminate, force kill it
echo "ClickHouse server did not stop gracefully, forcing shutdown."
kill -KILL $CLICKHOUSE_PID

# Verify if the process was killed
if pgrep -x "clickhouse" > /dev/null; then
    echo "Failed to stop ClickHouse server."
    exit 1
else
    echo "ClickHouse server stopped forcefully."
    exit 0
fi