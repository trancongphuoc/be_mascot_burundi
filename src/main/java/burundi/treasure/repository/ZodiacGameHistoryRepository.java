package burundi.treasure.repository;

import burundi.treasure.model.zodiacgame.ZodiacGame;
import burundi.treasure.model.zodiacgame.ZodiacGameHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface ZodiacGameHistoryRepository extends JpaRepository<ZodiacGameHistory, String> {
//    @Query("SELECT SUM(z.totalIcoinWin) FROM ZodiacGameHistory z WHERE DATE(z.addTime) = CURRENT_DATE AND z.user.id = :userId")
    @Query("SELECT SUM(z.totalIcoinWin) " +
            "FROM ZodiacGameHistory z " +
            "WHERE z.addTime BETWEEN :startOfDay AND :endOfDay " +
            "AND z.user.id = :userId")
    Long sumTotalIcoinWinTodayByUser(@Param("userId") Long userId,
                                     @Param("startOfDay") Date startOfDay,
                                     @Param("endOfDay") Date endOfDay);

//    @Query("SELECT COUNT(DISTINCT z.zodiacGame.id) " +
//            "FROM ZodiacGameHistory z " +
//            "WHERE z.user.id = :userId AND DATE(z.addTime) = CURRENT_DATE")
    @Query("SELECT COUNT(DISTINCT z.zodiacGame.id) " +
            "FROM ZodiacGameHistory z " +
            "WHERE z.addTime BETWEEN :startOfDay AND :endOfDay " +
            "AND z.user.id = :userId")
    Long countDistinctZodiacGameByUserToday(@Param("userId") Long userId,
                                            @Param("startOfDay") Date startOfDay,
                                            @Param("endOfDay") Date endOfDay);

    @Query("SELECT COUNT(DISTINCT z.zodiacCard.id) " +
            "FROM ZodiacGameHistory z " +
            "WHERE  z.user.id = :userId AND z.zodiacGame.id = :zodiacGameId")
    Long countDistinctZodiacCardByUserAndZodiacGame(@Param("userId") Long userId, @Param("zodiacGameId") Long zodiacGameId);

    @Query("SELECT DISTINCT z.zodiacCard.id " +
            "FROM ZodiacGameHistory z " +
            "WHERE z.user.id = :userId AND z.zodiacGame.id = :zodiacGameId")
    List<String> findDistinctZodiacCardIdsByUserAndZodiacGame(@Param("userId") Long userId, @Param("zodiacGameId") Long zodiacGameId);


    @Query("SELECT z FROM ZodiacGameHistory z WHERE z.zodiacGame.id = :zodiacGameId")
    List<ZodiacGameHistory> findByZodiacGameId(@Param("zodiacGameId") Long zodiacGameId);

    @Query("SELECT z FROM ZodiacGameHistory z WHERE z.user.id = :userId ORDER BY z.addTime DESC")
    List<ZodiacGameHistory> findTop200ByOrderByAddTimeDesc(Pageable pageable, Long userId);

}
