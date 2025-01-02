package burundi.treasure.service.zodiacgame;

import burundi.treasure.model.dto.zodiacgame.ZodiacCardDTO;
import burundi.treasure.model.zodiacgame.ZodiacCard;
import burundi.treasure.repository.ZodiacCardRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ZodiacCardService {

    @Autowired
    private ZodiacCardRepository zodiacCardRepository;

//    @PostConstruct
//    public void initZodiacCards() {
//        List<ZodiacCard> zodiacCards = new ArrayList<>();
//
//        zodiacCards.add(new ZodiacCard("ZODIAC_CARD_1", "Taurus", 5L, 19.0, "https://mascot.lumitel.bi/images/taurus.svg"));
//        zodiacCards.add(new ZodiacCard("ZODIAC_CARD_2", "Tiger", 5L, 19.0, "https://mascot.lumitel.bi/images/tiger.svg"));
//        zodiacCards.add(new ZodiacCard("ZODIAC_CARD_3", "Dragon", 5L, 19.0, "https://mascot.lumitel.bi/images/dragon.svg"));
//        zodiacCards.add(new ZodiacCard("ZODIAC_CARD_4", "Snake", 8L, 12.0, "https://mascot.lumitel.bi/images/snake.svg"));
//        zodiacCards.add(new ZodiacCard("ZODIAC_CARD_5", "Horse", 8L, 12.0, "https://mascot.lumitel.bi/images/horse.svg"));
//        zodiacCards.add(new ZodiacCard("ZODIAC_CARD_6", "Goat", 10L, 9.0, "https://mascot.lumitel.bi/images/goat.svg"));
//        zodiacCards.add(new ZodiacCard("ZODIAC_CARD_7", "Chicken", 15L, 6.0, "https://mascot.lumitel.bi/images/chicken.svg"));
//        zodiacCards.add(new ZodiacCard("ZODIAC_CARD_8", "Pig", 20L, 4.0, "https://mascot.lumitel.bi/images/pig.svg"));
//
//        zodiacCardRepository.saveAll(zodiacCards);
//
//        System.out.println("Zodiac cards initialized with 6 records.");
//    }

    private Map<String, ZodiacCard> zodiacCardMap = new HashMap<>();

    public ZodiacCardDTO parseToDTOFromEntity(ZodiacCard zodiacCard) {
        ZodiacCardDTO zodiacCardDTO = new ZodiacCardDTO();
        zodiacCardDTO.setId(zodiacCard.getId());
        zodiacCardDTO.setMultiply(zodiacCard.getMultiply());
        zodiacCardDTO.setImageUrl(zodiacCard.getImageUrl());
        zodiacCardDTO.setName(zodiacCard.getName());

        return zodiacCardDTO;
    }

    public Map<String, ZodiacCardDTO> parseToDTOFromEntitiesMap(List<ZodiacCard> zodiacCards) {
        Map<String, ZodiacCardDTO> zodiacCardDTOMap = new LinkedHashMap<>();
        for(ZodiacCard zodiacCard: zodiacCards) {
            ZodiacCardDTO zodiacCardDTO = parseToDTOFromEntity(zodiacCard);
            zodiacCardDTOMap.put(zodiacCardDTO.getId(), zodiacCardDTO);
        }

        return zodiacCardDTOMap;
    }

    public ZodiacCard findById(String id) {
        return zodiacCardRepository.getReferenceById(id);
    }

    public ZodiacCard findByIdAndCache(String id) {
        if(zodiacCardMap.containsKey(id)) {
            return zodiacCardMap.get(id);
        }
        return zodiacCardRepository.getReferenceById(id);
    }

    public List<ZodiacCard> findAll() {
        return zodiacCardRepository.findAll();
    }

    public List<ZodiacCard> findAllAndCache() {
        if(!zodiacCardMap.isEmpty()) {
            return new ArrayList<>(zodiacCardMap.values());
        }

        List<ZodiacCard> zodiacCards = zodiacCardRepository.findAll();
        for(ZodiacCard zodiacCard: zodiacCards) {
            zodiacCardMap.put(zodiacCard.getId(), zodiacCard);
        }
        return zodiacCards;
    }

    public ZodiacCard randomize(List<ZodiacCard> cards) {
        // Tính tổng xác suất của tất cả các phần tử
        double totalProbability = 0;
        for (ZodiacCard card : cards) {
            totalProbability += card.getProbability();
        }

        // Sinh một số ngẫu nhiên từ 0 đến tổng xác suất
        Random random = new Random();
        double randomProbability = random.nextDouble() * totalProbability;

        // Lặp qua danh sách các phần tử và cộng dần giá trị xác suất
        double cumulativeProbability = 0;
        for (ZodiacCard card : cards) {
            cumulativeProbability += card.getProbability();
            if (randomProbability <= cumulativeProbability) {
                return card;
            }
        }

        // Trả về phần tử cuối cùng nếu không tìm thấy phần tử thích hợp
        return cards.get(cards.size() - 1);
    }
}
