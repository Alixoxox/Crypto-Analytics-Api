package starter.Repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import starter.Entity.CoinSnapshot;

@Repository
public interface CoinSnapRepo extends MongoRepository<CoinSnapshot,String> {
}
