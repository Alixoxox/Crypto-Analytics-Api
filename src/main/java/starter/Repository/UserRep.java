package starter.Repository;

import starter.Entity.User;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRep extends MongoRepository<User, ObjectId> {

    User findByName(String name );
    User findByEmail(String email);
}
