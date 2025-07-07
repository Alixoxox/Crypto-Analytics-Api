package starter.Entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "coin_snapshots")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoinSnapshot {

    @Id
    private String  id; // Format: coinId + timestamp or UUID

    private String coinId; // Refers to Coin._id

    private String image;
    private double current_price; //in btc
    private double high_24h;
    private double low_24h;
    private double price_change_percentage_24h;

    private long market_cap;
    private long total_volume;
    private int market_cap_rank;

    private double circulating_supply;
    private double total_supply;

    private long last_updated; // Epoch millis (UTC)
}
