package starter.Services;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import starter.Entity.User;
import starter.Repository.UserRep;

import java.beans.Encoder;
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
    public Optional<User> verifyUser(User data) {
        User existing = UER.findByEmail(data.getEmail());
        if (existing == null) {
            return Optional.empty();
        }
        // Compare raw password with encoded one using matches()
        if (passwordEncoder.matches(data.getPassword(), existing.getPassword())) {
            return Optional.of(existing);
        }

        return Optional.empty();
    }
    public int saveGoogleUser(User data){
        User existing = UER.findByEmail(data.getEmail());
        if (existing == null) {UER.save(data); return 0;}
        if(existing.getGoogleId()!=null && existing.getGoogleId().equals(data.getGoogleId())){return -1;}
        else {
            existing.setPictureUrl(data.getPictureUrl());
            if(existing.getGoogleId()==null){
                existing.setGoogleId(data.getGoogleId());
            }
            existing.setName(data.getName());
            UER.save(existing);
            return 1;
        }
    }
    public void ChangePass(String newPass,User exists){
        exists.setPassword(passwordEncoder.encode(newPass));
        System.out.print("pass");
        UER.save(exists);
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
