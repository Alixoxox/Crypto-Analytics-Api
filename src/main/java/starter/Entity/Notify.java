package starter.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notify {
    @Id
    String id;

    private String title;

    private String Message;

    private long lastUpdated;
}
