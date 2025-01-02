package burundi.treasure.controller;

import java.util.concurrent.CompletableFuture;

import burundi.treasure.firebase.ZodiacGameFirebaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import burundi.treasure.common.Utils;
import burundi.treasure.model.MPSRequest;
import burundi.treasure.model.User;
import burundi.treasure.service.MPSService;
import burundi.treasure.service.UserService;

import lombok.extern.log4j.Log4j2;

@RestController
@Log4j2
public class MPSWebservice {

	@Autowired
	private MPSService mpsService;

	@Autowired
	private Utils utils;
	
	@Autowired
	private UserService userService;

	@Autowired
	private ZodiacGameFirebaseService zodiacGameFirebaseService;

	@PostMapping("/Register")
	public ResponseEntity<String> register(@RequestBody String body) {
		String response = mpsService.responseSusccessfully;
		try {
			MPSRequest mpsRequest = MPSRequest.parseRequestMPS(body, mpsService.REGISTER);
			if(mpsRequest == null) {
//				response = Const.responseFailed;
			} else {
				
				String phone = utils.formatPhoneBurundi(mpsRequest.getMsisdn());
				User user = userService.findByPhone(phone);
				
				// Trường hợp đăng ký vip bằng ussd. Nếu chưa đăng ký tài khoản sẽ tự động đăng ký
				if(user == null) {
					user = userService.createNewUserWithPhone(phone, "WS");
				}
				
				mpsRequest.setUser(user);
				mpsService.add(mpsRequest);
				
				if(mpsRequest.getAmount() > 0) {
					user.setPremium(true);
					user.setTotalPlay(user.getTotalPlay() + 1000);

					// Đồng bộ icoin sang firebase
					zodiacGameFirebaseService.updateTotalIcoin(user.getId(), user.getTotalPlay());
				} else if(user.getFirstRegister() == null || !user.getFirstRegister()) {
					user.setFirstRegister(true);
					user.setPremium(true);
					user.setTotalPlay(user.getTotalPlay() + 1000);

					// Đồng bộ icoin sang firebase
					zodiacGameFirebaseService.updateTotalIcoin(user.getId(), user.getTotalPlay());
				} else {
					user.setPremium(true);
				}
				userService.saveUser(user);
			}
		} catch (Exception e) {
			log.warn("BUGS", e);
//			response = Const.responseFailed;
		}
		return ResponseEntity.ok()
				.header("Content-type", "text/xml")
				.body(response);
	}

	@PostMapping("/PlayOneTime")
	public ResponseEntity<String> playOneTime(@RequestBody String body) {
		String response = mpsService.responseSusccessfully;
		try {
			MPSRequest mpsRequest = MPSRequest.parseRequestMPS(body, mpsService.PLAYONETIME);
			if(mpsRequest == null) {
//				response = Const.responseFailed;
			} else {
				String phone = utils.formatPhoneBurundi(mpsRequest.getMsisdn());
				User user = userService.findByPhone(phone);

				// Trường hợp đăng ký vip bằng ussd. Nếu chưa đăng ký tài khoản sẽ tự động đăng ký
				if(user == null) {
					user = userService.createNewUserWithPhone(phone, "WS");
				}

				mpsRequest.setUser(user);
				mpsService.add(mpsRequest);

				if(mpsRequest.getAmount() > 0) {
					user.setFirstRegister(true);
					user.setTotalPlay(user.getTotalPlay() + 1000);

					// Đồng bộ icoin sang firebase
					zodiacGameFirebaseService.updateTotalIcoin(user.getId(), user.getTotalPlay());
				}
				userService.saveUser(user);
			}
		} catch (Exception e) {
			log.warn("BUGS", e);
//			response = Const.responseFailed;
		}
		return ResponseEntity.ok()
				.header("Content-type", "text/xml")
				.body(response);
	}
	
	@PostMapping("/Cancel")
	public ResponseEntity<String> cancel(@RequestBody String body) {
		String response = mpsService.responseSusccessfully;
		try {
			MPSRequest mpsRequest = MPSRequest.parseRequestMPS(body, mpsService.CANCEL);
			if(mpsRequest == null) {
	//			response = Const.responseFailed;
			} else {
				String phone = utils.formatPhoneBurundi(mpsRequest.getMsisdn());
				User user = userService.findByPhone(phone);
				user.setPremium(false);
				userService.saveUser(user);

				mpsRequest.setUser(user);
				mpsService.add(mpsRequest);
			}
		} catch (Exception e) {
			log.warn("BUGS", e);
	//		response = Const.responseFailed;
		}
		return ResponseEntity.ok()
				.header("Content-type", "text/xml")
				.body(response);
	}
	

	@PostMapping("/Renew")
	public ResponseEntity<String> renew(@RequestBody String body) {
		String response = mpsService.responseSusccessfully;
		CompletableFuture.runAsync(() -> {
			try {
				MPSRequest mpsRequest = MPSRequest.parseRequestMPS(body, mpsService.RENEW);
				if(mpsRequest == null) {
					log.warn("MPSRequest NULL");
				} else {				
					String phone = utils.formatPhoneBurundi(mpsRequest.getMsisdn());
					User user = userService.findByPhone(phone);
					
					// Trường hợp đăng ký vip bằng ussd. Nếu chưa đăng ký tài khoản sẽ tự động đăng ký
					if(user == null) {
						user = userService.createNewUserWithPhone(phone, "WS");
					}
					
					// param = 0 là tài khoản còn tiền
					if(mpsRequest.getAmount() > 0 && "0".equals(mpsRequest.getParams())) {
						user.setTotalPlay(user.getTotalPlay() + 1000);
						user.setPremium(true);
						mpsRequest.setUser(user);
						mpsService.add(mpsRequest);

						// Đồng bộ icoin sang firebase
						zodiacGameFirebaseService.updateTotalIcoin(user.getId(), user.getTotalPlay());
					} else {
						user.setPremium(false);
					}
					userService.saveUser(user);
				}
			} catch (Exception e) {
				log.warn("BUGS", e);
			}
		});
		return ResponseEntity.ok()
				.header("Content-type", "text/xml")
				.body(response);
	}
	
	@PostMapping("/Reward")
	public ResponseEntity<String> reward(@RequestBody String body) {
		String response = mpsService.responseSusccessfully;
		try {
			MPSRequest mpsRequest = MPSRequest.parseRequestMPS(body, mpsService.RENEW);
			if(mpsRequest == null) {
	//			response = Const.responseFailed;
			} else {
				mpsService.add(mpsRequest);
			}
		} catch (Exception e) {
			log.warn("BUGS", e);
	//		response = Const.responseFailed;
		}
		return ResponseEntity.ok()
				.header("Content-type", "text/xml")
				.body(response);
	}
	
}
