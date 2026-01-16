package starter.Repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import starter.Entity.Market;

import java.util.List;

@Repository
public interface MarketRepo extends MongoRepository<Market,String> {
    List<Market> findTopByOrderByLastUpdatedDesc(Pageable pageable);
}
