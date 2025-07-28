package starter.Utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import starter.Entity.CoinSnapshot;

import java.util.List;

@Data
@AllArgsConstructor
public class Chart {
    private CoinSnapshot lastestShot;
    private List<Double> prices;
    private List<Long> timestamps;
}
