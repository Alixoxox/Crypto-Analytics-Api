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
import starter.Entity.CoinPredictions;
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
import java.util.*;
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
    @Scheduled(cron = "0 */5 * * * *", zone = "Asia/Karachi")
    public void runPredictions() {
    try {
        long now = System.currentTimeMillis();
        long cutoff = now - 24 * 60 * 60 * 1000;
        Set<String> allCoins = new HashSet<>(
                mongoTemplate.findDistinct(
                        new Query(),
                        "coinId",
                        CoinSnapshot.class,
                        String.class
                )
        );
        if (allCoins.isEmpty()) return;
        List<String> predictedToday = mongoTemplate.find(
                Query.query(Criteria.where("generatedAt").gte(cutoff)),
                CoinPredictions.class
        ).stream().map(CoinPredictions::getCoinId).toList();
        System.out.println("Already predicted today: " + predictedToday);

        List<String> pendingCoins = allCoins.stream()
                .filter(c -> !predictedToday.contains(c))
                .limit(5)
                .toList();

        if (pendingCoins.isEmpty()) return;

        ObjectMapper mapper = new ObjectMapper();
        long twoMonthsAgo = Instant.now()
                .minus(90, ChronoUnit.DAYS)
                .toEpochMilli();

        for (String coin : pendingCoins) {

            List<CoinSnapshot> history = mongoTemplate.find(
                    Query.query(
                            Criteria.where("coinId").is(coin)
                                    .and("last_updated").gte(twoMonthsAgo)
                    ).with(Sort.by(Sort.Direction.ASC, "last_updated")),
                    CoinSnapshot.class
            );

            if (history.size() < 30) continue; // Prophet needs enough data

            List<Long> timestamps = history.stream()
                    .map(CoinSnapshot::getLastUpdated)
                    .toList();

            List<Double> prices = history.stream()
                    .map(CoinSnapshot::getCurrent_price)
                    .toList();

            File script = new File("/app/scripts/predict_prophet.py");
            if (!script.exists()) {
             throw new RuntimeException("Python script not found at " + script.getAbsolutePath());
            }

            ProcessBuilder pb = new ProcessBuilder(
                    "python3",
                    script.getAbsolutePath(),
                    coin,
                    String.join(",", timestamps.stream().map(String::valueOf).toList()),
                    String.join(",", prices.stream().map(String::valueOf).toList())
            );

            pb.redirectErrorStream(true);
            Process process = pb.start();

           // Capture all output for debugging
            String fullOutput;
            try (BufferedReader br = new BufferedReader(
            new InputStreamReader(process.getInputStream())
            )) {
            fullOutput = br.lines().collect(Collectors.joining("\n"));
            }

            if (process.waitFor() != 0 || fullOutput.isBlank()) continue;

            String json = fullOutput.lines()
            .filter(line -> line.trim().startsWith("{"))
                .reduce((first, second) -> second)  // Get last match
            .orElse("");

            if (json.isBlank()) {
             System.out.println("No JSON found in output: " + fullOutput);
            continue;
            }

            JsonNode node = mapper.readTree(json);
            if (!node.has("times") || !node.has("prices")) continue;

            CoinPredictions cp = new CoinPredictions();
            cp.setCoinId(coin);
            cp.setGeneratedAt(now);

            List<Long> t = new ArrayList<>();
            node.get("times").forEach(x -> t.add(x.asLong()));

            List<Double> p = new ArrayList<>();
            node.get("prices").forEach(x -> p.add(x.asDouble()));

            cp.setPredictedTime(t);
            cp.setPredictedPrice(p);

            CPR.save(cp);

            // Small delay to avoid CPU spike
            Thread.sleep(2000);
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}


    @Scheduled(cron="0 */10 * * * *")
    public String CheckHealth() {
        return "ok";
    }
}