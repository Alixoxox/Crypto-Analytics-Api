package starter.Services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import starter.Entity.TrendingCoins;
import starter.Repository.TrendingCoinRepo;

import java.util.ArrayList;
import java.util.List;

@Service
public class TrendingCoinService {

    @Autowired
    private GekoApi ga;

    @Autowired
    private TrendingCoinRepo TCR;

    private static final ObjectMapper mapper=new ObjectMapper(); //convert string to Object entity java rules

    public void SaveData(){

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
                            item.get("coin_id").asText(),                                              // ObjectId coin_id (from DB)
                            item.get("symbol").asText(),                               // String symbol
                            item.get("name").asText(),                                 // String name
                            item.get("large").asText(),                                // String large (image)
                            item.get("market_cap_rank").asInt(),                       // int market_cap_rank
                            item.get("price_btc").asDouble(),                          // double price_btc
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

    public Page<TrendingCoins> FetchTrendy(int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        return TCR.findAll(pageable);
    }

}
