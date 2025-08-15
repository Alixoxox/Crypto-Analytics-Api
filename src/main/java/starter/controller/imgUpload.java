package starter.controller;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import starter.Entity.User;
import starter.Repository.UserRep;

import java.util.Map;

@RestController
@RequestMapping("/imgUpload")
public class imgUpload {

    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    private UserRep Uer;

    @PostMapping("/{email}")
    public ResponseEntity ImgFunc(@PathVariable String email, @RequestParam("file") MultipartFile file){
        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), Map.of("folder", "user_uploads"));
            String imageUrl = uploadResult.get("secure_url").toString();
            User user = Uer.findByEmail(email);
            if (user != null) {
                user.setPictureUrl(imageUrl);
                Uer.save(user);
                return ResponseEntity.ok(Map.of("url", imageUrl));
            } else {
                System.out.println("Bad");
                return ResponseEntity.badRequest().body("User not found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}
