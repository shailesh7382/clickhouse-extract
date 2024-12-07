## To ingest data into ClickHouse for a financial system, you should consider the following best practices:  
- Schema Design: Design your schema to optimize for read performance. Use appropriate data types and consider partitioning and indexing strategies.  
- Batch Ingestion: Use batch ingestion to reduce the overhead of individual inserts. ClickHouse performs better with bulk inserts.  
- Data Compression: Enable data compression to save storage space and improve query performance.  
- Data Deduplication: Ensure data deduplication to avoid redundant data, which can affect performance and storage.  
- Monitoring and Alerts: Set up monitoring and alerts to track the performance and health of your ClickHouse instance.


## ClickHouse Client Libraries
The clickhouse-client-v2 library is a modern and efficient client for interacting with ClickHouse. 
It supports various protocols, including HTTP and native, and is designed for high-performance data ingestion and querying.



# Ingestion Project

## Overview

The Ingestion Project is a Java-based application designed to read market data from a Chronicle Queue and ingest it into a ClickHouse database. The project uses Maven for dependency management and builds, and it leverages SLF4J with Logback for logging.

## Features

- **Market Data Generation**: Generates market data with varying decimal places for different currency pairs.
- **Chronicle Queue Integration**: Reads market data from a Chronicle Queue.
- **ClickHouse Integration**: Inserts market data into a ClickHouse database.
- **Logging**: Uses SLF4J with Logback for logging to both console and rolling file appenders.

## Components

### Market Data Generator

A separate class responsible for generating market data, including bid and ask prices, amount bands, and points. It triggers a `marketDataUpdate` listener to notify when new market data is generated.

### Chronicle Queue Writer

Listens for market data updates and writes the data to a Chronicle Queue.

### Data Ingestion Pipeline

Reads market data from the Chronicle Queue and inserts it into a ClickHouse database. It handles batch processing to ensure efficient data insertion.

## Configuration

### Logback Configuration

The project uses Logback for logging, configured to log messages to both the console and a rolling file. The rolling file appender creates a new log file each day and retains up to 30 days of log files.

### Maven Dependencies

The project includes dependencies for Chronicle Queue, ClickHouse client, SLF4J, Logback, and LZ4 compression.

## Usage

1. **Set Environment Variables**:
   - `QUEUE_PATH`: Path to the Chronicle Queue.
   - `CLICKHOUSE_ENDPOINT`: ClickHouse server endpoint.
   - `CLICKHOUSE_USER`: ClickHouse username.
   - `CLICKHOUSE_PASSWORD`: ClickHouse password.
   - `CLICKHOUSE_DATABASE`: ClickHouse database name.

2. **Run the Application**:
   - Execute the `DataIngestionPipeline` class to start reading from the Chronicle Queue and ingesting data into ClickHouse.

## Logging

Logs are written to both the console and a rolling file located in the `logs` directory. The log files are rotated daily, and up to 30 days of logs are retained.

## Dependencies

- **Chronicle Queue**: For reading and writing market data.
- **ClickHouse Client**: For interacting with the ClickHouse database.
- **SLF4J**: For logging abstraction.
- **Logback**: For logging implementation.
- **LZ4**: For compression.

## Build and Run

1. **Build the Project**:
   - Use Maven to build the project: `mvn clean install`.

2. **Run the Application**:
   - Execute the main class: `java -cp target/ingestion-1.0-SNAPSHOT.jar com.example.DataIngestionPipeline`.
