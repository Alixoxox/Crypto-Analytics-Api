package starter.Repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import starter.Entity.Coin;

@Repository
public interface CoinDataRepo extends MongoRepository<Coin,String> {
}
