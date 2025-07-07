package starter.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import starter.Config.UserDetailImpl;
import starter.Entity.Coin;
import starter.Entity.CoinSnapshot;
import starter.Entity.TrendingCoins;
import starter.Entity.User;
import starter.Repository.UserRep;
import starter.Services.CoinEntryService;
import starter.Services.CoinSnapshotService;
import starter.Services.TrendingCoinService;
import starter.Services.UserEntryService;
import starter.Utils.JwtUtils;

import java.util.List;

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
    private UserRep UER;

    @Autowired
    private CoinEntryService CES;
    @Autowired
    private CoinSnapshotService CSS;
    @Autowired
    private TrendingCoinService TCS;

    @PostMapping("signup/")
    public ResponseEntity<String> Signup(@RequestBody User current){
        try {
            int a=UES.saveEntry(current);
            if (a==0){
                return ResponseEntity.badRequest().body("Already exists");
            }
            String jwt = jwtUtils.generateToken(current.getEmail());
            return ResponseEntity.ok().body(jwt);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed To Create an account");
        }
    }

    @PostMapping("login/")
    public ResponseEntity<String> Login(@RequestBody User currentData){
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(currentData.getName(),currentData.getPassword()));
            userdetailload.loadUserByUsername(currentData.getEmail());

            String jwt= jwtUtils.generateToken(currentData.getEmail());
            return ResponseEntity.ok().body(jwt);
        } catch (AuthenticationException e) {
            return ResponseEntity.badRequest().body("Failed to login");
        }

    }

    @GetMapping("coins/trending")
    public ResponseEntity<String> fetchTrends(@RequestParam int page,@RequestParam int size){
        try {
//            TCS.SaveData();//Do Cron job
            Page<TrendingCoins> response =TCS.FetchTrendy(page,size);
            return ResponseEntity.ok().body(response.toString());
        }catch (Exception e){
            return ResponseEntity.badRequest().body("Failed to fetch Trending Coins");
        }
    }
    @GetMapping("coins/list") //coins/list?page=1&size=10
    public ResponseEntity<String> fetchCoins(@RequestParam int page,@RequestParam int size){
        try {
//            CES.SaveCoinPlain();// NO NEED
            Page<Coin> response =CES.FetchCoins(page,size);
            return ResponseEntity.ok().body(response.toString());
        }catch (Exception e){
            return ResponseEntity.badRequest().body("Failed to fetch Coins");
        }
    }
    @GetMapping("coins/Snapshots")//coins/Snapshots?page=1&size=10
    public ResponseEntity<String> fetchSnapshot(@RequestParam int page,@RequestParam int size){
        try {
//            CSS.SaveSnapshot(); //DO cronjob
            Page<CoinSnapshot> response =CSS.FetchSnatch(page,size);
            return ResponseEntity.ok().body(response.toString());
        }catch (Exception e){
            return ResponseEntity.badRequest().body("Failed to fetch Coins");
        }
    }

}

