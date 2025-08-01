package starter.Repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import starter.Entity.CoinSnapshot;

import java.util.List;
import java.util.Optional;

@Repository
public interface CoinSnapRepo extends MongoRepository<CoinSnapshot,String> {
    Optional<CoinSnapshot> findFirstByCoinIdOrderByLastUpdatedDesc(String coinId);
    Optional<CoinSnapshot> findFirstByCoinIdContainingIgnoreCase(String coinId);
    Optional<CoinSnapshot> findTopByCoinIdAndLastUpdatedLessThanEqualOrderByLastUpdatedDesc(String coinId, Long timestamp);

}
