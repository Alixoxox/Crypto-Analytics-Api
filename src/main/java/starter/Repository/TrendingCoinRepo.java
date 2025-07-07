package starter.Repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import starter.Entity.TrendingCoins;

@Repository
public interface TrendingCoinRepo extends MongoRepository<TrendingCoins,String> {
}
