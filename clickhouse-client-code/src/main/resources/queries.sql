-- Create table for LpPriceEvent
CREATE TABLE IF NOT EXISTS lp_price_events (
    timestamp DateTime64(3),     -- Event timestamp
    uuid UUID,                   -- Unique identifier for the event
    bidPrices Array(Float64),    -- Array of bid prices
    askPrices Array(Float64),    -- Array of ask prices
    quantities Array(Float64),   -- Array of quantities
    ccyPair String,              -- Currency pair
    tenor String,                -- Tenor
    localDate Date,              -- Local date
    lpName String                -- Liquidity provider name
) ENGINE = MergeTree()
PARTITION BY toYYYYMM(localDate)
ORDER BY (timestamp, ccyPair, lpName)
TTL localDate + INTERVAL 1 YEAR;

SELECT MAX(arrayJoin(askPrices)) AS max_ask_price
FROM lp_price_events
WHERE ccyPair = 'EURUSD' AND timestamp BETWEEN '2024-10-01 00:00:00' AND '2024-09-31 23:59:59';

SELECT 
    toDate(timestamp) AS date,
    MIN(arrayJoin(bidPrices)) AS daily_lowest_bid,
    MAX(arrayJoin(askPrices)) AS daily_highest_ask
FROM lp_price_events
GROUP BY date
ORDER BY date;