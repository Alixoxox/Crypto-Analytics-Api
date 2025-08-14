package starter.controller;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import starter.Entity.CoinPredictions;
import starter.Entity.Notify;
import starter.Entity.User;
import starter.Repository.CoinPredictionRepository;
import starter.Repository.NotifyRepo;
import starter.Repository.UserRep;
import starter.Services.CoinSnapshotService;
import starter.Services.NotifyService;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/user")
public class App {
    @Autowired
    private NotifyService NFS;
    @Autowired
    private CoinSnapshotService CES;
    @Autowired
    private UserRep UER;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private NotifyRepo NFR;
@Autowired
private CoinPredictionRepository CPR;
    @PostMapping("/notify")
    public ResponseEntity<List<Notify>> run(@RequestBody CoinRequest info) {
        User user = UER.findByEmail(info.getEmail());
        if (user == null || user.getWatching() == null || user.getWatching().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // Create a set of coinIds: "1", plus whatever is in the user's watchlist
        List<String> coinIds = new ArrayList<>();
        coinIds.add("1");
        coinIds.addAll(user.getWatching());
        // Single query using $in
        List<Notify> notifications = mongoTemplate.find(
                Query.query(Criteria.where("coinId").in(coinIds))
                .with(Sort.by(Sort.Direction.DESC, "last_updated")),
                Notify.class
        );
        if (notifications.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(notifications);
    }

    @Getter
    @Setter
    public static class CoinRequest {
        private String CoinName;
        private String email;
    }

    @PostMapping("add/watching")
    public ResponseEntity<Object> AddCoinToList(@RequestBody CoinRequest coin) {
        User exist = UER.findByEmail(coin.getEmail());
        if (exist != null) {
            List<String> coinsPresent=exist.getWatching();
            if (coinsPresent == null) {
                exist.setWatching(new ArrayList<>());
            }
            boolean x=coinsPresent.stream().anyMatch(c-> c.equals(coin.getCoinName()));
            if(x==true){
                return ResponseEntity.ok().build();
            }
            exist.getWatching().add(coin.getCoinName());
            UER.save(exist);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("delete/watching")
    public ResponseEntity<Object> DeleteFromList(@RequestBody CoinRequest coin) {
        User exist = UER.findByEmail(coin.getEmail());
        if (exist != null) {
            if (exist.getWatching() == null) {
                exist.setWatching(new ArrayList<>());
            } else {
                exist.getWatching().remove(coin.getCoinName());
                UER.save(exist);
                return ResponseEntity.ok().build();
            }
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("view/watching")
    public ResponseEntity<Object> GetWatchingCoins(@RequestBody CoinRequest coin) {
        User existing = UER.findByEmail(coin.getEmail());
        if (existing != null) {
            List<String> watching = existing.getWatching();
            if (!watching.isEmpty()) {
                List<org.bson.Document> data = CES.FetchWatching(watching);
                return ResponseEntity.ok().body(data);
            }
        }
        return ResponseEntity.badRequest().build();
    }
    @GetMapping("/predict")
    public ResponseEntity<List<CoinPredictions>> PredictCoin(@RequestParam String coin){
        try {
            List<CoinPredictions> x = CPR.findByCoinId(coin);
            return ResponseEntity.ok().body(x);
        }catch(Exception e){
            return ResponseEntity.badRequest().build();
        }
    }
}
