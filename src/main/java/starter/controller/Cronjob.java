package starter.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import starter.Repository.MarketRepo;
import starter.Repository.NotifyRepo;
import starter.Repository.TrendingCoinRepo;
import starter.Services.EntryService;
import starter.Services.NotifyService;

@Component
public class Cronjob {
    @Autowired
    private NotifyRepo NFR;
    @Autowired
    private EntryService CES;
    @Autowired
    private TrendingCoinRepo TCR;
    @Autowired
    private MarketRepo MR;
    @Autowired
    private NotifyService NFS;
    @Scheduled(cron = "0 0 0 */4 * ?")  //every 4 days
    public void deleteNotifications(){
        NFR.deleteAll();
    }

    @Scheduled(cron = "0 0 */1 * * *")  // Every hour
    public void runJobMarketData(){
        CES.SaveSnapshot();
}
    @Scheduled(cron = "0 */30 * * * *")  // Every 30 mins
    public void runTrendingJob(){
        TCR.deleteAll();
        CES.SaveNewTrend();
}
    @Scheduled(cron = "0 0 17 * * *")  // 17 = 5 PM
    public void runMarketreviewJob(){
        CES.SaveMarketReview();
    }

    @Scheduled(cron = "0 0 17 * * *")  // 17 = 5 PM
    public void runGlobalUpdate(){
        NFS.MarketType();
        NFS.updateTopCoinNotifications();
    }
}
