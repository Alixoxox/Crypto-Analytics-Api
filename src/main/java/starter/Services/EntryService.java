package starter.Services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import starter.Entity.CoinSnapshot;
import starter.Entity.Market;
import starter.Entity.TrendingCoins;
import starter.Repository.CoinDataRepo;
import starter.Repository.CoinSnapRepo;
import starter.Repository.MarketRepo;
import starter.Repository.TrendingCoinRepo;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class EntryService {

    @Autowired
    private GekoApi ga;

    @Autowired
    private CoinDataRepo CDR;

    @Autowired
    private MarketRepo MS;

    @Autowired
    private CoinSnapRepo snapshotRepository;

    private static final ObjectMapper mapper=new ObjectMapper(); //convert string to Object entity java rules
    @Autowired
    private TrendingCoinRepo TCR;

    public void SaveNewTrend(){
        try {
            String json=ga.getTrending();
            if(json!=null) {
                JsonNode root = mapper.readTree(json).get("coins");
                long now = System.currentTimeMillis();

                List<TrendingCoins> coins = new ArrayList<>();
                for (JsonNode node : root) {
                    JsonNode item = node.get("item");
                    TrendingCoins coin = new TrendingCoins(
                            item.get("id").asText(),                                   // String id
                            item.get("coin_id").asText(),                              // ObjectId coin_id (from DB)
                            item.get("symbol").asText(),                               // String symbol
                            item.get("name").asText(),                                 // String name
                            item.get("large").asText(),                                // String large (image)
                            item.get("market_cap_rank").asInt(),                       // int market_cap_rank
                            item.get("data").path("price").asDouble(0),                          // double price_btc
                            item.path("data").path("price_change_percentage_24h").path("usd").asDouble(0), // double priceChangePercentage24hUsd
                            item.path("data").path("market_cap").asLong(0),                                      // long market_cap
                            item.path("data").path("total_volume").asLong(0),                                         // long totalVolume
                            item.get("score").asInt(),                                 // int score
                            item.path("data").path("sparkline").asText(null),          // String sparkline
                            now                                                        // long fetchedAt
                    );
                    coins.add(coin);
                }
                TCR.saveAll(coins);
                System.out.println("Saved trending coins: " + coins.size());
            }else {
                System.out.println("Failed to save trends db error");
            }

        } catch (JsonProcessingException e) {
            System.err.println("Failed to save trending coins: " + e.getMessage());
        }
    }

    public void SaveMarketReview(){
        try{
        String response=ga.getMarketOverview();
        if(response!=null) {
            JsonNode root = new ObjectMapper().readTree(response);
            JsonNode data = root.path("data");
            long updatedAt = data.path("updated_at").asLong() * 1000L;
            String id = data.path("markets").asText() + "-" + updatedAt;
            Market market = new Market(
                            id,
                            data.path("active_cryptocurrencies").asLong(),
                            data.path("markets").asLong(),
                            data.path("total_market_cap").path("usd").asDouble(),
                            data.path("total_volume").path("usd").asDouble(),
                            data.path("market_cap_percentage").path("btc").asDouble(),
                            updatedAt
            );
            MS.save(market);
        } else {
            System.err.println("Failed to fetch coins: " );
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}
    public void SaveSnapshot(){
        try {
            String response=ga.getMarketData();
            if (response!=null) {
                JsonNode marketData = mapper.readTree(response);

                for (JsonNode coinNode : marketData) {
                    String coinIdStr = coinNode.get("id").asText();
                    long updatedAt = Instant.parse(coinNode.get("last_updated").asText()).toEpochMilli();

                    CoinSnapshot snapshot = new CoinSnapshot(
                            coinIdStr + "_" + updatedAt,
                            coinNode.path("id").asText(""), // id here coin id
                            coinNode.path("image").asText(null),
                            coinNode.path("current_price").asDouble(0.0),
                            coinNode.path("high_24h").asDouble(0.0),
                            coinNode.path("low_24h").asDouble(0.0),
                            coinNode.path("price_change_percentage_24h").asDouble(0.0),
                            coinNode.path("market_cap").asLong(0),
                            coinNode.path("total_volume").asLong(0),
                            coinNode.path("market_cap_rank").asInt(0),
                            coinNode.path("circulating_supply").asDouble(0.0),
                            coinNode.path("total_supply").asDouble(0.0),
                            coinNode.path("max_supply").asInt(0),
                            coinNode.path("ath").asDouble(0.0),
                            coinNode.path("ath_change_percentage").asDouble(0.0),
                            coinNode.path("ath_date").asLong(0),
                            coinNode.path("alt").asDouble(0.0),
                            coinNode.path("atl_change_percentage").asDouble(0.0),
                            coinNode.path("alt_date").asLong(0),
                            updatedAt
                    );
                    snapshotRepository.save(snapshot);
                }
                System.out.println("Saved coin snaps");

            } else {
                System.err.println("Failed to fetch market data. " );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

