package burundi.treasure.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

import burundi.treasure.model.Config;
import burundi.treasure.repository.ConfigRepository;
import burundi.treasure.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import burundi.treasure.common.Const;
import burundi.treasure.common.Utils;
import burundi.treasure.config.ConfigProperties;
import burundi.treasure.model.LuckyHistory;
import burundi.treasure.model.User;

import lombok.extern.log4j.Log4j2;

@RestController
@Log4j2
@RequestMapping("/api/admin")
public class AdminController {
    @Autowired
    private UserService userService;

    @Autowired
    private MPSService mpsService;

    @Autowired
    private Utils utils;

    @Autowired
    private LuckyService luckyService;

    @Autowired
    private AnypayService anypayService;
    @Autowired
    private ConfigProperties properties;

    @Autowired
    private LumicashService lumicashService;

    private final String key = "mpDalODlyypOYKwBIOfzCJETaOobzadtTFip";

    @GetMapping("/find_all_lucky_history")
    public ResponseEntity<?> getAllLuckyHistory() {
        return ResponseEntity.ok(luckyService.getAllHistories());
    }

    @GetMapping("/test_charge")
    public ResponseEntity<?> testCharge(@RequestParam String phone, @RequestParam String command, @RequestParam String cate) throws Exception {
    	String responseCallSendOtp = mpsService.callApiCharge(phone, command , cate);

		log.info("Response charge: " + responseCallSendOtp);

		Map<String, String> mResponse = mpsService.convertResponseOTP(responseCallSendOtp);
        return ResponseEntity.ok(mResponse);
    }

    @GetMapping("/test")
    public void test() throws Exception {
        String s = mpsService.decryptResponseOTP("DATA=K4zGXsoEZgHHje2CQfKPJI+D3+PJn5QTOR/Bxrx2d3ZU7bAR+BhZKgXir1JV53wGSPKOjr21E0h+yg5U2emnGike8Thu0WB7xbZCAVDgd6ADUd7NdRpNkVUz2eid37CLtGf6QIceI1DtpdYmUm6U6l3CRjxWIGWmZTiAQo3NWHpJMSvlmg71aX89aPpPibeiHjW7CQFVAghu+bTjDdT5qUDS/joQgYWE6YxhbH9gH+GzQA6dJmxoIthjr79x3DQQrza5Mxu5M72jCMZ1ChxLmkSIj46XQeRkcOZ9WrqSEPDZK0Pq8W65OIUB/xIEUDKmdKey6eqHGjR3+tW4cZh6xNPlHRQ8C/dhW7h7VeuenA1P0yhPz8UqfLbZy6q3piVVE/frz+s4ABvvz+osJH2fiQ2h2kK9kYT2F/k2UnwI//rzFJOs0h7C9bl02wb1ZPEm/xaQF77xxN47PUxTlHBCUqnoStdnrBwj0nwmK4V02+Vdj9K6dztGAkQeVMe2vflo5m9+22060ndyxiI55XVFQ/isNWl/Rkm7uoj7O94230syoPcyGkW9Q2hC89+HgpJIEaeP3JBOR1CZSGZv8p3Gh2ZJA5PgT91eU9CeeSjJEUtGo52NkITvEyFrGfqRgAetVNv1YUkb5BPPdH1sjupN72qu0cfG/e9DXE8XeXdNfkc=&SIG=CsEt6pQdCqMeGpteZ3%2FE2gHeyQ3Hz4RmmtK1uPilnygBNzTBnW7v1f%2Bh6hkS1G%2Bm2j637t3Iz17ZZcmiCKWjdYAxack9qdKmLelUUfryUFrTkifU%2B%2BEf2oheiAiIxHjVF6g62fop2dFwhwGunWU%2FSt%2B%2FsKdlCUbFoph2ZrM046EuHHoW%2F%2BBZnHEaRlA9vYHt%2FNqOclmseVqHOL4y2XqcSIZ2A6iWDDwwqYAe%2FC7FOn97e%2BqGb7f8TcmBtJTAxbFVCtanxtryuP8KarDvFA14zghfgXnVpG2hqjU%2BCB7JL5aykZkSnOR9Id5Hcs6SMSWqDDgudiKmtNZo2g71PSmUXp0pFSOUTI7qw73Ln%2FrhzJcwXuN379iceCgJF0AsR%2FaJMbgkbI%2FisNFmLQTCy0eUYuPFNV7BKFJmBvA7DlSgkogsrBc6%2FjleM1K%2F3pZxz9u1avMeWIN9Fi7WIEWUmyPwz2haP4U6cBlbp9sUiFVUd9DBj7JQ65tIeueWuXNmS%2BW17TywWcp9FOupXfdt5JctT6qq38XYEz3KeNyj1A0%2BsLbCT3JDdEO3iExv2kpzaywVENHv5%2BREFaHkH8alwD2C2ManUFg9oFDxZcTY%2BJ%2F4O7kWD1S%2F3xCxo7c1FjuZ7NyuP8gt0jsbzUwWmZQekkJ9QEF1CaqW%2FfNsc7AexjCpYHA%3D");
        System.out.println(s);
    }


