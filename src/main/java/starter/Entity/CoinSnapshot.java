package starter.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

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
    private double max_supply;

    private double ath;  //all time high
    private double ath_change_percentage;
    private long ath_date; // or String

    private double atl; //all time low
    private double atl_change_percentage;
    private long atl_date; // or String
    @Field("last_updated")  // 🔁 maps MongoDB field to Java field
    private long lastUpdated;

    @Indexed(expireAfterSeconds = 7776000)
    private Date expiryDate = new Date();
}
