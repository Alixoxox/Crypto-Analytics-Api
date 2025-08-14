package starter.Entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@Document(collection = "coin_predictions")
public class CoinPredictions {
    @Id
    private String id;
    private String coinId;
    private List<Long> predictedTime;
    private List<Double> predictedPrice;
    private long generatedAt;     // When this prediction was generated
}