package starter.Services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import starter.Entity.Coin;
import starter.Entity.CoinSnapshot;
import starter.Repository.CoinSnapRepo;

import java.time.Instant;
import java.util.List;

@Service
public class CoinSnapshotService {

    @Autowired
    private GekoApi ga;

    @Autowired
    private CoinSnapRepo snapshotRepository;

    private static final ObjectMapper mapper=new ObjectMapper(); //convert string to Object entity java rules

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
                            updatedAt
                    );

                    snapshotRepository.save(snapshot);
                }

            } else {
                System.err.println("Failed to fetch market data. " );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public Page<CoinSnapshot> FetchSnatch(int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        return snapshotRepository.findAll(pageable);
    }
}
