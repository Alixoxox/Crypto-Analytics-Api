package starter.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.error.Mark;
import starter.Entity.Coin;
import starter.Entity.CoinSnapshot;
import starter.Entity.Market;
import starter.Entity.Notify;
import starter.Repository.NotifyRepo;

import javax.swing.text.Document;
import java.util.List;

@Service
public class NotifyService {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private NotifyRepo NR;
    @Autowired
    private CoinSnapshotService CSS;

    public void MarketType(){
        Aggregation aggregation=Aggregation.newAggregation(
                Aggregation.sort(Sort.Direction.DESC, "last_updated"),
                Aggregation.limit(7)
        );
        List<Market> data=mongoTemplate.aggregate(aggregation, "Market", Market.class).getMappedResults();
        if(data.size()<2) return;
        Market today = data.get(0);
        Market yesterday = data.get(data.size()-1);
        boolean isBull = today.getTotalMarketCapUsd() > yesterday.getTotalMarketCapUsd()
                && today.getBtcCapPercentage() < yesterday.getBtcCapPercentage();
        String title = isBull ? "Bull Market Detected" : "Bear Market Warning";

        // Check if similar notification already exists in last 12 hours
        long cutoff = System.currentTimeMillis() - 12 * 60 * 60 * 1000;
        List<Notify> existing = NR.findByTitleAndLastUpdatedAfter(title, cutoff);

        if (!existing.isEmpty()) {
            System.out.println("Duplicate notification prevented: " + title);
            return;
        }

        Notify notify = new Notify();
        notify.setTitle(title);
        notify.setCoinId("1");
        notify.setMessage(isBull ?
                "Market cap increased and BTC dominance dropped — altcoins might be gaining strength!" :
                "Market cap dropped and BTC dominance rose — investors may be moving to BTC as a safe haven.");
        notify.setLastUpdated(System.currentTimeMillis());
        NR.save(notify);
    }

    public void updateTopCoinNotifications() {
        // Fetch latest snapshot per coin
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.sort(Sort.Direction.DESC, "last_updated"),
                Aggregation.group("coinId")
                        .first("coinId").as("coinId")
                        .first("price_change_percentage_24h").as("price_change_percentage_24h")
                        .first("price").as("price")
                        .first("last_updated").as("lastUpdated"),
                Aggregation.match(Criteria.where("price_change_percentage_24h").gte(5)),
                Aggregation.sort(Sort.Direction.DESC, "price_change_percentage_24h"),
                Aggregation.limit(40)
        );

        List<CoinSnapshot> topCoins = mongoTemplate
                .aggregate(agg, "coin_snapshots", CoinSnapshot.class)
                .getMappedResults();

        for (CoinSnapshot coin : topCoins) {
            Notify n = new Notify();
            n.setCoinId(coin.getCoinId());

            double change = coin.getPrice_change_percentage_24h();
            String formattedChange = String.format("%.2f%%", change);
            String formattedPrice = String.format("$%,.2f", coin.getCurrent_price());

            // Always positive
            n.setTitle("🚀 " + coin.getCoinId().toUpperCase() + " surged " + formattedChange);
            n.setMessage(
                    String.format("%s hit %s, gaining %s in the past 24h.",
                            coin.getCoinId(),
                            formattedPrice,
                            formattedChange)
            );

            n.setLastUpdated(coin.getLastUpdated());
            mongoTemplate.save(n);
        }
    }






}
