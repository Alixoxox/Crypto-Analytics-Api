package starter.Repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import starter.Entity.CoinPredictions;

import java.util.List;

public interface CoinPredictionRepository extends MongoRepository<CoinPredictions, String> {
    List<CoinPredictions> findByCoinId(String coinId);
}
