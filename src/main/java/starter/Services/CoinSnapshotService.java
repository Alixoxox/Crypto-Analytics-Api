package starter.Services;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOptions;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import starter.Entity.Coin;
import starter.Utils.Chart;
import starter.Entity.CoinSnapshot;
import starter.Repository.CoinSnapRepo;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
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
    List<String> blacklist = List.of(
            "tether", "usdt", "usdt0", "usdtb",
            "usd-coin", "usdc", "usdc0",
            "dai", "susd", "usds", "susds",
            "tusd", "trueusd", "gusd", "paxos-standard", "usdp",
            "binance-usd", "busd", "fei-usd", "usdn", "alusd",
            "frax", "lusd", "musd", "nusd","binance-bridged-usdc-bnb-smart-chain",

            // Bridged/staked variants
            "ethena-usde", "ethena-staked-usde",
            "polygon-bridged-usdt-polygon",
            "binance-bridged-usdt-bnb-smart-chain",
            "usd1-wlfi", "usde",
            "bridged-usdc", "bridged-usdt", "axlusdc", "axlusdt",

            // Institutional & obscure
            "blackrock-usd-institutional-digital-liquidity-fund",
            "first-digital-usd", "fdusd",
            "sai", "husd", "xsgd", "vai",


            // Algorithmic/failed stablecoins
            "ust", "terrausd", "usn", "ees",
            "bean", "mim", "ustc"
    );
    public List<org.bson.Document> FetchTopGainers(int limit){ //highest price increase in 24h
        Aggregation aggregation=newAggregation(
                Aggregation.match(Criteria.where("last_updated").gte(Instant.now().minus(Duration.ofDays(1)).toEpochMilli())),
                Aggregation.match(Criteria.where("coinId").nin(blacklist)),
                Aggregation.sort(Sort.by(Sort.Order.asc("coinId"), Sort.Order.desc("last_updated"))),
                Aggregation.group("coinId").first("$$ROOT").as("doc"),
                Aggregation.replaceRoot("doc"),
                Aggregation.sort(Sort.Direction.DESC, "price_change_percentage_24h"),
                limit(limit)
        );
        return mongoTemplate.aggregate(aggregation, "coin_snapshots", org.bson.Document.class).getMappedResults();
    }
    public List<Document> FetchWatching(List<String> watchlist) {
            Aggregation aggregation = Aggregation.newAggregation(
                    Aggregation.match(Criteria.where("coinId").in(watchlist)),
                    Aggregation.sort(Sort.Direction.DESC, "last_updated"),
                    Aggregation.group("coinId").first(Aggregation.ROOT).as("latest"),
                    Aggregation.replaceRoot("latest"),
                    Aggregation.sort(Sort.Direction.DESC, "market_cap") // sort by market_cap in MongoDB
            );

            return mongoTemplate
                    .aggregate(aggregation, "coin_snapshots", Document.class)
                    .getMappedResults();
        }

    public List<org.bson.Document> FetchBestCoins(int limit){ //market best

      AggregationOptions options = AggregationOptions.builder()
    .allowDiskUse(true)
    .build();

    Aggregation aggregation = Aggregation.newAggregation(
        Aggregation.match(Criteria.where("market_cap_rank").gte(1).lte(limit)),
        Aggregation.match(Criteria.where("coinId").nin(blacklist)),
        Aggregation.group("coinId").first("$$ROOT").as("doc"),
        Aggregation.replaceRoot("doc"),
        Aggregation.sort(Sort.Direction.ASC, "market_cap_rank")
    ).withOptions(options);

    return mongoTemplate.aggregate(aggregation, "coin_snapshots", Document.class)
                    .getMappedResults();}

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
