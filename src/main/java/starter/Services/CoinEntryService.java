package starter.Services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import starter.Entity.Coin;
import starter.Repository.CoinDataRepo;

import java.util.List;
import java.util.Map;

@Service
public class CoinEntryService {

    @Autowired
    private GekoApi ga;

    @Autowired
    private CoinDataRepo CDR;

public void SaveCoinPlain(){
    try{
        String response=ga.getCoins();
        if(response!=null){
            JsonNode coinList = new ObjectMapper().readTree(response);
            for (JsonNode node : coinList) {
                Coin coin = new Coin(
                        node.path("id").asText(),
                        node.path("symbol").asText(),
                        node.path("name").asText(),
                        new ObjectMapper().convertValue(node.path("platforms"), new TypeReference<Map<String, String>>() {})
                );
                CDR.save(coin);
            }
        } else {
                System.err.println("Failed to fetch coins: " );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Page<Coin> FetchCoins(int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        return CDR.findAll(pageable);
    }
}

