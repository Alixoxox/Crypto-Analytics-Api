package starter.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import starter.Repository.MarketRepo;
import starter.Repository.TrendingCoinRepo;
import starter.Services.EntryService;

@Component
public class Cronjob {
    @Autowired
    private EntryService CES;
    @Autowired
    private TrendingCoinRepo TCR;
    @Autowired
    private MarketRepo MR;

    @Scheduled(cron = "0 0 */2 * * *")  // Every 2 hour
    public void runJobMarketData(){
        CES.SaveSnapshot();
}
    @Scheduled(cron = "0 */30 * * * *")  // Every 30 mins
    public void runTrendingJob(){
        TCR.deleteAll();
        CES.SaveNewTrend();
}
    @Scheduled(cron = "0 0 20 * * *") // 18 = 6 PM
    public void runMarketreviewJob(){
        CES.SaveMarketReview();
    }

}
