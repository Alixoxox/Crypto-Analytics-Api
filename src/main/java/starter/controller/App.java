package starter.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import starter.Entity.Notify;
import starter.Repository.NotifyRepo;
import starter.Services.NotifyService;

import java.util.List;

@RestController
@RequestMapping("/user")
public class App {
@Autowired
private NotifyService NFS;
@Autowired
private NotifyRepo NFR;
@GetMapping("/notify")
public ResponseEntity<List<Notify>> run(){
List<Notify> x=NFR.findAll();
if (x.isEmpty()){
    return ResponseEntity.badRequest().build();
}
return ResponseEntity.ok(x);
}

}