    @GetMapping("/get_user")
    public ResponseEntity<?> getUser(@RequestParam String phone) {
        phone = utils.formatPhoneBurundi(phone);
        User user = userService.findByPhone(phone);

        return ResponseEntity.ok(user);
    }

    @GetMapping("/set_user")
    public ResponseEntity<?> getUser(@RequestParam String phone, @RequestParam Boolean premium) {
        phone = utils.formatPhoneBurundi(phone);
        User user = userService.findByPhone(phone);
        user.setPremium(premium);
        userService.saveUser(user);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/send_sms_async")
    public ResponseEntity<?> getUser(@RequestParam String phone, @RequestParam String message) throws Exception {
        mpsService.callApiSmswsAsync(phone, message);
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/delete_mps_request")
    public ResponseEntity<?> deleteMpsRequest() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -30);
        Date chargetTime = calendar.getTime();

        mpsService.deleteByChargetTimeBefore(chargetTime);
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/delete_mps_lucky_history")
    public ResponseEntity<?> deleteLuckyHistory() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -30);
        Date chargetTime = calendar.getTime();

        luckyService.deleteByAddTimeBefore(chargetTime);
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/clear_mps_request")
    public ResponseEntity<?> clearMpsRequest(@RequestParam Integer day) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, - day);
        Date chargetTime = calendar.getTime();

        mpsService.deleteByChargetTimeBefore(chargetTime);
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/clear_mps_lucky_history")
    public ResponseEntity<?> clearLuckyHistory(@RequestParam Integer day) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, - day);
        Date chargetTime = calendar.getTime();

        luckyService.deleteByAddTimeBefore(chargetTime);
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/get_size_db")
    public ResponseEntity<?> getSizeDb() throws IOException {
    	String serverNumberFile = "./data/iluckydb.mv.db";
    	File file = new File(serverNumberFile); // Replace "example.txt" with the path to your file
    	long fileSize = 0;
        if(file.exists()) {
            fileSize = file.length();
            System.out.println("File size: " + fileSize + " bytes");
        } else {
            System.out.println("File does not exist.");
        }
        return ResponseEntity.ok(fileSize);
    }

    @GetMapping("/find_all_lucky_history_by_phone")
    public ResponseEntity<?> history(@RequestParam String phone) {
        try {
            User user = userService.findByPhone(phone);
            List<LuckyHistory> luckyHistories = luckyService.getHistoriesByUserId(user.getId());
            return ResponseEntity.ok(luckyHistories);
        } catch (Exception e) {
            log.warn(e);
            return ResponseEntity.internalServerError().body(new ArrayList<>());
        }
    }

