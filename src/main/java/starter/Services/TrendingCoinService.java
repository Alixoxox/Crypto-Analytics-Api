package starter.Services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import starter.Entity.TrendingCoins;
import starter.Repository.TrendingCoinRepo;

import java.util.ArrayList;
import java.util.List;

@Service
public class TrendingCoinService {

    @Autowired
    private GekoApi ga;

    @Autowired
    private TrendingCoinRepo TCR;

    public List<TrendingCoins> FetchTrendy(){
        return TCR.findAll();
    }

}
