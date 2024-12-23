package burundi.treasure.service;

import burundi.treasure.common.Utils;
import burundi.treasure.model.Gift;
import burundi.treasure.model.LuckyHistory;
import burundi.treasure.model.User;
import burundi.treasure.model.dto.LuckyHistoryDTO;
import burundi.treasure.repository.LuckyHistoryRepository;
import burundi.treasure.repository.UserRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Log4j2
public class LuckyService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LuckyHistoryRepository luckyHistoryRepository;
    
    @Autowired
    private MPSService mpsService;

    @Autowired
    private LumicashService lumicashService;

    @Autowired
    private Utils utils;

    public List<LuckyHistory> getAllHistories() {
        return  luckyHistoryRepository.findAll();
    }

    public List<LuckyHistory> getHistoriesByUserId(long userId) {
        return luckyHistoryRepository.findByUserIdOrderByAddTimeDesc(userId);
    }

    public List<LuckyHistory> getHistoriesByUserIdLimit100(long userId) {
        Pageable pageable = PageRequest.of(0, 100); // Trang đầu tiên, giới hạn 100 bản ghi
        return luckyHistoryRepository.findByUserIdOrderByAddTimeDesc(userId, pageable);
    }

    public List<LuckyHistory> getHistoriesByUserIdAndGiftType(long userId, String giftType) {
        return luckyHistoryRepository.findByUserIdAndGiftTypeOrderByAddTimeDesc(userId, giftType);
    }

    public List<LuckyHistory> findByUserIdAndGiftTypeAfterStartOfDay(long userId, String giftType) {
        return luckyHistoryRepository.findByUserIdAndGiftTypeAfterStartOfDay(userId, giftType, utils.getStartOfDay());
    }

    public List<LuckyHistory> getShareHistories(String giftId) {
        return luckyHistoryRepository.findByGiftIdAndIsShareFalse(giftId);
    }

    public boolean plusPlay(User user) {
        boolean result = false;

        List<LuckyHistory> luckyHistories = luckyHistoryRepository.findByGiftIdAndIsShareFalse("SHARE_PLUS");
        log.info(luckyHistories);
        for(LuckyHistory luckyHistory: luckyHistories) {
            luckyHistory.setIsShare(true);
            luckyHistoryRepository.save(luckyHistory);

            user.setTotalPlay(user.getTotalPlay() + 1);
            userRepository.save(user);

            result = true;
        }

        return result;
    }

    public void saveLuckyGiftHistory(LuckyHistory luckyHistory) {
    	luckyHistoryRepository.save(luckyHistory);
    }

    public void saveAll(List<LuckyHistory> luckyHistories) {
        luckyHistoryRepository.saveAll(luckyHistories);
    }
    public long sumNoItemByGiftTypeWithin24Hours(Date startTime, String type) {
    	return luckyHistoryRepository.sumNoItemByGiftTypeWithin24Hours(startTime, type);
    }
    
    public List<Object[]> groupByDateAndSumNoItemByDateRange(Date startDate, Date endDate, List<String> giftsType) {
    	return luckyHistoryRepository.groupByDateAndSumNoItemByDateRange(startDate, endDate, giftsType);
    }
	
	public List<Object[]> groupByUserPhoneAndSumNoItemByDateRange(Date startDate, Date endDate, List<String> giftsType, Pageable pageable) {
        return luckyHistoryRepository.groupByUserPhoneAndSumNoItemByDateRange(startDate, endDate, giftsType, pageable);
	}
	
	public List<Object[]> groupByUserPhoneAndCountRecordByDateRange(Date startDate, Date endDate, Pageable pageable) {
        return luckyHistoryRepository.groupByUserPhoneAndCountRecordByDateRange(startDate, endDate, pageable);
	}
	
	public long sumNoItemByDateRange(Date startDate, Date endDate, List<String> giftsType) {
        return luckyHistoryRepository.sumNoItemByDateRange(startDate, endDate, giftsType);
	}
	
	public long countRecordByDateRange(Date startDate, Date endDate) {
        return luckyHistoryRepository.countRecordByDateRange(startDate, endDate);
	}
    
	public void deleteByAddTimeBefore(Date date) {
		luckyHistoryRepository.deleteByAddTimeBefore(date);
	}
    public static void main(String[] args) {
        for(int i = 0; i < 1000; i++) {
//            lucky();
        }

    }
    public long sumMoneyByGiftTypeHTGWithin24Hours(Date startTime) {
        return luckyHistoryRepository.sumMoneyByGiftTypeFBUWithin24Hours(startTime);
    }

    public long sumNoItemByGiftTypeHTGWithin24Hours(Date startTime) {
        return luckyHistoryRepository.sumNoItemByGiftTypeHTGWithin24Hours(startTime);
    }

    public List<Object[]> getTotalMoneyByDateRange(Date startDate, Date endDate, String phone) {
        // TODO Auto-generated method stub
        return luckyHistoryRepository.groupByDateAndSumMoneyByDateRange(startDate, endDate, phone);
    }


    public List<Object[]> groupByUserPhoneAndSumMoneyByDateRange(Date startDate, Date endDate, String phone, Pageable pageable) {
        return luckyHistoryRepository.groupByUserPhoneAndSumMoneyByDateRange(startDate, endDate, "FBU", phone, pageable);
    }

    public List<Object[]> groupByUserPhoneAndCountRecordByDateRange(Date startDate, Date endDate, String phone, Pageable pageable) {
        return luckyHistoryRepository.groupByUserPhoneAndCountRecordByDateRange(startDate, endDate, phone, pageable);
    }

    public Long sumMoneyByDateRange(Date startDate, Date endDate, String phone) {
        Long res = luckyHistoryRepository.sumMoneyByDateRange(startDate, endDate, "FBU", phone);
        return res != null ? res : 0;
    }

    public Long countRecordByDateRange(Date startDate, Date endDate, String phone) {
        Long res = luckyHistoryRepository.countRecordByDateRange(startDate, endDate, phone);
        return res != null ? res : 0;
    }

    public Page<LuckyHistory> findAllByAddTimeBetweenAndUser_PhoneContainingAndGiftTypeContaining(Date startDate, Date endDate, String phone, String giftType, Pageable pageable) {
        if (startDate != null && endDate != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(endDate);
            calendar.add(Calendar.DATE, 1);
            // Cả hai điều kiện đều được thỏa mãn
            return luckyHistoryRepository.findAllByAddTimeBetweenAndUser_PhoneContainingAndGiftTypeContainingOrderByAddTimeDesc(startDate, calendar.getTime(), phone, giftType, pageable);
        } else if (startDate != null) {
            // Chỉ điều kiện startDate được thỏa mãn
            return luckyHistoryRepository.findAllByAddTimeAfterAndUser_PhoneContainingAndGiftTypeContainingOrderByAddTimeDesc(startDate, phone, giftType, pageable);
        } else if (endDate != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(endDate);
            calendar.add(Calendar.DATE, 1);
            // Chỉ điều kiện endDate được thỏa mãn
            return luckyHistoryRepository.findAllByAddTimeBeforeAndUser_PhoneContainingAndGiftTypeContainingOrderByAddTimeDesc(calendar.getTime(), phone, giftType, pageable);
        } else {
            // Không có điều kiện tìm kiếm về thời gian
            return luckyHistoryRepository.findAllByUser_PhoneContainingAndGiftTypeContainingOrderByAddTimeDesc(phone, giftType, pageable);
        }

    }
}
