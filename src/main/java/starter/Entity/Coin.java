package starter.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Document(collection = "Coins")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Coin {
    @Id
    private String id;
    private String symbol;
    private String name;
    private Map<String, String> platforms;

}
