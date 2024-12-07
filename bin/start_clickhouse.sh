#!/bin/bash

# Default paths
DEFAULT_CLICKHOUSE_BINARY="$HOME/bin/clickhouse"
DEFAULT_CONFIG_FILE="../system-config/system-config.xml"
DEFAULT_LOG_DIR="../log"

# Function to print usage
usage() {
    echo "Usage: $0 {start|stop|status|restart} [-b clickhouse_binary] [-c config_file] [-l log_dir]"
    exit 1
}

# Function to rotate logs
rotate_logs() {
    local log_file=$1
    local error_log_file=$2
    if [ -f "$log_file" ]; then
        mv "$log_file" "$log_file.$(date +%Y%m%d%H%M%S)"
    fi
    if [ -f "$error_log_file" ]; then
        mv "$error_log_file" "$error_log_file.$(date +%Y%m%d%H%M%S)"
    fi
}

# Function to check if ClickHouse is running
is_clickhouse_running() {
    if command -v pgrep > /dev/null; then
        pgrep -x "clickhouse" > /dev/null
    else
        ps aux | grep -v grep | grep "clickhouse server" > /dev/null
    fi
}

# Function to start ClickHouse server
start_server() {
    local clickhouse_binary=$1
    local config_file=$2
    local log_dir=$3
    local log_file="$log_dir/clickhouse-server.log"
    local error_log_file="$log_dir/clickhouse-server.err.log"

    # Check if ClickHouse is already running
    if is_clickhouse_running; then
        echo "ClickHouse server is already running."
        exit 0
    fi

    # Ensure the log directory exists
    mkdir -p $log_dir

    # Rotate logs
    rotate_logs $log_file $error_log_file

    # Start ClickHouse server and redirect both stdout and stderr to the same log file
    $clickhouse_binary server --config-file=$config_file > $log_file 2> $error_log_file &

    # Capture the PID of the ClickHouse server process
    clickhouse_pid=$!

    # Check if the server started successfully
    if [ $? -eq 0 ]; then
        echo "ClickHouse server started successfully. PID: $clickhouse_pid"
        echo "Logs can be found at:"
        echo "Standard Log: $log_file"
        echo "Error Log: $error_log_file"
    else
        echo "Failed to start ClickHouse server. Check logs for details:"
        echo "Standard Log: $log_file"
        echo "Error Log: $error_log_file"
        exit 1
    fi
}

# Function to stop ClickHouse server
stop_server() {
    if is_clickhouse_running; then
        pkill -x "clickhouse"
        echo "ClickHouse server stopped."
    else
        echo "ClickHouse server is not running."
    fi
}

# Function to check ClickHouse server status
check_status() {
    if is_clickhouse_running; then
        echo "ClickHouse server is running."
    else
        echo "ClickHouse server is not running."
    fi
}

# Function to restart ClickHouse server
restart_server() {
    stop_server
    start_server $CLICKHOUSE_BINARY $CONFIG_FILE $LOG_DIR
}

# Parse command line arguments
ACTION=$1
shift

CLICKHOUSE_BINARY=$DEFAULT_CLICKHOUSE_BINARY
CONFIG_FILE=$DEFAULT_CONFIG_FILE
LOG_DIR=$DEFAULT_LOG_DIR

while getopts "b:c:l:" opt; do
    case $opt in
        b) CLICKHOUSE_BINARY=$OPTARG ;;
        c) CONFIG_FILE=$OPTARG ;;
        l) LOG_DIR=$OPTARG ;;
        *) usage ;;
    esac
done

# Execute the specified action
case $ACTION in
    start) start_server $CLICKHOUSE_BINARY $CONFIG_FILE $LOG_DIR ;;
    stop) stop_server ;;
    status) check_status ;;
    restart) restart_server ;;
    *) usage ;;
esac