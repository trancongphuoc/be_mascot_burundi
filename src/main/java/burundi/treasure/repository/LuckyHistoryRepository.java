package burundi.treasure.repository;

import burundi.treasure.model.LuckyHistory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Repository
public interface LuckyHistoryRepository extends JpaRepository<LuckyHistory, Long> {
    List<LuckyHistory> findByUserIdOrderByAddTimeDesc(long userId);
	List<LuckyHistory> findByUserIdOrderByAddTimeDesc(long userId, Pageable pageable);

	List<LuckyHistory> findByUserIdAndGiftTypeOrderByAddTimeDesc(long userId, String giftType);

	@Query("SELECT lh FROM LuckyHistory lh WHERE lh.user.id = :userId AND lh.giftType = :giftType AND lh.addTime >= :startOfDay ORDER BY lh.addTime DESC")
	List<LuckyHistory> findByUserIdAndGiftTypeAfterStartOfDay(
			@Param("userId") long userId,
			@Param("giftType") String giftType,
			@Param("startOfDay") Date startOfDay
	);

    List<LuckyHistory> findByGiftIdAndIsShareFalse(String giftId);
    
 // Query method để tính tổng noItem của giftType = "HTG" trong 24 giờ gần nhất
    @Query("SELECT COALESCE(SUM(lh.noItem), 0) FROM LuckyHistory lh WHERE lh.giftType = 'FBU' AND lh.addTime >= :startTime")
    long sumNoItemByGiftTypeHTGWithin24Hours(Date startTime);
    
    @Query("SELECT COALESCE(SUM(lh.noItem), 0) FROM LuckyHistory lh WHERE lh.giftType = :type AND lh.addTime >= :startTime")
    long sumNoItemByGiftTypeWithin24Hours(Date startTime, String type);
    
	@Query("SELECT CONCAT(DAY(lh.addTime), '-', MONTH(lh.addTime), '-', YEAR(lh.addTime)), SUM(lh.noItem) " +
	        "FROM LuckyHistory lh WHERE lh.addTime BETWEEN :startDate AND :endDate " +
	        "AND lh.giftType IN :giftsType " +
	        "GROUP BY CONCAT(DAY(lh.addTime), '-', MONTH(lh.addTime), '-', YEAR(lh.addTime))")
	List<Object[]> groupByDateAndSumNoItemByDateRange(Date startDate, Date endDate, List<String> giftsType);
     
	@Query("SELECT lh.user.phone, SUM(lh.noItem) AS totalNoItem " +
			"FROM LuckyHistory lh " +
			"WHERE lh.giftType IN :giftsType " +
			"AND lh.addTime BETWEEN :startTime AND :endTime " +
			"GROUP BY lh.user.phone " +
			"ORDER BY totalNoItem DESC")
	List<Object[]> groupByUserPhoneAndSumNoItemByDateRange(Date startTime, Date endTime, List<String> giftsType, Pageable pageable);
      
	@Query("SELECT SUM(lh.noItem) AS totalNoItem " +
			"FROM LuckyHistory lh " +
			"WHERE lh.giftType IN :giftsType " +
			"AND lh.addTime BETWEEN :startTime AND :endTime ")
	long sumNoItemByDateRange(Date startTime, Date endTime, List<String> giftsType);
       
	@Query("SELECT lh.user.phone, count(*) AS totalPlay " +
			"FROM LuckyHistory lh " +
			"WHERE lh.addTime BETWEEN :startTime AND :endTime " +
			"GROUP BY lh.user.phone " +
			"ORDER BY totalPlay DESC")
	List<Object[]> groupByUserPhoneAndCountRecordByDateRange(Date startTime, Date endTime, Pageable pageable);
	
	@Query("SELECT count(*) AS totalPlay " +
			"FROM LuckyHistory lh " +
			"WHERE lh.addTime BETWEEN :startTime AND :endTime ")
	long countRecordByDateRange(Date startTime, Date endTime);
	
    @Transactional
    @Modifying
    @Query("DELETE FROM LuckyHistory lh WHERE lh.addTime < :date")
    void deleteByAddTimeBefore(Date date);


	List<LuckyHistory> findByUserId(long userId);

