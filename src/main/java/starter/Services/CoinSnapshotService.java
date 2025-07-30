package starter.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import starter.Entity.Coin;
import starter.Utils.Chart;
import starter.Entity.CoinSnapshot;
import starter.Repository.CoinSnapRepo;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static com.mongodb.client.model.Aggregates.group;
import static java.util.Collections.sort;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
public class CoinSnapshotService {

    @Autowired
    private GekoApi ga;
    @Autowired
    private CoinSnapRepo snapshotRepository;
    @Autowired
    private MongoTemplate mongoTemplate;

    public List<org.bson.Document> FetchUniqueLatestSnapshots(int page, int size){ //random may repeat
        Aggregation aggregation=Aggregation.newAggregation(
                Aggregation.sort(Sort.Direction.DESC, "last_updated"),
                Aggregation.group("coinId").first("$$ROOT").as("doc"),
                Aggregation.replaceRoot("doc"),
                Aggregation.skip((long) page * size),
                Aggregation.limit(size)
        );

        return mongoTemplate.aggregate(aggregation, "coin_snapshots", org.bson.Document.class)
                .getMappedResults();

    }

    public List<org.bson.Document> FetchTopGainers(int limit){ //highest price increase in 24h
        Aggregation aggregation=newAggregation(
                Aggregation.match(Criteria.where("last_updated").gte(Instant.now().minus(Duration.ofDays(1)).toEpochMilli())),
                Aggregation.sort(Sort.Direction.DESC, "last_updated"),
                Aggregation.group("coinId").first("$$ROOT").as("doc"),
                Aggregation.replaceRoot("doc"),
                Aggregation.sort(Sort.Direction.DESC, "price_change_percentage_24h"),
                limit(limit)
        );
        return mongoTemplate.aggregate(aggregation, "coin_snapshots", org.bson.Document.class).getMappedResults();
    }
    public List<org.bson.Document> FetchTopLosers(int limit){ //lowest increase in 24h
        Aggregation aggregation=newAggregation(
                Aggregation.sort(Sort.Direction.DESC, "last_updated"),
                Aggregation.group("coinId").first("$$ROOT").as("doc"),
                Aggregation.replaceRoot("doc"),
                Aggregation.sort(Sort.Direction.ASC, "price_change_percentage_24h"),
                limit(limit)
        );
        return mongoTemplate.aggregate(aggregation, "coin_snapshots", org.bson.Document.class).getMappedResults();
    }
    public List<org.bson.Document> FetchBestCoins(int page,int size){ //market best
        int startRank = page * size + 1;
        int endRank   = (page + 1) * size;
        Aggregation aggregation=Aggregation.newAggregation(
                Aggregation.match(Criteria.where("market_cap_rank").gte(startRank).lte(endRank)),
                Aggregation.sort(Sort.Direction.DESC, "last_updated"),
                Aggregation.group("market_cap_rank").first("$$ROOT").as("doc"), //remove duplicate
                Aggregation.replaceRoot("doc"),
                Aggregation.sort(Sort.Direction.ASC, "market_cap_rank")

        );
        return mongoTemplate.aggregate(aggregation, "coin_snapshots", org.bson.Document.class).getMappedResults();
    }

    public Chart fetchChartData(String name){
        Aggregation aggregation=Aggregation.newAggregation(
                Aggregation.match(Criteria.where("coinId").is(name)),
                Aggregation.sort(Sort.Direction.DESC, "last_updated")
        );
        List<CoinSnapshot> data=mongoTemplate.aggregate(aggregation, "coin_snapshots", CoinSnapshot.class).getMappedResults();
        List<Long> timestamps = new ArrayList<>();
        List<Double> prices = new ArrayList<>();

        for (CoinSnapshot s : data) {
            timestamps.add(s.getLastUpdated());
            prices.add(s.getCurrent_price());
        }
        CoinSnapshot lastestShot=data.getFirst();
        return new Chart(lastestShot,prices,timestamps);
    }
    @Async
    public CompletableFuture<Chart> GetCompData(String Coin){
        return CompletableFuture.completedFuture(fetchChartData(Coin));
    }
    public Set<String> getCoins(List<CoinSnapshot> snapshot){
        Set<String> uniqueName= new HashSet<>(); //hashset extends set
        for (CoinSnapshot snap: snapshot ){ //for each loop
            String name= snap.getCoinId();
            if(name!=null) uniqueName.add(name);
        }
        return uniqueName;
    }

}
