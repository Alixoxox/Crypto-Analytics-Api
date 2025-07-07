package starter.Services;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import starter.Entity.User;
import starter.Repository.UserRep;

import java.util.List;
import java.util.Optional;
@Service
public class UserEntryService {

    @Autowired
    private UserRep UER;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private boolean isBcryptEncoded(String password) {
        return password != null && password.matches("^\\$2[aby]\\$.{56}$");
    }

    public int saveEntry(User data){
        User existing = UER.findByEmail(data.getEmail());

        if (existing != null) {
            return 0;
        } else {
            // New user, encode password if it's not already encoded
            if (!isBcryptEncoded(data.getPassword())) {
                data.setPassword(passwordEncoder.encode(data.getPassword()));
            }
        }
        UER.save(data);
        return 1;
    }

    public List<User> getAll(){
        return UER.findAll();
    }
    public Optional<User> findByid(ObjectId id){
        return UER.findById(id);
    }
    public void DeleteId(ObjectId id){
        UER.deleteById(id);
    }
    public User findByName(String name) {
        return UER.findByName(name); // will no longer throw NullPointerException
    }
}
