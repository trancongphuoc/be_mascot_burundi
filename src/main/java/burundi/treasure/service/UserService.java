package burundi.treasure.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import burundi.treasure.model.dto.UserDTO;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import burundi.treasure.common.Utils;
import burundi.treasure.model.User;
import burundi.treasure.payload.Response;
import burundi.treasure.repository.UserRepository;

@Service
@Log4j2
public class UserService {
	@Autowired
    private UserRepository userRepository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private Utils utils;

	public void incrementTotalStar(User user, Long totalStar) {
		if(user.getTotalStar() == null) {
			user.setTotalStar(totalStar);
		} else {
			user.setTotalStar(user.getTotalStar() + totalStar);
		}

		if(user.getTotalStarMonth() == null) {
			user.setTotalStarMonth(totalStar);
		} else {
			user.setTotalStarMonth(user.getTotalStarMonth() + totalStar);
		}
	}

	public void incrementTotalPlay(User user, Long totalPlay) {
		if(user.getTotalPlay() == null) {
			user.setTotalPlay(totalPlay);
		} else {
			user.setTotalPlay(user.getTotalPlay() + totalPlay);
		}
	}

	public void incrementTotalWin(User user, Long totalWin) {
		if(user.getTotalWin() == null) {
			user.setTotalWin(totalWin);
		} else {
			user.setTotalWin(user.getTotalWin() + totalWin);
		}
	}
	
	public User findByPhone(String phone) {
		try {
			return userRepository.findByPhone(phone);
		} catch (Exception e) {
			return null;
		}
	}

	public User findById(Long id) {
		return userRepository.findById(id).orElse(null);
	}
	
	public User findByUserName(String phone) {
		try {
			return userRepository.findByUsername(phone);
		} catch (Exception e) {
			return null;
		}
	}

	public List<User> findAllUser() {
		return userRepository.findAll();
	}

	public List<UserDTO> convertUserDTOForTop(List<User> users) {
		List<UserDTO> results = new ArrayList<>();
		for(User user: users) {
			if(user.getPhone().startsWith("257")) {
				UserDTO u = new UserDTO(user);
				if (u.getPhone() != null && u.getPhone().length() >= 4) {
					// Cắt chuỗi từ đầu đến ba ký tự cuối và thay thế bằng dấu ***
					u.setPhone(u.getPhone().substring(0, u.getPhone().length() - 3) + "***");
				}
				results.add(u);
			}
		}

		return results;
	}
	
	public Response authOTP(User user, String otp) {
		Response response = new Response("OK", "");
		Date currentTime = new Date();
		
		if(currentTime.after(user.getExpiredOTP())) {
			response.setStatus("FAILED");
			response.setMessage("OTP yaheze, nimusabe iyindi OTP");
		}else if(!user.getOtp().equals(otp)) {
			response.setStatus("FAILED");
			response.setMessage("Kode OTP siyo, subira muyisubizemwo");
		}
		
		return response;
	}
	
	public User createNewUserWithPhone(String phone) {
		User user = new User();
		user.setUsername(phone);
		user.setPassword(passwordEncoder.encode(phone));
		user.setPhone(phone);
		user.setAddTime(new Date());
		user.setExpiredOTP(new Date());
		user.setTotalPlay(0L);
		user.setTotalStar(0L);
		user.setPremium(false);
//		user.setLastTransactionId(utils.);

		System.out.println(user);
		return userRepository.save(user);
	}

	public User createNewUser(String username, String password) {
		log.info("CREATE NEW USER ADMIN");
		User user = new User();
		user.setUsername(username);
		user.setPassword(passwordEncoder.encode(password));
		user.setPhone("813");
		user.setAddTime(new Date());
		user.setExpiredOTP(new Date());
		user.setTotalPlay(0L);
		user.setTotalStar(0L);
		user.setTotalWin(0L);
		user.setTotalStarMonth(0L);

		user.setPremium(false);

		log.info(user);
		return userRepository.save(user);
	}
	public User createNewUserWithPhone(String phone, String from) {
		User user = new User();
		user.setUsername(phone);
		user.setPassword(passwordEncoder.encode(phone));
		user.setPhone(phone);
		user.setAddTime(new Date());
		user.setExpiredOTP(new Date());
		user.setTotalPlay(0L);
		user.setTotalStar(0L);
		user.setTotalStarMonth(0L);
		user.setTotalWin(0L);

		user.setPremium(false);
		user.setFromUser(from);
//		user.setLastTransactionId(utils.);

		System.out.println(user);
		return userRepository.save(user);
	}
	

	public void deleteUser(User user) {
		userRepository.delete(user);
	}
	
	public void resetTotalPlayAndTotalStar() {
		userRepository.resetTotalPlayAndTotalStar();
	}
	
	public void resetTotalPlayAndTotalStarById(Long userId) {
		userRepository.resetTotalPlayAndTotalStarById(userId);
	}

	public User saveUser(User user) {
		user.setLastUpdate(new Date());
		return userRepository.save(user);
	}

	public void resetTotalPlay() {
		userRepository.resetTotalPlay();
	}


	public List<User> getTop2UsersWithMaxTotalStar() {
		Pageable pageable = PageRequest.of(0, 2); // Lấy 2 phần tử đầu tiên
		return userRepository.findTop2UserByOrderByTotalStarDesc(pageable).toList();
	}

	public List<User> getTop50UsersWithMaxTotalStar() {
		Pageable pageable = PageRequest.of(0, 50); // Lấy 2 phần tử đầu tiên
		return userRepository.findTop50UserByOrderByTotalStarDesc(pageable).toList();
	}

	public List<User> getTop30UsersOrderByTotalWin() {
		Pageable pageable = PageRequest.of(0, 30); // Lấy 2 phần tử đầu tiên
		return userRepository.findTop2UserByOrderByTotalWinDesc(pageable).toList();
	}

	public Long sumTotalPlayByPhone(String msisdn) {
		return userRepository.sumTotalPlayByPhone(msisdn);
	}

	public List<Object[]> groupByUserPhoneAndSumTotalPlay(String phone) {
		return userRepository.groupByUserPhoneAndSumTotalPlay(phone);
	}

	public void resetTotalStar() {
		userRepository.resetTotalStar();
	}

	public Page<User> findAllByTotalStarGreaterThanAndPhoneContainingOrderByTotalStarDesc(Long totalStar, String phone, Pageable pageable) {
		return userRepository.findAllByTotalStarGreaterThanAndPhoneContainingOrderByTotalStarDesc(totalStar, phone, pageable);
	}

}
