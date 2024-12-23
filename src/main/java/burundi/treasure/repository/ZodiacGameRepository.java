package burundi.treasure.repository;

import burundi.treasure.model.zodiacgame.ZodiacGame;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ZodiacGameRepository extends JpaRepository<ZodiacGame, Long> {
    boolean existsByStatus(String status);

    ZodiacGame findFirstByStatus(String status);

    @Query("SELECT z FROM ZodiacGame z ORDER BY z.addTime DESC")
    List<ZodiacGame> findTop50ByOrderByAddTimeDesc(Pageable pageable);
}