//    @GetMapping("/find_all_lucky_history")
//    public ResponseEntity<?> getAllLuckyHistory() {
//        try {
//            List<LuckyHistory> luckyHistories = luckyService.getAllHistories();
//            return ResponseEntity.ok(luckyHistories);
//        } catch (Exception e) {
//            log.warn(e);
//            return ResponseEntity.internalServerError().body(new ArrayList<>());
//        }
//    }

    @GetMapping("/find_all_user")
    public ResponseEntity<?> getAllUser() {
        try {
            return ResponseEntity.ok(userService.findAllUser());
        } catch (Exception e) {
            log.warn(e);
            return ResponseEntity.internalServerError().body(new ArrayList<>());
        }
    }

    @GetMapping("/find_mps_request")
    public ResponseEntity<?> findAllMpsRequest() {
        try {
            return ResponseEntity.ok(mpsService.findAllMPSRequest());
        } catch (Exception e) {
            log.warn(e);
            return ResponseEntity.internalServerError().body(new ArrayList<>());
        }
    }

//    @GetMapping("/register_account")
//    public ResponseEntity<?> registerAccount() {
//    	String s = "+509";
//
//    	List<String> phone = Arrays.asList("87654322","32924892", "40584943", "42731980", "41452422", "43537085", "42437512", "42996459", "35381446");
//    	User user = userService.createNewUserWithPhone(s+ "87654322");
//        return ResponseEntity.ok(user);
//    }

