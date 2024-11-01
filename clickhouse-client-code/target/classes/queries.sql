-- queries.sql

-- Create table for LpPriceEvent
CREATE_LP_PRICE_EVENT_TABLE = CREATE TABLE IF NOT EXISTS LpPriceEvent (
    timestamp DateTime,
    uuid String,
    bidPrices Array(Float64),
    askPrices Array(Float64),
    quantities Array(Float64),
    ccyPair String,
    tenor String,
    localDate Date,
    lpName String
) ENGINE = MergeTree()
ORDER BY timestamp;

-- Get number of prices for the last 10 days
GET_NUMBER_OF_PRICES_LAST_10_DAYS = SELECT COUNT(*) AS number_of_prices
FROM lp_price_events
WHERE timestamp >= NOW() - INTERVAL 10 DAY;

-- Get historical prices
GET_HISTORICAL_PRICES = SELECT * FROM LpPriceEvent WHERE ccyPair='%s' AND lpName='%s' AND timestamp BETWEEN '%s' AND '%s';

-- Check if a specific event exists
CHECK_EVENT_EXISTS = SELECT COUNT(*) FROM LpPriceEvent WHERE uuid='%s';

-- Insert a new event
INSERT_LP_PRICE_EVENT = INSERT INTO LpPriceEvent (timestamp, uuid, bidPrices, askPrices, quantities, ccyPair, tenor, localDate, lpName) VALUES ('%s', '%s', %s, %s, %s, '%s', '%s', '%s', '%s');