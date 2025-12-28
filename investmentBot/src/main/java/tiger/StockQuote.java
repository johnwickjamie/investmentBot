package tiger;

import com.tigerbrokers.stock.openapi.client.config.ClientConfig;
import com.tigerbrokers.stock.openapi.client.https.client.TigerHttpClient;
import com.tigerbrokers.stock.openapi.client.https.domain.quote.item.QuoteDelayItem;
import com.tigerbrokers.stock.openapi.client.https.request.quote.QuoteDelayRequest;
import com.tigerbrokers.stock.openapi.client.https.response.quote.QuoteDelayResponse;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import java.util.ArrayList;
import java.util.List;

public class StockQuote{

    private static final TigerHttpClient client;
    private List<QuoteDelayItem> quoteDelayItems;

    static {
        // 1. Initialize Configuration
        // Ensure tiger_openapi_config.properties is in your /src/main/resources folder
        ClientConfig clientConfig = ClientConfig.DEFAULT_CONFIG;
        clientConfig.configFilePath = "./src/main/resources";
       
        // 2. Initialize the Singleton Client
        client = TigerHttpClient.getInstance().clientConfig(clientConfig);
    }

    public QuoteDelayResponse delayedQuote (String ticker){
        List<String> symbols = new ArrayList<>();
        symbols.add(ticker);
        QuoteDelayRequest delayRequest = QuoteDelayRequest.newRequest(symbols);
        QuoteDelayResponse response = client.execute(delayRequest);    
        
        return response;
    }
    
    public Double responseDecoder (QuoteDelayResponse response){
       quoteDelayItems = response.getQuoteDelayItems();
       Double price = 0.0;
       List<QuoteDelayItem> items = response.getQuoteDelayItems();
        if (items == null || items.isEmpty()) {
            System.out.println("No delayed quote data returned.");
            return price;
        }

        for (QuoteDelayItem item : items) {
            price = item.getClose();   // per docs: "close" is the (delayed) last/closing price in the response
        }
        
       return price;
    }
    
        /**
     * Returns a delayed quote price using a 15-minute cache.
     * If the cached value is < 15 minutes old, it is returned.
     * If older, a new API request is made and the cache is refreshed.
     * If refresh fails but a cached value exists, the cached value is returned as a fallback.
     * @param stockTicker
     * @return 
     * @throws java.lang.Exception 
     */
    public Double delayedPriceCached(String stockTicker) throws Exception {
        if (stockTicker == null) {
            throw new IllegalArgumentException("stockTicker cannot be null");
        }

        final String symbol = stockTicker.trim().toUpperCase();
        final long now = System.currentTimeMillis();

        CachedPrice cached = PRICE_CACHE.get(symbol);
        if (cached != null && cached.isFresh(now)) {
            System.out.println("Returning cached price");
            return cached.price;
        }

        // Prevent multiple threads from refreshing the same symbol simultaneously
        final Object lock = SYMBOL_LOCKS.computeIfAbsent(symbol, s -> new Object());

        synchronized (lock) {
            // Re-check inside the lock in case another thread refreshed it
            cached = PRICE_CACHE.get(symbol);
            final long now2 = System.currentTimeMillis();
            if (cached != null && cached.isFresh(now2)) {
                return cached.price;
            }

            try {
                Double freshPrice = responseDecoder(delayedQuote(symbol));
                PRICE_CACHE.put(symbol, new CachedPrice(freshPrice, System.currentTimeMillis()));
                return freshPrice;
            } catch (Exception e) {
                // Optional resilience: if we have *any* cached value, return it even if stale
                if (cached != null) {
                    return cached.price;
                }
                throw e;
            }
        }
    }
    
    public static void clearPriceCache() {
        PRICE_CACHE.clear();
    }
    
    /* <----------------Private Caching methods--------------> */
    
    // Tiger delayed quotes update every 15 minutes, so cache for 15 minutes.
    private static final long PRICE_CACHE_TTL_MS = TimeUnit.MINUTES.toMillis(15);

    private static final ConcurrentHashMap<String, CachedPrice> PRICE_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Object> SYMBOL_LOCKS = new ConcurrentHashMap<>();

    private static final class CachedPrice {
        private final double price;
        private final long fetchedAtMs;

        private CachedPrice(double price, long fetchedAtMs) {
            this.price = price;
            this.fetchedAtMs = fetchedAtMs;
        }

        private boolean isFresh(long nowMs) {
            return (nowMs - fetchedAtMs) < PRICE_CACHE_TTL_MS;
        }
    }
    
    
    
    
}