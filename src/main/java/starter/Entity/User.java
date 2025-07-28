package starter.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    private ObjectId id;

    private String name;
    private String pictureUrl;

    @Indexed(unique = true)
    private String email;

    private String googleId;    // ❗️ Nullable for email users
    private String password;    // ❗️ Nullable for Google users

}






