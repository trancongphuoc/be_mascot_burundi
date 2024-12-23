package burundi.treasure.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import burundi.treasure.model.MPSRequest;

public interface MPSRequestRepository extends JpaRepository<MPSRequest, Long>{


    @Query("SELECT m FROM MPSRequest m WHERE m.action = :action AND m.params = :params AND m.chargetTime < :date")
    List<MPSRequest> findByActionAndParamsAndChargetTimeBefore(String action, String params, Date date);

    @Transactional
    @Modifying
    @Query("DELETE FROM MPSRequest m WHERE m.action = :action AND m.params = :params AND m.chargetTime < :date")
    void deleteByActionAndParamsAndChargetTimeBefore(String action, String params, Date date);


    // Query method để tính tổng amount trong 24 giờ gần nhất
    @Query("SELECT COALESCE(SUM(m.amount), 0) FROM MPSRequest m WHERE m.chargetTime >= :startTime "
            + "AND m.action IN ('RENEW', 'CHARGE', 'REGISTER') "
            + "AND m.params = '0' ")
    Long sumAmountWithin24Hours(Date startTime);

    @Query("SELECT CONCAT(DAY(chargetTime), '-', MONTH(chargetTime), '-', YEAR(chargetTime)), SUM(amount) " +
            "FROM MPSRequest WHERE chargetTime BETWEEN :startDate AND :endDate " +
            "AND params = :params " +
            "AND action IN :actions " +
            "AND msisdn like CONCAT('%', :msisdn, '%') " +
            "GROUP BY CONCAT(DAY(chargetTime), '-', MONTH(chargetTime), '-', YEAR(chargetTime))")
    List<Object[]> groupByDateAndSumAmountByDateRange(Date startDate, Date endDate, String params, List<String> actions, String msisdn );

    @Query("SELECT CONCAT(DAY(chargetTime), '-', MONTH(chargetTime), '-', YEAR(chargetTime)), COUNT(*) " +
            "FROM MPSRequest WHERE chargetTime BETWEEN :startDate AND :endDate " +
            "AND params = :params " +
            "AND action IN :actions " +
            "AND msisdn like CONCAT('%', :msisdn, '%') " +
            "GROUP BY CONCAT(DAY(chargetTime), '-', MONTH(chargetTime), '-', YEAR(chargetTime))")
    List<Object[]> groupByDateAndCountRecordByDateRange(Date startDate, Date endDate, String params, List<String> actions, String msisdn);


    @Query("SELECT mr.user.phone, SUM(mr.amount) AS totalAmount " +
            "FROM MPSRequest mr " +
            "WHERE mr.chargetTime BETWEEN :startTime AND :endTime " +
            "AND mr.action IN :actions " +
            "AND mr.params = :params " +
            "AND mr.msisdn like CONCAT('%', :msisdn, '%') " +
            "GROUP BY mr.user.phone " +
            "ORDER BY totalAmount DESC")
    List<Object[]> groupByUserPhoneAndSumAmountByDateRange(Date startTime, Date endTime, String params, List<String> actions, String msisdn, Pageable pageable);

    @Query("SELECT SUM(mr.amount) AS totalAmount " +
            "FROM MPSRequest mr " +
            "WHERE mr.chargetTime BETWEEN :startTime AND :endTime " +
            "AND mr.action IN :actions " +
            "AND mr.params = :params " +
            "AND mr.msisdn like CONCAT('%', :msisdn, '%') ")

    Long sumAmountByDateRange(Date startTime, Date endTime, String params, List<String> actions, String msisdn);

    @Transactional
    @Modifying
    @Query("DELETE FROM MPSRequest m WHERE m.chargetTime < :date")
    void deleteByChargetTimeBefore(Date date);
}
