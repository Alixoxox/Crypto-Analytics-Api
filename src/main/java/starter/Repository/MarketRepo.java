package starter.Repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import starter.Entity.Market;

@Repository
public interface MarketRepo extends MongoRepository<Market,String> {

}
