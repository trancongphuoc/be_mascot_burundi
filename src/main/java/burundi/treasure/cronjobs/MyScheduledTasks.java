package burundi.treasure.cronjobs;

import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import burundi.treasure.model.Config;
import burundi.treasure.repository.ConfigRepository;
import burundi.treasure.service.*;
import burundi.treasure.service.zodiacgame.ZodiacGameService;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import burundi.treasure.common.Const;
import burundi.treasure.model.Gift;
import burundi.treasure.model.LuckyHistory;
import burundi.treasure.model.User;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class MyScheduledTasks {

	@Autowired
	private UserService userService;

	@Autowired
	private MPSService mpsService;

	@Autowired
	private LuckyService luckyService;

    @Autowired
    private ConfigRepository configRepository;

    @Autowired
    private LumicashService lumicashService;

    @Autowired
    private ZodiacGameService zodiacGameService;

	@Scheduled(cron = "0 0 0 * * ?") // Chạy vào lúc 0 giờ mỗi ngày
    public void resetTotalPlay() {
        log.info("MyScheduledTasks.resetTotalPlay");
        userService.resetTotalPlay();

        log.info("ResetNoGameToday");
        zodiacGameService.resetNoGameToday();
    }


//    @Scheduled(cron = "0 0 8 1 * ?")
//    public void rewardTop2TotalStar() throws Exception {
//        log.info("rewardTop2TotalStar");
//        InetAddress inetAddress = InetAddress.getLocalHost();
//        String nodeReward = configRepository.findById("SERVER_REWARD").map(Config::getCodeValue).orElse(null);
//        if(nodeReward == null) {
//            nodeReward = "10.225.10.118";
//            configRepository.save(new Config("SERVER_REWARD", nodeReward));
//        }
//        if(!inetAddress.getHostAddress().equals(nodeReward)) {
//            return;
//        }
//        log.info("start");
//
//        Calendar calendar = Calendar.getInstance();
//        calendar.add(Calendar.MONTH, -1); // Lấy tháng trước
//        Date date = calendar.getTime();
//
//        SimpleDateFormat formatter = new SimpleDateFormat("MM/yyyy");
//        String formattedDate = formatter.format(date);
//
//        log.info("MyScheduledTasks.rewardTop2TotalStar");
//        List<User> users = userService.getTop2UsersWithMaxTotalStar();
//        log.info(users);
//    	String content = "Turabakeje!Uri umwe mubaronse inyenyeri nyinshi kuwa %s. Mufise inyenyeri %s. Mwaronse %sF";
//
//        if(users.size() > 2) {
//        	log.warn("Số lượng user nhận quà lớn hơn 2");
//        	return;
//        } else if(Const.PAUSE_GAME) {
//        	log.warn("Hệ thống đang bận.");
//        	return;
//        }
//
//        for(int i = 0; i < 2 && i < users.size(); i++) {
//            Gift gift;
//            if(i == 0) {
//                gift = GiftService.gifts.get("FBU_VIP_1");
//            } else {
//                gift = GiftService.gifts.get("FBU_VIP_2");
//            }
//            User user = users.get(i);
//        	if(user.getTotalWin() == null) {
//        		user.setTotalWin(gift.getNoItem());
//        	} else {
//        		user.setTotalWin(user.getTotalWin() + gift.getNoItem());
//        	}
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
//        	String responseSendSms = mpsService.callApiSmsws(user.getPhone(), content);
//        	log.info(responseSendSms);
//
//        	user.setWin(true);
//        	userService.saveUser(user);
//
//        	luckyHistory.setGiftType(gift.getType());
//        	luckyHistory.setAddTime(new Date());
//            luckyHistory.setGiftId(gift.getId());
//            luckyHistory.setNoItem(gift.getNoItem());
//            luckyHistory.setUser(user);
//            luckyService.saveLuckyGiftHistory(luckyHistory);
//
//        }
//
//        userService.resetTotalStar();
//    }
//
    @Scheduled(cron = "0 */5 * * * *") // Chạy mỗi 5 phút
    public void warning() throws Exception {
        log.info("MyScheduledTasks.warning");
        // Lấy thời gian bắt đầu của khoảng thời gian 24 giờ gần nhất
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -24);
        Date startTime = calendar.getTime();

        long totalWin = luckyService.sumMoneyByGiftTypeHTGWithin24Hours(startTime);
        log.info("Win: " + totalWin);

        long totalFee = mpsService.sumAmountWithin24Hours(startTime);
        log.info("Fee: " + totalFee);

        double totalHtgFee25Percentage = totalFee * 0.30; // 30% của total FBU


        if (totalWin > totalHtgFee25Percentage) {
            // set lại biến để ngừng trả thưởng
            Const.setIsReward(false);
            // totalHtg lớn hơn 15% của totalHtgFee
            log.info("totalWin lớn hơn 45% của totalFee");
            String content = "WARNING: The total prize (%sFbu) is exceeding 30 percent of the revenue (%sFbu)!!!!!";
            String warningMsisdns = configRepository.findById("WARNING_MSISDN").map(Config::getCodeValue).orElse("+25769000555");
            String[] msisdns = warningMsisdns.split(",");
//            SendSmsService.sendSMS(content, msisdns);
        } else {
            Const.setIsReward(true);
            // totalHtg không lớn hơn 15% của totalHtgFee
            log.info("totalHtg không lớn hơn 15% của totalHtgFee");
        }

    }
