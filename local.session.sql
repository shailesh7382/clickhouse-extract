WITH bid_diff AS (
    SELECT 
        ccyPair,
        lpName,
        groupArray(timestamp) AS timestamps,
        groupArray(bidPrices[1]) AS bidPricesArray
    FROM lp_price_events
    WHERE ccyPair = 'EURUSD' AND lpName = 'SomeLP'
    GROUP BY ccyPair, lpName
)
SELECT 
    ccyPair,
    lpName,
    MAX(arrayDifference(bidPricesArray)) AS max_bid_diff
FROM bid_diff;