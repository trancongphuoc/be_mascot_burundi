package burundi.treasure.repository;

import burundi.treasure.model.zodiacgame.ZodiacCard;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ZodiacCardRepository extends JpaRepository<ZodiacCard, String> {
}
