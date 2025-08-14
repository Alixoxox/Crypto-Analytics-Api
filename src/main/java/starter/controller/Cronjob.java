package starter.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import starter.Entity.CoinSnapshot;
import starter.Repository.*;
import starter.Services.CoinPredictService;
import starter.Services.CoinSnapshotService;
import starter.Services.EntryService;
import starter.Services.NotifyService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class Cronjob {
    @Autowired
    private NotifyRepo NFR;
    @Autowired
    private EntryService CES;
    @Autowired
    private TrendingCoinRepo TCR;
    @Autowired
    private MarketRepo MR;
    @Autowired
    private CoinSnapRepo CSR;
    @Autowired
    private CoinPredictService predictionService;
    @Autowired
    private CoinSnapshotService CSS;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private CoinPredictionRepository CPR;
    @Autowired
    private NotifyService NFS;

    @Scheduled(cron = "0 0 0 */4 * ?")  //every 4 days
    public void deleteNotifications() {
        NFR.deleteAll();
    }

    @Scheduled(cron = "0 0 */1 * * *")  // Every hour
    public void runJobMarketData() {
        CES.SaveSnapshot();
    }

    @Scheduled(cron = "0 */30 * * * *")  // Every 30 mins
    public void runTrendingJob() {
        TCR.deleteAll();
        CES.SaveNewTrend();
    }

    @Scheduled(cron = "0 0 17 * * *")  // 17 = 5 PM
    public void runMarketreviewJob() {
        CES.SaveMarketReview();
    }

    @Scheduled(cron = "0 0 23 * * *")  // 17 = 5 PM
    public void runGlobalUpdate() {
        NFS.MarketType();
        NFS.updateTopCoinNotifications();
    }
    @Scheduled(cron = "0 0 23 * * *")  // 17 = 5 PM
    public void runPredictions() throws Exception {
        CPR.deleteAll(); // clear previous predictions

        // Get top coins
        List<CoinSnapshot> snapshot = CSR.findAll();
        Set<String> coins = CSS.getCoins(snapshot);

        ObjectMapper mapper = new ObjectMapper();

        for (String coin : coins) {
            // Fetch historical snapshots
            List<CoinSnapshot> history = mongoTemplate.find(
                    Query.query(Criteria.where("coinId").is(coin))
                            .with(Sort.by(Sort.Direction.ASC, "last_updated")),
                    CoinSnapshot.class
            );

            if (history.isEmpty()) continue;

            List<Long> timestamps = history.stream().map(CoinSnapshot::getLastUpdated).toList();
            List<Double> prices = history.stream().map(CoinSnapshot::getCurrent_price).toList();

            ProcessBuilder pb = new ProcessBuilder(
                    "python3",
                    "C:\\Users\\Sufyan_Ali\\OneDrive\\Desktop\\Crypto-app\\server\\Crypto\\src\\main\\resources\\predict_prophet.py",
                    coin,
                    timestamps.stream().map(String::valueOf).collect(Collectors.joining(",")),
                    prices.stream().map(String::valueOf).collect(Collectors.joining(","))
            );
            pb.redirectErrorStream(false); // keep stdout and stderr separate
            Process process = pb.start();

            StringBuilder jsonOut = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonOut.append(line);
                }
            }
            String jsonResult = jsonOut.toString().trim();
            if (jsonResult.isEmpty()) {
                System.err.println("No JSON output for coin: " + coin);
                continue;
            }

            JsonNode jsonNode = mapper.readTree(jsonResult);

            if (jsonNode.has("times") && jsonNode.has("prices")) {
                List<Long> predictedTimes = new ArrayList<>();
                jsonNode.get("times").forEach(t -> predictedTimes.add(Long.parseLong(t.asText())));

                List<Double> predictedPrices = new ArrayList<>();
                jsonNode.get("prices").forEach(p -> predictedPrices.add(p.asDouble()));

                // Save predictions to Mongo
                predictionService.savePrediction(coin, predictedTimes, predictedPrices);
            } else {
                System.err.println("Invalid JSON format for coin: " + coin + " → " + jsonResult);
            }
        }
    }
    }