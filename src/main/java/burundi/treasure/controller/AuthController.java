package burundi.treasure.controller;

import burundi.treasure.common.Utils;
import burundi.treasure.jwt.JwtTokenProvider;
import burundi.treasure.model.User;
import burundi.treasure.payload.*;
import burundi.treasure.service.MPSService;
import burundi.treasure.service.OTPService;
import burundi.treasure.service.SupperAppService;
import burundi.treasure.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Log4j2
@RequestMapping("/api/auth")
public class AuthController {

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JwtTokenProvider tokenProvider;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private UserService userService;

	@Autowired
	private OTPService otpService;
	
	@Autowired
	private MPSService mpsService;
	
	@Autowired
	private Utils utils;

	@Autowired
	private SupperAppService supperAppService;
	
	@PostMapping("/verify_otp")
	public ResponseEntity<?> auth(@Valid @RequestBody VerifyOTPRequest verifyOTPRequest) {
//		log.info("Auth request: " + authRequest);
		
		// Lấy thông tin user bằng số điện thoại
		String phone = utils.formatPhoneBurundi(verifyOTPRequest.getPhone());
		User user = userService.findByPhone(phone);

		// Xác thực otp
		Response response = userService.authOTP(user, verifyOTPRequest.getOtp());
		if ("OK".equals(response.status)) {
//        	Authentication authentication = authenticationManager.authenticate(
//                    new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
//
//            SecurityContextHolder.getContext().setAuthentication(authentication);

			String jwt = tokenProvider.generateToken(user);

			VerifyOTPResponse verifyOTPResponse = new VerifyOTPResponse(jwt);
			return ResponseEntity.ok().body(verifyOTPResponse);
		} else {
			return ResponseEntity.ok().body(response);
		}
	}
	
	@PostMapping("/send_otp")
	public ResponseEntity<?> sendOTP(@Valid @RequestBody SendOTPRequest sendOTPRequest) {
		
		String phone = utils.formatPhoneBurundi(sendOTPRequest.getPhone());
		User user = userService.findByPhone(phone);
		if(user == null) {
			user = userService.createNewUserWithPhone(phone, "WEB");
		}
		
		Boolean isSendOTP = otpService.sendOTP(user);
		if(isSendOTP) {
			return ResponseEntity.ok(new Response("OK","OK"));
		} else {
			return ResponseEntity.ok(new Response("FAILED","FAILED"));
		}
	}

	@PostMapping("/verify_supper_app")
	public ResponseEntity<?> verifySupperApp(@Valid @RequestBody VerifySupperAppRequest verifySupperApp) {

		Response response = new Response("FAILED","FAILED");
		try {
			boolean verify = supperAppService.verifyToken(verifySupperApp.getMsisdn(), verifySupperApp.getToken());
			if (verify) {
				String phone = verifySupperApp.getMsisdn();
				phone = utils.formatPhoneBurundi(phone);

				User user = userService.findByPhone(phone);
				if(user == null) {
					user = userService.createNewUserWithPhone(phone, "SUPPER_APP");
				}

				String jwt = tokenProvider.generateToken(user);

				VerifyOTPResponse verifyOTPResponse = new VerifyOTPResponse(jwt);
				return ResponseEntity.ok().body(verifyOTPResponse);
			} else {
				// Block test. Xóa sau khi test xong
//		    	String serverNumberFile = "test.txt";
//		    	String serverNumber = new String(Files.readAllBytes(Paths.get(serverNumberFile)));
//		    	log.info("TEST: " + serverNumber);
//		    	if("NATCOM".equals(serverNumber)) {
//		    		String phone = "50940677800";
//		    		phone = utils.formatPhoneHaiti(phone);
//
//					User user = userService.findByPhone(phone);
//					if(user == null) {
//						user = userService.createNewUserWithPhone(phone);
//					}
//
//					String jwt = tokenProvider.generateToken(user);
//
//					VerifyOTPResponse verifyOTPResponse = new VerifyOTPResponse(jwt);
//					return ResponseEntity.status(HttpStatus.OK).body(verifyOTPResponse);
//		    	}
			}

		} catch (Exception e) {
			log.warn("BUGS", e);
		}

		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
	}

	@PostMapping("/logout")
	public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
		if (authentication != null) {
			new SecurityContextLogoutHandler().logout(request, response, authentication);
		}
		request.getSession().invalidate(); // Hủy session hiện tại
		return ResponseEntity.status(HttpStatus.OK).body(utils.getResponseOK());
	}

}
