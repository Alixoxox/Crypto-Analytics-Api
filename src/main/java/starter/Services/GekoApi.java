package starter.Services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class GekoApi {
    private RestTemplate restTemplate;

    @Value("${coingecko.api.key}")
    private String apiKey;

    public void CoinGeckoService(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    public String getMarketData() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.coingecko.com/api/v3/coins/markets?vs_currency=usd"))
                    .header("accept", "application/json")
                    .header("x-cg-demo-api-key", apiKey)
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (Exception e) {
            return null;
        }
    }
    public String getMarketOverview(){
        try{
            HttpRequest request=HttpRequest.newBuilder()
                    .uri(URI.create("https://api.coingecko.com/api/v3/global"))
                    .header("accept","application/json")
                    .header("x-cg-demo-api-key",apiKey)
                    .method("GET",HttpRequest.BodyPublishers.noBody()).build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        }catch(Exception e){
        return null;
        }
    }
    public String getCoins(){
        try{
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.coingecko.com/api/v3/coins/list?include_platform=true"))
                    .header("accept", "application/json")
                    .header("x-cg-demo-api-key", "CG-iFv6sVygV17DivpzSFgEycUs")
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        }catch(Exception e){
            return null;
        }
    }

    public String getTrending(){
        try{
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.coingecko.com/api/v3/search/trending"))
                    .header("accept", "application/json")
                    .header("x-cg-demo-api-key", "CG-iFv6sVygV17DivpzSFgEycUs")
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        }catch(Exception e){
            return null;
        }
    }

}