//
//    @Scheduled(fixedDelay = 1000 * 60 * 5) // Chạy mỗi 5 phút
//    public void checkSMSandMB() throws Exception {
//        log.info("MyScheduledTasks.checkSMSandMB");
//     // Lấy thời gian bắt đầu của khoảng thời gian 24 giờ gần nhất
//        Calendar calendar = Calendar.getInstance();
//        calendar.add(Calendar.HOUR_OF_DAY, -24);
//        Date startTime = calendar.getTime();
//
//        long totalSMS = luckyService.sumNoItemByGiftTypeWithin24Hours(startTime, "SMS");
//        long totalMB = luckyService.sumNoItemByGiftTypeWithin24Hours(startTime, "MB");
//
//        if(totalSMS > 100000 || totalMB > 100000) {
//            log.info("totalSMS > 100000 || totalMB > 100000");
//            userService.resetTotalPlayBonus(0L);
//        }
//    }
//
//    @Scheduled(fixedDelay = 1000 * 60 * 5) // Chạy mỗi 5 phút
//    public void deleteMpsRequestRenewFail() throws Exception {
//        log.info("MyScheduledTasks.deleteMpsRequestRenewFail");
//    	// Lấy thời gian bắt đầu của khoảng thời gian 24 giờ gần nhất
//        Calendar calendar = Calendar.getInstance();
//        calendar.add(Calendar.HOUR_OF_DAY, -24);
//        Date chargetTime = calendar.getTime();
//
//        mpsService.deleteByActionAndParamsAndChargetTimeBefore("RENEW", 1, chargetTime);
//    }
//
//
//    @Scheduled(cron = "0 */30 * * * *") // Chạy mỗi 5 phút
//    public void deleteNohupFile() throws Exception {
//        log.info("MyScheduledTasks.deleteNohupFile");
//        String filePath = "nohup.out";
//
//        // Tạo đối tượng File
//        File file = new File(filePath);
//
//        // Kiểm tra xem tệp tồn tại trước khi xóa
//        if (file.exists()) {
//            // Thực hiện xóa
//            if (file.delete()) {
//                log.info("File deleted successfully.");
//            } else {
//            	log.info("Failed to delete the file.");
//            }
//        } else {
//        	log.info("File does not exist.");
//        }
//    }
//
//
//    @Scheduled(fixedDelay = 1000 * 60 * 5) // Chạy mỗi 5 phút
//    public void crawCdr() {
//        log.info("MyScheduledTasks.crawCdr");
//    	if(!cdrService.run()) {
//            log.info("MyScheduledTasks.crawCdr: RETURN");
//    		return;
//    	} else {
//            log.info("MyScheduledTasks.crawCdr: RUN");
//    	}
//    	try {
//    		cdrService.retrieveFilesData();
//    		cdrService.retrieveFilesBalance();
//    	} catch (Exception e) {
//			log.warn(e);
//		}
//    }
//
//    @Scheduled(fixedDelay = 1000 * 60 * 5) // Chạy mỗi 5 phút
//    public void deleteCdr() {
//        log.info("MyScheduledTasks.deleteCdr");
//    	if(!cdrService.run()) {
//            log.info("MyScheduledTasks.deleteCdr: RETURN");
//    		return;
//    	} else {
//            log.info("MyScheduledTasks.deleteCdr: RUN");
//    	}
//    	try {
//    		cdrService.checkData();
//    		cdrService.checkBalance();
//    	} catch (Exception e) {
//			log.warn(e);
//		}
//    }
//
//    @Scheduled(fixedDelay = 1000 * 60 * 5) // Chạy mỗi 5 phút
//    public void handleLowData() {
//        log.info("MyScheduledTasks.handleLowData");
//    	if(!cdrService.run()) {
//            log.info("MyScheduledTasks.handleLowData: RETURN");
//    		return;
//    	} else {
//            log.info("MyScheduledTasks.handleLowData: RUN");
//    	}
//    	try {
//    		cdrService.processData();
//    	} catch (Exception e) {
//			log.warn(e);
//		}
//    }
//
//    @Scheduled(fixedDelay = 1000 * 60 * 5) // Chạy mỗi 5 phút
//    public void handleLowBalance() {
//        log.info("MyScheduledTasks.handleLowBalance");
//    	if(!cdrService.run()) {
//            log.info("MyScheduledTasks.handleLowBalance: RETURN");
//    		return;
//    	} else {
//            log.info("MyScheduledTasks.handleLowBalance: RUN");
//    	}
//    	try {
//    		cdrService.processBalance();
//    	} catch (Exception e) {
//			log.warn(e);
//		}
//    }
//
//    @Scheduled(fixedDelay = 1000 * 60 * 60) // Chạy mỗi 60 phút
//    public void clearOldDate() {
//        log.info("MyScheduledTasks.clearOldDate");
//    	try {
//    		 long twoDaysAgoMillis = System.currentTimeMillis() - 1L * 24 * 60 * 60 * 1000;
//    	        Date twoDaysAgo = new Date(twoDaysAgoMillis);
//
//    	        Iterator<Map.Entry<String, Date>> iterator = CDRService.smsSentMap.entrySet().iterator();
//    	        while (iterator.hasNext()) {
//    	            Map.Entry<String, Date> entry = iterator.next();
//    	            if (entry.getValue().before(twoDaysAgo)) {
//    	                iterator.remove();
//    	                log.info("MyScheduledTasks.remove: " + entry.getKey());
//    	            }
//    	        }
//    	} catch (Exception e) {
//			log.warn(e);
//		}
//    }
}
