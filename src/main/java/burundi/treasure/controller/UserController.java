	package burundi.treasure.controller;

import burundi.treasure.common.Utils;
import burundi.treasure.model.LuckyHistory;
import burundi.treasure.model.User;
import burundi.treasure.model.dto.LuckyHistoryDTO;
import burundi.treasure.model.dto.UserDTO;
import burundi.treasure.service.MPSService;
import burundi.treasure.service.UserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@Log4j2
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserService userService;
    
    @Autowired
    private MPSService mpsService;

    @Autowired
    private Utils utils;

    @PostMapping("/info")
    public ResponseEntity<?> getUser(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByUserName(userDetails.getUsername());
            
            UserDTO userDTO = new UserDTO(user); 
            
            // Chỉ trả về bằng true 1 lần cho client để show popup win
            if(user.isWin()) {
            	user.setWin(false);
            	userService.saveUser(user);
            }
            
            return ResponseEntity.ok(userDTO);
        } catch (Exception e) {
            log.warn(e);
            return ResponseEntity.internalServerError().body("");
        }
    }

}
