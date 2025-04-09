package burundi.treasure.service;

import java.util.Date;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import burundi.treasure.model.User;
import burundi.treasure.repository.UserRepository;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class OTPService {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private MPSService mpsService;
	
	//Thời gian có hiệu lực của otp
    private final long OTP_EXPIRATION = 300000L; //5 phút
	
	public String generateOTP() {
		Random rnd = new Random();
		int number = rnd.nextInt(999999);
		String otp = String.format("%06d", number);
		return otp;
	}
	
	public Boolean sendOTP(User user) {
		try {
			Date currentTime = new Date();
			user = userRepository.getById(user.getId());
			
			String otp = generateOTP();
			System.out.println(otp);
			user.setOtp(otp);
			user.setExpiredOTP(new Date(currentTime.getTime() + OTP_EXPIRATION));
			userRepository.save(user);
			
			String content = "Your verification code is " + otp;
			String responseCallSmsws = mpsService.callApiSmsws(user.getPhone(), content);
			String responseCallSmsUssd = mpsService.callApiSmsUssd(user.getPhone(), content);

			return "0".equals(responseCallSmsws);
		} catch (Exception e) {
			log.warn("BUGS", e);
			return false;
		}

	}
}
