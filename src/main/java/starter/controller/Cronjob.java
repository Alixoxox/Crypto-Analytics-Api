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
import java.io.File;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
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

    @Scheduled(cron = "0 0 0 */3 * ?")  //every 4 days
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

    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Karachi")
    public void runMarketreviewJob() {
        CES.SaveMarketReview();
    }

    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Karachi")
    public void runGlobalUpdate() {
        NFS.MarketType();
    }
    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Karachi")  // Every 4 hour
    public void runnotifyjob(){
        NFS.updateTopCoinNotifications();
    }
    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Karachi")  // 16 = 4 PM, 5 = minute 5
    public void runPredictions() {
        try {
            // Clear previous predictions
            CPR.deleteAll();
            // Get top coins
            List<CoinSnapshot> snapshots = CSR.findAll();
            Set<String> coins = CSS.getCoins(snapshots);

            ObjectMapper mapper = new ObjectMapper();
            LocalDateTime twoMonthsAgo = LocalDateTime.now().minusMonths(2);
            long twoMonthsAgoMillis = twoMonthsAgo.toInstant(ZoneOffset.UTC).toEpochMilli();
            for (String coin : coins) {
                List<CoinSnapshot> history = mongoTemplate.find(
                        Query.query(Criteria.where("coinId").is(coin)
                                        .and("last_updated").gte(twoMonthsAgoMillis))
                                .with(Sort.by(Sort.Direction.ASC, "last_updated")),
                        CoinSnapshot.class
                );

                if (history.isEmpty()) continue;

                List<Long> timestamps = history.stream().map(CoinSnapshot::getLastUpdated).toList();
                List<Double> prices = history.stream().map(CoinSnapshot::getCurrent_price).toList();
                // Use relative path for deployment
                String scriptPath = new File("src/main/resources/predict_prophet.py").getAbsolutePath();

                ProcessBuilder pb = new ProcessBuilder(
                        "python3", // or "python" depending on server
                        scriptPath,
                        coin,
                        timestamps.stream().map(String::valueOf).collect(Collectors.joining(",")),
                        prices.stream().map(String::valueOf).collect(Collectors.joining(","))
                );

                pb.redirectErrorStream(false); // keep errors separate
                Process process = pb.start();

                StringBuilder jsonOut = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) jsonOut.append(line);
                }

                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    System.err.println("Python process failed for coin: " + coin);
                    continue;
                }

                String jsonResult = jsonOut.toString().trim();
                if (jsonResult.isEmpty()) {
                    System.err.println("Empty JSON output for coin: " + coin);
                    continue;
                }

                JsonNode jsonNode = mapper.readTree(jsonResult); // safe now

                if (jsonNode.has("times") && jsonNode.has("prices")) {
                    List<Long> predictedTimes = new ArrayList<>();
                    jsonNode.get("times").forEach(t -> predictedTimes.add(Long.parseLong(t.asText())));

                    List<Double> predictedPrices = new ArrayList<>();
                    jsonNode.get("prices").forEach(p -> predictedPrices.add(p.asDouble()));

                    predictionService.savePrediction(coin, predictedTimes, predictedPrices);
                } else {
                    System.err.println("Invalid JSON format for coin: " + coin + " → " + jsonResult);
                }
            }

        } catch (Exception e) {
            System.err.println("runPredictions error:");
            e.printStackTrace();
        }
    }

    @Scheduled(cron="0 */10 * * * *")
    public String CheckHealth() {
        return "ok";
    }
}