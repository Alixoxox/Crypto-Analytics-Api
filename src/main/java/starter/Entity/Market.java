package starter.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Market")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Market {
    @Id
    private String id;
    private Long activeCrypto;
    private Long marketsCirculating; //long removes . after
    private Double totalMarketCapUsd;
    private Double TotalVol;
    private Double btcCapPercentage;
    private long lastUpdated; // Epoch millis (UTC)

}
