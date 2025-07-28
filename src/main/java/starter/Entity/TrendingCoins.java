package starter.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "trending_coins")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrendingCoins {

    @Id
    private String id; // CoinGecko coin ID (e.g., "bitcoin")

    private String coin_id; // Reference to Coin._id

    private String symbol;
    private String name;
    private String large; //image

    private int market_cap_rank;

    private double price_usd; //btc as a common price convert it to local currency
    private double priceChangePercentage24hUsd;

    private long market_cap;
    private long total_volume;
    private int score;
    private String sparkline; // Optional

    private long fetchedAt; // Epoch millis
}
