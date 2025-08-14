package starter.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import starter.Entity.CoinPredictions;
import starter.Repository.CoinPredictionRepository;
import java.util.List;

@Service
public class CoinPredictService {

    @Autowired
    private CoinPredictionRepository repo;

    public void savePrediction(String coinId, List<Long> predictedTimes, List<Double> predictedPrices) {
        CoinPredictions prediction = new CoinPredictions();
        prediction.setCoinId(coinId);
        prediction.setPredictedTime(predictedTimes);
        prediction.setPredictedPrice(predictedPrices);
        prediction.setGeneratedAt(System.currentTimeMillis());
        repo.save(prediction);
    }
}