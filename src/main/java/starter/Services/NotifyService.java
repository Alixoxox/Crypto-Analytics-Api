package starter.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.error.Mark;
import starter.Entity.CoinSnapshot;
import starter.Entity.Market;
import starter.Entity.Notify;
import starter.Repository.NotifyRepo;
import java.util.List;

@Service
public class NotifyService {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private NotifyRepo NR;
    @Autowired
    private CoinSnapshotService CSS;
    public void ShowPriceUpdate(String name){
        long cutoff = System.currentTimeMillis() - 24 * 60 * 60 * 1000;

        Aggregation aggregation=Aggregation.newAggregation(
                Aggregation.match(Criteria.where("coinId").is(name)),
                Aggregation.match(Criteria.where("last_updated").gte(cutoff)),
                Aggregation.sort(Sort.Direction.DESC, "last_updated"),
                Aggregation.limit(3)
        );
        List<CoinSnapshot> data=mongoTemplate.aggregate(aggregation, "coin_snapshots", CoinSnapshot.class).getMappedResults();
        if (data.size() < 2) return;
        CoinSnapshot present=data.get(0);
        CoinSnapshot past=data.get(data.size()-1);
        Notify notify = new Notify();
        double priceDiff = present.getCurrent_price() - past.getCurrent_price();
        double percentageChange = (priceDiff / past.getCurrent_price()) * 100;

        if (priceDiff > 0) {
            notify.setTitle("Price Increase");
            notify.setMessage(name + " price increased by $" + String.format("%.2f", priceDiff) +
                    " +(" + String.format("%.2f", percentageChange) + "%)");
        } else {
            notify.setTitle("Price Decrease");
            notify.setMessage(name + " price decreased by $" + String.format("%.2f", Math.abs(priceDiff)) +
                    " -(" + String.format("%.2f", Math.abs(percentageChange)) + "%)");
        }
        notify.setLastUpdated(System.currentTimeMillis());
        NR.save(notify);
    }
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
        notify.setMessage(isBull ?
                "Market cap increased and BTC dominance dropped — altcoins might be gaining strength!" :
                "Market cap dropped and BTC dominance rose — investors may be moving to BTC as a safe haven.");
        notify.setLastUpdated(System.currentTimeMillis());

        NR.save(notify);
    }}