//    @GetMapping("/mps_request_delete_all")
//    public ResponseEntity<?> deleteAllMpsRequest() {
//    	mpsService.deleteAll();
//        return ResponseEntity.ok("OK");
//    }

    @GetMapping("/register_phone")
    public ResponseEntity<?> registerPhone(@RequestParam String phone) {
    	String res = "OK";
        User user = userService.findByPhone("+509" + phone);
        if(user == null) {
        	user = userService.createNewUserWithPhone("+509" + phone);
        	res += "_NEW";
        }

    	user.setTotalPlay(user.getTotalPlay() + 5);
		user.setPremium(true);
		userService.saveUser(user);

        User userDelete = userService.findByPhone("+509+" + phone);
        if(userDelete != null) {
        	userService.deleteUser(userDelete);
        	res += "_DELETE";
        }


        return ResponseEntity.ok(res);
    }

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/createnewuseradmin")
    public ResponseEntity<?> createNewUserAdmin(@RequestParam String username, @RequestParam String password, @RequestParam String key) {
        if(key.equals(this.key)) {
            log.info("createNewUserAdmin!!!");
            User user = userService.findByUserName(username);
            if(user == null) {
                userService.createNewUser(username, password);
            } else {
                user.setPassword(passwordEncoder.encode(password));
                userService.saveUser(user);
            }

            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.ok("FAILED");
        }
    }

    @GetMapping("/logs")
    public ResponseEntity<?> logs() throws IOException {
    	String serverNumberFile = "./logs/app.log";
        String serverNumber = new String(Files.readAllBytes(Paths.get(serverNumberFile)));

        return ResponseEntity.ok(serverNumber);
    }

    @GetMapping("/resetTotalPlay")
    public ResponseEntity<?> resetTotalPlay() throws IOException {
        userService.resetTotalPlay();
        return ResponseEntity.ok("OK");
    }


    @GetMapping("/setTotalPlay")
    public ResponseEntity<?> setTotalPlay(@RequestParam String phone, @RequestParam Long totalPlay) throws IOException {
        phone = utils.formatPhoneBurundi(phone);

        User user = userService.findByPhone(phone);
        user.setTotalPlay(totalPlay);
        userService.saveUser(user);

        return ResponseEntity.ok(user);
    }

    @GetMapping("/setTotalStar")
    public ResponseEntity<?> setTotalStar(@RequestParam String phone, @RequestParam Long totalStar) throws IOException {
        phone = utils.formatPhoneBurundi(phone);

        User user = userService.findByPhone(phone);
        user.setTotalStar(totalStar);
        userService.saveUser(user);

        return ResponseEntity.ok(user);
    }

    @GetMapping("/warning")
    public ResponseEntity<?> warning(@RequestParam(defaultValue = "") String t) throws Exception {
        log.info("MyScheduledTasks.warning");
    	// Lấy thời gian bắt đầu của khoảng thời gian 24 giờ gần nhất
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -24);
        Date startTime = calendar.getTime();

        long totalHtgWin = luckyService.sumNoItemByGiftTypeHTGWithin24Hours(startTime);
        log.info("Win: " + totalHtgWin);

        long totalHtgFee = mpsService.sumAmountWithin24Hours(startTime);
        log.info("Fee: " + totalHtgFee);

        double totalHtgFee25Percentage = totalHtgFee * 0.25; // 25% của totalHtgFee

        if(!t.isEmpty()) {
            String responseSmsws = mpsService.callApiSmsws("+50940677800", "WARNING: Tổng giải thưởng đang vượt quá 25% doanh thu!!!!!");
            log.info(responseSmsws);
        }

        if (totalHtgWin > totalHtgFee25Percentage) {
            // totalHtg lớn hơn 25% của totalHtgFee
            log.info("totalHtg lớn hơn 25% của totalHtgFee");
        } else {
            // totalHtg không lớn hơn 25% của totalHtgFee
        	log.info("totalHtg không lớn hơn 25% của totalHtgFee");
        }
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/readfile")
    public ResponseEntity<?> readFile() throws IOException {
    	String serverNumberFile = "test.txt";
        String serverNumber = new String(Files.readAllBytes(Paths.get(serverNumberFile)));
        return ResponseEntity.ok(serverNumber);
    }


    @GetMapping("/writefile")
    public ResponseEntity<?> writeFile(@RequestParam(defaultValue = "") String text) throws IOException {
    	BufferedWriter writer = new BufferedWriter(new FileWriter("test.txt", false));
    	writer.append(text);
        writer.close();

        String serverNumberFile = "test.txt";
        String serverNumber = new String(Files.readAllBytes(Paths.get(serverNumberFile)));

        return ResponseEntity.ok(serverNumber);
    }

    @GetMapping("/readfilecdr")
    public ResponseEntity<?> readFilecdr() throws IOException {
    	String serverNumberFile = "testcdr.txt";
        String serverNumber = new String(Files.readAllBytes(Paths.get(serverNumberFile)));
        return ResponseEntity.ok(serverNumber);
    }


    @GetMapping("/writefilecdr")
    public ResponseEntity<?> writeFilecdr(@RequestParam(defaultValue = "true") String text) throws IOException {
    	BufferedWriter writer = new BufferedWriter(new FileWriter("testcdr.txt", false));
    	writer.append(text);
        writer.close();

        String serverNumberFile = "test.txt";
        String serverNumber = new String(Files.readAllBytes(Paths.get(serverNumberFile)));

        return ResponseEntity.ok(serverNumber);
    }

