package starter.Repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import starter.Entity.Notify;

import java.util.List;

public interface NotifyRepo extends MongoRepository<Notify,String> {
    List<Notify> findByTitleAndLastUpdatedAfter(String title, long lastUpdated);

}
