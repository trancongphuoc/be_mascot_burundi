package burundi.treasure.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import burundi.treasure.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{
    User findByUsername(String username);
    
    User findByPhone(String phone);
    
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.totalPlay = 0, u.totalStar = 0")
    void resetTotalPlayAndTotalStar();
    
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.totalPlay = 0 where u.totalPlay > 0")
    void resetTotalPlay();
    
//    @Modifying
//    @Transactional
//    @Query("UPDATE User u SET u.totalPlayBonus = :number")
//    void resetTotalPlayBonus(Long number);
    
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.totalStar = 0 where u.totalStar > 0")
    void resetTotalStar();

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.totalStarMonth = 0 where u.totalStarMonth > 0")
    void resetTotalStarMonth();
    
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.totalPlay = 0, u.totalStar = 0 WHERE u.id = :userId")
    void resetTotalPlayAndTotalStarById(@Param("userId") Long userId);
    
//    // Truy vấn 2 người dùng có totalStar lớn nhất
//    @Query("SELECT u FROM User u ORDER BY u.totalStar DESC LIMIT 2")
//    List<User> findTop2UsersWithMaxTotalStar();
    
 // Truy vấn 2 người dùng có totalStar lớn nhất
    Page<User> findTop3UserByOrderByTotalStarDesc(Pageable pageable);

    Page<User> findTop3UserByOrderByTotalStarMonthDesc(Pageable pageable);
    Page<User> findTop50UserByOrderByTotalStarDesc(Pageable pageable);
    Page<User> findTop50UserByOrderByTotalStarMonthDesc(Pageable pageable);

    List<User> findAllByTotalStarGreaterThanOrderByTotalStarDesc(Long totalStar);
    List<User> findAllByTotalStarMonthGreaterThanOrderByTotalStarMonthDesc(Long totalStar);

    Page<User> findAllByTotalStarGreaterThanAndPhoneContainingOrderByTotalStarDesc(Long totalStar, String phone, Pageable pageable);

    // Top user total win
    Page<User> findTop2UserByOrderByTotalWinDesc(Pageable pageable);

    @Query("SELECT sum(u.totalPlay) AS totalPlay " +
            "FROM User u " +
            "WHERE u.phone like CONCAT('%', :phone, '%') " +
            "AND u.totalPlay > 0")
    Long sumTotalPlayByPhone(String phone);

    @Query("SELECT u.phone, u.totalPlay " +
            "FROM User u " +
            "WHERE u.phone like CONCAT('%', :phone, '%') " +
            "AND u.totalPlay > 0" +
            "ORDER BY u.totalPlay DESC")
    List<Object[]> groupByUserPhoneAndSumTotalPlay(String phone);
}