//    @GetMapping("/setiswin")
//    public ResponseEntity<?> setIsWin(@RequestParam(defaultValue = "") String phone
//    								, @RequestParam(defaultValue = "true") String win) throws IOException {
//    	phone = utils.formatPhoneHaiti(phone);
//    	User user = userService.findByPhone(phone);
//
//    	user.setWin(Boolean.parseBoolean(win));
//    	userService.saveUser(user);
//
//        return ResponseEntity.ok("OK");
//    }

    @Autowired
    private ConfigRepository configRepository;

    @PostMapping("/hello")
    public String helloPost() throws UnknownHostException {
        InetAddress inetAddress = InetAddress.getLocalHost();
        log.info(inetAddress.getHostAddress());
        log.info(inetAddress.toString());

        String nodeReward = configRepository.findById("SERVER_REWARD").map(Config::getCodeValue).orElse(null);
        if(nodeReward == null) {
            nodeReward = "10.225.10.118";
            configRepository.save(new Config("SERVER_REWARD", nodeReward));
            return "NEW";
        }

        if(inetAddress.getHostAddress().equals(nodeReward)) {
            return "OK";
        } else {
            return "NO OK";
        }
    }

    @GetMapping("/shutdown")
    public void shutdown(@RequestParam String key) {
    	if(key.equals(this.key)) {
        	log.info("SHUTDOWN!!!");
            System.exit(0);
    	}
    }

    @GetMapping("/pause")
    public String pause(@RequestParam String key) {
    	if(key.equals(this.key)) {
        	log.info("PAUSE!!!");
            Const.PAUSE_GAME = true;
            return "OK";
    	} else {
            return "FAILED";
    	}
    }

    @GetMapping("/send-sms")
    public String sendSms(@RequestParam String phone, @RequestParam String content) throws Exception {
        phone = utils.formatPhoneBurundi(phone);
        return mpsService.callApiSmsws(phone, content);
    }

    @GetMapping("/resume")
    public String resume(@RequestParam String key) {
    	if(key.equals(this.key)) {
        	log.info("RESUME!!!");
            Const.PAUSE_GAME = false;
            return "OK";
    	} else {
            return "FAILED";
    	}
    }

    @GetMapping("/is-run")
    public String isRun() {
        return String.valueOf(Const.IS_REWARD);
    }

    @GetMapping("/set-reward")
    public String setReward(@RequestParam boolean reward) {
        Const.setIsReward(reward);
        return String.valueOf(Const.IS_REWARD);
    }

    @GetMapping("/resetTotalStar")
    public ResponseEntity<?> resetTotalStar() throws IOException {
        userService.resetTotalStar();
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/hello")
    public String hello() throws UnknownHostException {
        InetAddress inetAddress = InetAddress.getLocalHost();
        log.info(inetAddress.getHostAddress());
        log.info(inetAddress.toString());

        String nodeReward = configRepository.findById("SERVER_REWARD").map(Config::getCodeValue).orElse(null);
        if(nodeReward == null) {
            nodeReward = "10.225.10.118";
            configRepository.save(new Config("SERVER_REWARD", nodeReward));
            return "NEW";
        }

        if(inetAddress.getHostAddress().equals(nodeReward)) {
            return "OK";
        } else {
            return "NO OK";
        }
    }

    @GetMapping("/hello2")
    public String hello2() {
        return "Hello";
    }


    @GetMapping("/mod")
    public ResponseEntity<?> _charge(@RequestParam(defaultValue = "69000555") String phone
            , @RequestParam Long money) {
        try {
            User u = userService.findByPhone(utils.formatPhoneBurundi(phone));

            if(u == null) {
                u = userService.createNewUserWithPhone(phone, "TEST");
            }

            String rs = lumicashService.callWsClient(phone, money);
            log.info(rs);
            Date date = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy");
            String formattedDate = formatter.format(date);

            // Gửi tin nhắn trúng thưởng
        	String content = "Turabakeje!Uri umwe mubaronse inyenyeri nyinshi kuwa " +formattedDate+". Mufise inyenyeri " + u.getTotalStar()+ ". Mwaronse "+money+"F";
        	String responseSendSms = mpsService.callApiSmsws(phone, content);
        	log.info("Response send sms: " + responseSendSms);
            return ResponseEntity.ok(rs);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ResponseEntity.ok("OK");

    }

    @GetMapping("/rewardTop2TotalStar") // Chạy vào lúc 8 giờ sáng mỗi ngày
    public String rewardTop2TotalStar(@RequestParam String key) throws Exception {
        if(!key.equals(this.key)) {
            return "FAILED";
        }
        log.info("rewardTop2TotalStar");

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1); // Lấy tháng trước
        Date date = calendar.getTime();

        SimpleDateFormat formatter = new SimpleDateFormat("MM/yyyy");
        String formattedDate = formatter.format(date);

        log.info("MyScheduledTasks.rewardTop2TotalStar");
        List<User> users = userService.getTop3UsersWithMaxTotalStarMonth();
        log.info(users);
        String content = "Turabakeje!Uri umwe mubaronse inyenyeri nyinshi kuwa %s. Mufise inyenyeri %s. Mwaronse %sF";

        if(users.size() > 2) {
            log.warn("Số lượng user nhận quà lớn hơn 2");
            return "Số lượng user nhận quà lớn hơn 2";
        } else if(Const.PAUSE_GAME) {
            log.warn("Hệ thống đang bận.");
            return "Hệ thống đang bận.";
        }

//        for(int i = 0; i < 2 && i < users.size(); i++) {
//            Gift gift;
//            if(i == 0) {
//                gift = GiftService.gifts.get("FBU_VIP_1");
//            } else {
//                gift = GiftService.gifts.get("FBU_VIP_2");
//            }
//            User user = users.get(i);
//            if(user.getTotalWin() == null) {
//                user.setTotalWin(gift.getNoItem());
//            } else {
//                user.setTotalWin(user.getTotalWin() + gift.getNoItem());
//            }
//            LuckyHistory luckyHistory = new LuckyHistory();
//
//            String lumicashRequest = lumicashService.generateRequestBody(user.getPhone(), gift.getNoItem());
//            String lumicashResponse = lumicashService.callWsClient(lumicashRequest);
//            try {
//                lumicashRequest = StringEscapeUtils.unescapeHtml4(lumicashRequest);
//                lumicashResponse = StringEscapeUtils.unescapeHtml4(lumicashResponse);
//                luckyHistory.setLumicashRequest(lumicashRequest);
//                luckyHistory.setLumicashResponse(lumicashResponse);
//
//                if(lumicashService.isPayOk(lumicashResponse)) {
//                    luckyHistory.setStatus("PROCESSED");
//                } else {
//                    luckyHistory.setStatus("FAILED");
//                }
//            } catch (Exception e) {
//                luckyHistory.setStatus("ERROR");
//                log.warn(e);
//            }
//            content = String.format(content, formattedDate, user.getTotalStar(), gift.getNoItem());
//            String responseSendSms = mpsService.callApiSmsws(user.getPhone(), content);
//            log.info(responseSendSms);
//
//            user.setWin(true);
//            userService.saveUser(user);
//
//            luckyHistory.setGiftType(gift.getType());
//            luckyHistory.setAddTime(new Date());
//            luckyHistory.setGiftId(gift.getId());
//            luckyHistory.setNoItem(gift.getNoItem());
//            luckyHistory.setUser(user);
//            luckyService.saveLuckyGiftHistory(luckyHistory);
//
//        }

        userService.resetTotalStar();
        return "OK";
    }

    @PostMapping("/addLuckyHistory")
    public ResponseEntity<?> addLuckyHistory(@RequestBody LuckyHistory luckyHistory, @RequestParam String msisdn) {
        String phone = utils.formatPhoneBurundi(msisdn);
        User user = userService.findByPhone(phone);

        luckyHistory.setUser(user);
        luckyService.saveLuckyGiftHistory(luckyHistory);

        return ResponseEntity.ok(luckyHistory);
    }



    @Value("${log.folder.path}")
    private String logFolderPath;

    @GetMapping("/logs-download")
    public ResponseEntity<FileSystemResource> downloadLogFile(@RequestParam(defaultValue = "app.log") String fileName) {
        File logFile = new File(logFolderPath, fileName);

        // Kiểm tra nếu file không tồn tại
        if (!logFile.exists() || logFile.isDirectory()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        // Trả về file với header để download
        FileSystemResource resource = new FileSystemResource(logFile);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
    public static void main(String[] args) {



	}
}