	Page<LuckyHistory> findAllByAddTimeBetweenAndUser_PhoneContainingAndGiftTypeContainingOrderByAddTimeDesc(Date startDate, Date endDate, String phone, String giftType, Pageable pageable);

	Page<LuckyHistory> findAllByAddTimeAfterAndUser_PhoneContainingAndGiftTypeContainingOrderByAddTimeDesc(Date startDate, String phone, String giftType, Pageable pageable);

	Page<LuckyHistory> findAllByAddTimeBeforeAndUser_PhoneContainingAndGiftTypeContainingOrderByAddTimeDesc(Date endDate, String phone, String giftType, Pageable pageable);

	Page<LuckyHistory> findAllByUser_PhoneContainingAndGiftTypeContainingOrderByAddTimeDesc(String phone, String giftType, Pageable pageable);

	@Query("SELECT lh FROM LuckyHistory lh WHERE (:startDate IS NULL OR lh.addTime >= :startDate) " +
			"AND (:endDate IS NULL OR lh.addTime <= :endDate) " +
			"AND (:phone IS NULL OR lh.user.phone = :phone)")
	Page<LuckyHistory> findByConditions(Date startDate, Date endDate, String phone, Pageable pageable);


	// Query method để tính tổng noItem của giftType = "FBU" trong 24 giờ gần nhất
	@Query("SELECT COALESCE(SUM(lh.noItem), 0) FROM LuckyHistory lh WHERE lh.giftType = 'FBU' AND lh.addTime >= :startTime")
	long sumMoneyByGiftTypeFBUWithin24Hours(Date startTime);

	@Query("SELECT CONCAT(DAY(lh.addTime), '-', MONTH(lh.addTime), '-', YEAR(lh.addTime)), SUM(lh.noItem) " +
			"FROM LuckyHistory lh WHERE lh.addTime BETWEEN :startDate AND :endDate " +
			"AND lh.giftType = 'FBU' " +
			"AND lh.user.phone like CONCAT('%', :msisdn, '%') " +
			"GROUP BY CONCAT(DAY(lh.addTime), '-', MONTH(lh.addTime), '-', YEAR(lh.addTime))")
	List<Object[]> groupByDateAndSumMoneyByDateRange(Date startDate, Date endDate, String msisdn);


	@Query("SELECT lh.user.phone, SUM(lh.noItem) AS totalMoney " +
			"FROM LuckyHistory lh " +
			"WHERE lh.giftType = :giftType " +
			"AND lh.addTime BETWEEN :startTime AND :endTime " +
			"AND lh.user.phone like CONCAT('%', :msisdn, '%') " +
			"GROUP BY lh.user.phone " +
			"ORDER BY totalMoney DESC")
	List<Object[]> groupByUserPhoneAndSumMoneyByDateRange(Date startTime, Date endTime, String giftType, String msisdn, Pageable pageable);

	@Query("SELECT SUM(lh.noItem) AS totalNoItem " +
			"FROM LuckyHistory lh " +
			"WHERE lh.giftType = :giftType " +
			"AND lh.addTime BETWEEN :startTime AND :endTime " +
			"AND lh.user.phone like CONCAT('%', :msisdn, '%') ")
	Long sumMoneyByDateRange(Date startTime, Date endTime, String giftType, String msisdn);


	@Query("SELECT lh.user.phone, count(*) AS totalPlay " +
			"FROM LuckyHistory lh " +
			"WHERE lh.addTime BETWEEN :startTime AND :endTime " +
			"AND lh.user.phone like CONCAT('%', :msisdn, '%') " +
			"GROUP BY lh.user.phone " +
			"ORDER BY totalPlay DESC")
	List<Object[]> groupByUserPhoneAndCountRecordByDateRange(Date startTime, Date endTime, String msisdn, Pageable pageable);


	@Query("SELECT count(*) AS totalPlay " +
			"FROM LuckyHistory lh " +
			"WHERE lh.addTime BETWEEN :startTime AND :endTime " +
			"AND lh.user.phone like CONCAT('%', :msisdn, '%') ")
	Long countRecordByDateRange(Date startTime, Date endTime, String msisdn);

}

