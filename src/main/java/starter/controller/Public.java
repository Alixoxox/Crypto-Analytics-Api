package starter.controller;

import org.apache.coyote.Response;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import starter.Config.UserDetailImpl;
import starter.Entity.*;
import starter.Repository.CoinSnapRepo;
import starter.Repository.MarketRepo;
import starter.Repository.UserRep;
import starter.Services.EntryService;
import starter.Services.CoinSnapshotService;
import starter.Services.TrendingCoinService;
import starter.Services.UserEntryService;
import starter.Utils.Chart;
import starter.Utils.JwtUtils;

import javax.swing.text.html.Option;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/")
public class Public {

    @Autowired
    private UserEntryService UES;

    @Autowired
    private UserDetailImpl userdetailload;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private EntryService CES;
    @Autowired
    private CoinSnapshotService CSS;
    @Autowired
    private CoinSnapRepo CSR;
    @Autowired
    private TrendingCoinService TCS;
    @Autowired
    private MarketRepo MR;
@Autowired
private UserRep UER;
    @GetMapping("ping/")
    public ResponseEntity Ping(){
        return ResponseEntity.ok().build();
    }

    @PostMapping("signup/")
    public ResponseEntity<String> Signup(@RequestBody User current){
        try {
            int a=UES.saveEntry(current);
            if (a==0){
                return ResponseEntity.badRequest().body("Email Already Taken");
            }
            String jwt = jwtUtils.generateToken(current.getEmail());
            return ResponseEntity.ok().body(jwt);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    @PostMapping("google/")
    public ResponseEntity<String> Google(@RequestBody User current){
        try{
            int a=UES.saveGoogleUser(current);
            if(a==0 || a==1 || a==-1){
                String jwt = jwtUtils.generateToken(current.getEmail());
                return ResponseEntity.ok().body(jwt);
            }else{
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("login/")
    public ResponseEntity Login(@RequestBody User currentData){
        try {
            System.out.println(currentData.getEmail()+" "+currentData.getPassword());
            Optional<User> x=UES.verifyUser(currentData);
            if(x.isPresent()){
                String jwt= jwtUtils.generateToken(currentData.getEmail());
                Map<String,Object> response=new HashMap<>();
                response.put("token",jwt);
                response.put("User",x.get());
                return ResponseEntity.ok(response);
            }else{
                return ResponseEntity.badRequest().body("Email not Found");
            }
        } catch (AuthenticationException e) {
            e.printStackTrace();
            return  ResponseEntity.badRequest().build();
        }
    }
    @GetMapping("coins/trending")
    public ResponseEntity<List<TrendingCoins>> fetchTrends(){
        try {
            List<TrendingCoins> response =TCS.FetchTrendy();// limit to 10
            return ResponseEntity.ok(response);
        }catch (Exception e){
            e.printStackTrace();
            return  ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("coins/Snapshots")//coins/Snapshots?page=1&size=10
    public ResponseEntity<List<Document>> fetchSnapshot(@RequestParam int page,@RequestParam int size){
        try {
            List<Document> response =CSS.FetchUniqueLatestSnapshots(page,size); //list of coin via market data
            return ResponseEntity.ok(response);
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    @GetMapping("coins/gainers")
    public ResponseEntity<List<Document>> fetchToppers(){
        try{
            List<Document> reponse=CSS.FetchTopGainers(20);
            return ResponseEntity.ok(reponse);
        }catch(Exception e){
            e.printStackTrace();
            return  ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("coins/topRank")
    public ResponseEntity<List<Document>> GetBYRank(@RequestParam int limit){
        try{
            List<Document> response=CSS.FetchBestCoins(limit);
            return ResponseEntity.ok(response);
        }catch(Exception e){
            e.printStackTrace();
           return ResponseEntity.badRequest().build();
        }
    }
    @GetMapping("coins/chart") //?coinName=
    public ResponseEntity<Chart> GetMarketChart(@RequestParam String name){
        try {
            Chart nw= CSS.fetchChartData(name);
            return ResponseEntity.ok(nw);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    //compare two coins
    @GetMapping("coins/compare")
    public ResponseEntity<Map<String, Chart>> compareTwoCoins(@RequestParam String coin1, String coin2){
        try{
          CompletableFuture<Chart> data1=CSS.GetCompData(coin1);
          CompletableFuture<Chart> data2=CSS.GetCompData(coin2);
          CompletableFuture.allOf(data1,data2).join();
          Map<String, Chart> result =new HashMap<>();
          result.put("coin1",data1.get());
          result.put("coin2",data2.get());
          return ResponseEntity.ok(result);
        }catch(Exception e){
            e.printStackTrace();
            return  ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("market/info")
    public ResponseEntity GetRecentData(){
        try{
            List data= MR.findTopByOrderByLastUpdatedDesc();
            return ResponseEntity.ok(data);
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.badRequest().build();

        }

    }
    @GetMapping("coin/detail")
    public ResponseEntity getDetails(@RequestParam String id) {
        try {
            // Get latest snapshot by coinId
            Optional<CoinSnapshot> snapshot = CSR.findFirstByCoinIdOrderByLastUpdatedDesc(id);
            if (snapshot.isPresent()) {
                Chart nw = CSS.fetchChartData(snapshot.get().getCoinId());
                return ResponseEntity.ok(nw);
            }
            // Try fuzzy match if exact fails
            Optional<CoinSnapshot> snapshot2 = CSR.findFirstByCoinIdContainingIgnoreCase(id);
            if (snapshot2.isPresent()) {
                String actualCoinId = snapshot2.get().getCoinId();
                Chart nw = CSS.fetchChartData(actualCoinId);
                return ResponseEntity.ok(nw);
            }
            return ResponseEntity.badRequest().body("Coin not found");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error occurred");
        }
    }

    @GetMapping("coins/name")
    public ResponseEntity getcoinNames(){
        try{
            List<CoinSnapshot> snapshot=CSR.findAll();
            Set<String> Coins=CSS.getCoins(snapshot);
            return ResponseEntity.ok(Coins);
        }catch(Exception e){
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error occurred");
        }
    }
    @PostMapping("account/changePass")
    public ResponseEntity ChangePassword(@RequestBody User change){
        try{
            User exist=UER.findByEmail(change.getEmail());
            if(exist==null){
                System.out.println("failed");
                return ResponseEntity.badRequest().body("No email id found");
            }else {
                UES.ChangePass(change.getPassword(), exist);
                return ResponseEntity.ok().build();
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error occurred");
        }
    }
    @PostMapping("account/linkG")
    public ResponseEntity LinkGoogle(@RequestBody User current){
        try{
            User exist=UER.findByEmail(current.getEmail());
            if(exist!=null && exist.getGoogleId()!=null){
                return ResponseEntity.badRequest().build();
            } else if (exist!=null && exist.getGoogleId()==null ) {
                exist.setGoogleId(current.getGoogleId());
                UER.save(exist);
                return ResponseEntity.ok().build();
            }else{
                return ResponseEntity.badRequest().build();
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error occurred");
        }
    }
    @PostMapping("account/UnlinkG")
    public ResponseEntity UnLinkGoogle(@RequestBody User current){
        try{
            User exist=UER.findByEmail(current.getEmail());
            if(exist!=null && exist.getGoogleId()==null){
                System.out.println("bad");
                return ResponseEntity.badRequest().build();
            } else if (exist!=null && exist.getGoogleId()!=null ) {
                exist.setGoogleId(null);
                UER.save(exist);
                return ResponseEntity.ok().build();
            }else{
                System.out.println("fail");
                return ResponseEntity.badRequest().build();
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error occurred");
        }
    }
    @PostMapping("account/delete")
    public ResponseEntity DeleteAcc(@RequestBody User current){
        try{
            User exist=UER.findByEmail(current.getEmail());
            if(exist!=null ){
                UER.delete(exist);
                return ResponseEntity.ok().build();
            } else{
                return ResponseEntity.badRequest().build();
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }


}
