package burundi.treasure.controller;

import java.util.Map;

import burundi.treasure.firebase.ZodiacGameFirebaseService;
import burundi.treasure.model.MPSRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import burundi.treasure.common.Utils;
import burundi.treasure.model.User;
import burundi.treasure.payload.Response;
import burundi.treasure.payload.VerifyOTPRequestMPS;
import burundi.treasure.service.MPSService;
import burundi.treasure.service.UserService;

import lombok.extern.log4j.Log4j2;

@RestController
@RequestMapping("/api/mps")
@Log4j2
public class MPSController {
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private MPSService mpsService;
	
	@Autowired
	private Utils utils;

	@Autowired
	private ZodiacGameFirebaseService zodiacGameFirebaseService;

	@PostMapping("/register")
	public ResponseEntity<?> register(@AuthenticationPrincipal UserDetails userDetails) {
		try {

			// Lưu lại transactionID để dùng ở api verify_register
			User user = userService.findByUserName(userDetails.getUsername());
			MPSRequest mpsRequest = mpsService.newMPSRequest(user, "REGISTER_WEB");

			String transactionId = utils.generateTransactionID();
			user.setLastTransactionId(transactionId);
			user = userService.saveUser(user);
			
			String responseCallSendOtp = mpsService.callApiSendOtp(user.getPhone(), "REGISTER", transactionId, mpsRequest);

			log.info("Response register: " + responseCallSendOtp);

			Map<String, String> mResponse = mpsService.convertResponseOTP(responseCallSendOtp);
			Response response;
			int price = Integer.parseInt(mResponse.get("PRICE"));
			if(("100".equals(mResponse.get("RES")) || "0".equals(mResponse.get("RES"))) && price <= 0) {
				// Đăng ký lại trong ngày không mất tiền

				if(user.getFirstRegister() == null || !user.getFirstRegister()) {
					user.setFirstRegister(true);
					user.setTotalPlay(user.getTotalPlay() + 1000);

					// Đồng bộ icoin sang firebase
					zodiacGameFirebaseService.updateTotalIcoin(user.getId(), user.getTotalPlay());
				}

				user.setPremium(true);
				userService.saveUser(user);

				// Tạo 1 record để làm báo cáo doanh thu
				mpsRequest.setStatus("PROCESSED");
				response = utils.getResponseOK();

//				String content = "Mwiyandikishije neze mu gisata Roulette. Muce kuri  https://treasure.lumitel.bi kugira mukoreshe kino gisata.Murakoze";
//				String responseSendSms = mpsService.callApiSmsws(user.getPhone(), content);
//				log.info("Response send sms: " + responseSendSms);
			} else if("100".equals(mResponse.get("RES")) || "0".equals(mResponse.get("RES"))) {
				// Verify ok thì cộng lượt chơi cho user
				user.setFirstRegister(true);
				user.setTotalPlay(user.getTotalPlay() + 1000);
				user.setPremium(true);
				userService.saveUser(user);
				response = utils.getResponseOK();
				mpsRequest.setAmount(price);
				mpsRequest.setStatus("PROCESSED");

				// Đồng bộ icoin sang firebase
				zodiacGameFirebaseService.updateTotalIcoin(user.getId(), user.getTotalPlay());

//				String content = "Mwiyandikishije neze mu gisata Roulette. Muce kuri  https://treasure.lumitel.bi kugira mukoreshe kino gisata.Murakoze";
//				String responseSendSms = mpsService.callApiSmsws(user.getPhone(), content);
//				log.info("Response send sms: " + responseSendSms);

			} else if("408".equals(mResponse.get("RES"))){
				//Đã đăng ký sau đó hủy và đăng ký lại cùng ngày. Không bị mất tiền nên không cộng lượt chơi
				user.setPremium(true);
				userService.saveUser(user);
				response = utils.getResponseOK();
				mpsRequest.setStatus("PROCESSED");

			} else if("401".equals(mResponse.get("RES"))){
				//Ou pa gen ase lajan, Tanpri rechaje pou w ka achte plis vire : Không đủ tiền trong tài khoản, vui lòng nạp thêm
				response = new Response("FAILED", "Ubutunzi bwanyu ntibukwiye, Mushiremwo ama unite");
				mpsRequest.setStatus("FAILED");

			} else {
				response = utils.getResponseFailed();
				mpsRequest.setStatus("FAILED");
			}
			mpsService.save(mpsRequest);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			log.warn(e);
			return ResponseEntity.ok().body(utils.getResponseFailed());
		}
	}
	
	
	@PostMapping("/verify_register")
	public ResponseEntity<?> verifyRegister(@AuthenticationPrincipal UserDetails userDetails
											, @RequestBody VerifyOTPRequestMPS verifyOTPRequestMPS) {
		try {
			User user = userService.findByUserName(userDetails.getUsername());
			
			String responseCallSendOtp = mpsService.callApiVerifyOtp(user.getPhone(), "REGISTER", verifyOTPRequestMPS.getOtp(), user.getLastTransactionId());

			log.info("Response verify register: " + responseCallSendOtp);

			Map<String, String> mResponse = mpsService.convertResponseOTP(responseCallSendOtp);

			Response response;
			long price = Long.parseLong(mResponse.get("PRICE"));
			if(("100".equals(mResponse.get("RES")) || "0".equals(mResponse.get("RES"))) && price <= 0) {
				// Đăng ký lại trong ngày không mất tiền
				user.setPremium(true);
				userService.saveUser(user);
				
				// Tạo 1 record để làm báo cáo doanh thu
				mpsService.newMPSRequest(user, "REGISTER_WEB");
				response = utils.getResponseOK();
			} else if("100".equals(mResponse.get("RES")) || "0".equals(mResponse.get("RES"))) {
				// Verify ok thì cộng lượt chơi cho user
				user.setTotalPlay(user.getTotalPlay() + 1000);
				user.setPremium(true);
				userService.saveUser(user);
				response = utils.getResponseOK();

				// Đồng bộ icoin sang firebase
				zodiacGameFirebaseService.updateTotalIcoin(user.getId(), user.getTotalPlay());
			} else if("408".equals(mResponse.get("RES"))){
				//Đã đăng ký sau đó hủy và đăng ký lại cùng ngày. Không bị mất tiền nên không cộng lượt chơi
				user.setPremium(true);
				userService.saveUser(user);
				response = utils.getResponseOK();
			} else if("401".equals(mResponse.get("RES"))){
				
				//Ou pa gen ase lajan, Tanpri rechaje pou w ka achte plis vire : Không đủ tiền trong tài khoản, vui lòng nạp thêm
				response = new Response("FAILED", "Ubutunzi bwanyu ntibukwiye, Mushiremwo ama unite");
			} else if("415".equals(mResponse.get("RES")) || "416".equals(mResponse.get("RES"))){
				//Kod OTP a Espire, Tanpri eseye avek lot kod: OTP đã hết hạn, vui lòng thử lại mã khác
				response = new Response("FAILED", "Kode OTP siyo, saba iyindi");
			} else {
				response = utils.getResponseFailed();
			}

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			log.warn(e);
			return ResponseEntity.ok().body(utils.getResponseFailed());
		}
	}
	
	
	@PostMapping("/cancel")
	public ResponseEntity<?> cancel(@AuthenticationPrincipal UserDetails userDetails) {
		try {
			User user = userService.findByUserName(userDetails.getUsername());
			MPSRequest mpsRequest = mpsService.newMPSRequest(user, "CANCEL_WEB");

			String transactionId = utils.generateTransactionID();
			user.setLastTransactionId(transactionId);
			user = userService.saveUser(user);
			
			String responseCallSendOtp = mpsService.callApiSendOtp(user.getPhone(), "CANCEL", transactionId, mpsRequest);

			log.info("Response cancel: " + responseCallSendOtp);

			Map<String, String> mResponse = mpsService.convertResponseOTP(responseCallSendOtp);
			if("100".equals(mResponse.get("RES")) || "0".equals(mResponse.get("RES")) || "411".equals(mResponse.get("RES")) || "414".equals(mResponse.get("RES"))) {
				user.setPremium(false);
				userService.saveUser(user);

				mpsRequest.setStatus("PROCESSED");
				mpsService.save(mpsRequest);

//				String content = "Mwakuyemwo neza igisata ca Roulette. Fyonda *813*4# kugira mwiyandikishe kandi. Murakoze";
//				String responseSendSms = mpsService.callApiSmsws(user.getPhone(), content);
//				log.info("Response send sms: " + responseSendSms);

				return ResponseEntity.ok(utils.getResponseOK());
			} else {
				mpsRequest.setStatus("FAILED");
				mpsService.save(mpsRequest);
				return ResponseEntity.ok(utils.getResponseFailed());
			}


		} catch (Exception e) {
			log.warn(e);
			return ResponseEntity.ok().body(utils.getResponseFailed());
		}
	}
	
	
	@PostMapping("/verify_cancel")
	public ResponseEntity<?> verifyCancel(@AuthenticationPrincipal UserDetails userDetails
										, @RequestBody VerifyOTPRequestMPS verifyOTPRequestMPS) {
		try {
			User user = userService.findByUserName(userDetails.getUsername());
			
			String responseCallSendOtp = mpsService.callApiVerifyOtp(user.getPhone(), "CANCEL", verifyOTPRequestMPS.getOtp(), user.getLastTransactionId());

			log.info("Response verify cancel: " + responseCallSendOtp);

			Map<String, String> mResponse = mpsService.convertResponseOTP(responseCallSendOtp);

			

			Response response;
			if("100".equals(mResponse.get("RES")) || "0".equals(mResponse.get("RES")) || "411".equals(mResponse.get("RES")) || "414".equals(mResponse.get("RES"))) {
				// Hủy thành công thì set lại premium
				user.setPremium(false);
				userService.saveUser(user);
				
				// Tạo 1 record để làm báo cáo doanh thu
				mpsService.newMPSRequest(user, "CANCEL_WEB");
				response = utils.getResponseOK();
			} else if("415".equals(mResponse.get("RES")) || "416".equals(mResponse.get("RES"))){
				
				//Kod OTP a Espire, Tanpri eseye avek lot kod: OTP đã hết hạn, vui lòng thử lại mã khác
				response = new Response("FAILED", "Kode OTP siyo, saba iyindi ");
			} else {
				response = utils.getResponseFailed();
			}
			
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			log.warn(e);
			return ResponseEntity.ok().body(utils.getResponseFailed());
		}
	}
	
	
	@PostMapping("/charge")
	public ResponseEntity<?> charge(@AuthenticationPrincipal UserDetails userDetails) {
		try {
			User user = userService.findByUserName(userDetails.getUsername());
			// Tạo 1 record để làm báo cáo doanh thu
			MPSRequest mpsRequest = mpsService.newMPSRequest(user, "CHARGE");
			String responseCallSendOtp = mpsService.callApiCharge(user.getPhone(),"CHARGE" , "MASCOT");

			log.info("Response charge: " + responseCallSendOtp);

			Map<String, String> mResponse = mpsService.convertResponseOTP(responseCallSendOtp);

			Response response;
			int price = Integer.parseInt(mResponse.get("PRICE"));
			if("0".equals(mResponse.get("RES")) && price > 0) {
				// Charge ok thì cộng lượt chơi cho user
				user.setTotalPlay(user.getTotalPlay() + 1000);
				userService.saveUser(user);

				mpsRequest.setAmount(price);
				mpsRequest.setStatus("PROCESSED");
				response = utils.getResponseOK();

				// Đồng bộ icoin sang firebase
				zodiacGameFirebaseService.updateTotalIcoin(user.getId(), user.getTotalPlay());
			} else if("401".equals(mResponse.get("RES"))) {
				mpsRequest.setStatus("FAILED");
				//Ou pa gen ase lajan, Tanpri rechaje pou w ka achte plis vire 
				response = new Response("FAILED", "Ubutunzi bwanyu ntibukwiye, Mushiremwo ama unite ");
			} else {
				mpsRequest.setStatus("FAILED");
				response = utils.getResponseFailed("Ubutunzi bwanyu ntibukwiye, Mushiremwo ama unite.");
			}
			mpsService.save(mpsRequest);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			log.warn("BUGS", e);
			return ResponseEntity.ok().body(utils.getResponseFailed());
		}
	}

	@PostMapping("/_charge")
	public ResponseEntity<?> _charge(@AuthenticationPrincipal UserDetails userDetails) {
		try {
			User user = userService.findByUserName(userDetails.getUsername());

			// Charge ok thì cộng lượt chơi cho user
			user.setTotalPlay(user.getTotalPlay() + 1000);
			userService.saveUser(user);


			// Đồng bộ icoin sang firebase
			zodiacGameFirebaseService.updateTotalIcoin(user.getId(), user.getTotalPlay());

			// Tạo 1 record để làm báo cáo doanh thu
			mpsService.newMPSRequest(user, "CHARGE");
			Response response = utils.getResponseOK();

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			log.warn(e);
			return ResponseEntity.internalServerError().body(utils.getResponseFailed());
		}
	}

}
