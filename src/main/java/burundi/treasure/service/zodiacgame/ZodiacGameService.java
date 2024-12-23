package burundi.treasure.service.zodiacgame;

import burundi.treasure.model.LuckyHistory;
import burundi.treasure.model.User;
import burundi.treasure.model.ZodiacGameProperties;
import burundi.treasure.model.dto.zodiacgame.ZodiacCardDTO;
import burundi.treasure.model.dto.zodiacgame.ZodiacGameDTO;
import burundi.treasure.model.zodiacgame.ZodiacCard;
import burundi.treasure.model.zodiacgame.ZodiacGame;
import burundi.treasure.model.zodiacgame.ZodiacGameHistory;
import burundi.treasure.repository.ZodiacGamePropertiesRepository;
import burundi.treasure.repository.ZodiacGameRepository;
import burundi.treasure.service.LuckyService;
import burundi.treasure.service.UserService;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Log4j2
public class ZodiacGameService {

    @Autowired
    private ZodiacGamePropertiesRepository zodiacGamePropertiesRepository;

    @Autowired
    private ZodiacGameRepository zodiacGameRepository;

    @Autowired
    private ZodiacGameHistoryService zodiacGameHistoryService;

    @Autowired
    private ZodiacCardService zodiacCardService;

    @Autowired
    private UserService userService;

    @Autowired
    private LuckyService luckyService;

    private static final String PROPERTY_KEY = "ZodiacGame";
    @PostConstruct
    public void initializeZodiacGameProperties() {
        boolean exists = zodiacGamePropertiesRepository.existsById(PROPERTY_KEY);

        if (!exists) {
            log.info("Initializing ZodiacGameProperties...");
            ZodiacGameProperties zodiacGameProperties = new ZodiacGameProperties();
            zodiacGameProperties.setId("ZodiacGame");
            zodiacGameProperties.setNoGame(0L);
            zodiacGamePropertiesRepository.save(zodiacGameProperties);
            log.info("ZodiacGameProperties initialized successfully.");
        } else {
            log.info("ZodiacGameProperties already exists. No initialization needed.");
        }
    }


    public List<ZodiacGame> getLatest50Records() {
        Pageable pageable = PageRequest.of(0, 50);
        return zodiacGameRepository.findTop50ByOrderByAddTimeDesc(pageable);
    }


    public ZodiacGame newZodiacGame() {
        ZodiacGame zodiacGame = new ZodiacGame();
        zodiacGame.setAddTime(new Date());
        zodiacGame.setStatus("NEW");
        zodiacGame.setNoGame(getNoGameToday());
        zodiacGameRepository.save(zodiacGame);
        return zodiacGame;
    }

    public ZodiacGame save(ZodiacGame zodiacGame) {
        return zodiacGameRepository.save(zodiacGame);
    }

    public ZodiacGame findById(Long zodiacGameId) {
        return zodiacGameRepository.findById(zodiacGameId).orElse(null);
    }
    public Long getNoGameToday() {
        ZodiacGameProperties zodiacGameProperties = zodiacGamePropertiesRepository.getReferenceById(PROPERTY_KEY);
        zodiacGameProperties.setNoGame(zodiacGameProperties.getNoGame() + 1L);
        zodiacGamePropertiesRepository.save(zodiacGameProperties);
        return zodiacGameProperties.getNoGame();
    }

    public void resetNoGameToday() {
        ZodiacGameProperties zodiacGameProperties = zodiacGamePropertiesRepository.getReferenceById(PROPERTY_KEY);
        zodiacGameProperties.setNoGame(0L);
        zodiacGamePropertiesRepository.save(zodiacGameProperties);
    }

    public Boolean isGameInProcess() {
        return zodiacGameRepository.existsByStatus("NEW");
    }

    public ZodiacGame getCurrentGame() {
        return zodiacGameRepository.findFirstByStatus("NEW");
    }

    public List<ZodiacGameDTO> parseToDTOFromEntity(List<ZodiacGame> zodiacGameList) {
        List<ZodiacGameDTO> zodiacGameDTOList = new ArrayList<>();

        for(ZodiacGame zodiacGame : zodiacGameList) {

            // Trường hợp ván game đang diễn ra sẽ chưa có kết quả
            if(zodiacGame.getZodiacCard() == null)
                continue;

            ZodiacGameDTO zodiacGameDTO = parseToDTOFromEntity(zodiacGame);
            zodiacGameDTOList.add(zodiacGameDTO);
        }
        return zodiacGameDTOList;
    }

    public ZodiacGameDTO parseToDTOFromEntity(ZodiacGame zodiacGame) {
        ZodiacGameDTO zodiacGameDTO = new ZodiacGameDTO();
        zodiacGameDTO.setId(zodiacGame.getId());
        zodiacGameDTO.setStatus(zodiacGame.getStatus());
        zodiacGameDTO.setAddTime(zodiacGame.getAddTime());
        zodiacGameDTO.setTotalIcoinBetting(zodiacGame.getTotalIcoinBetting());
        zodiacGameDTO.setTotalIcoinWin(zodiacGame.getTotalIcoinWin());
        zodiacGameDTO.setNoGame(zodiacGame.getNoGame());

        // Trường hợp ván game đang diễn ra sẽ chưa có kết quả
        if(zodiacGame.getZodiacCard() != null) {
            ZodiacCardDTO zodiacCardDTO = zodiacCardService.parseToDTOFromEntity(zodiacGame.getZodiacCard());

            zodiacGameDTO.setZodiacCard(zodiacCardDTO);
            zodiacGameDTO.setZodiacCardId(zodiacCardDTO.getId());
        }

        return zodiacGameDTO;
    }


   public void processResult(Long zodiacGameId) {
       log.warn(String.format("ZodiacGameTasks.processResult Start: %s, GameId: %s", new Date(), zodiacGameId));

       ZodiacGame zodiacGame = findById(zodiacGameId);
       List<ZodiacGameHistory> zodiacGameHistories =
               zodiacGameHistoryService.findAllByZodiacGameId(zodiacGameId);
       // Tổng tiền thắng thua của ván cược
       Long totalScoreWin = 0L;
       Long totalIcoinBetting = 0L;
       ZodiacCard zodiacCard = zodiacGame.getZodiacCard();

       List<LuckyHistory> luckyHistories = new ArrayList<>();
       for(ZodiacGameHistory zodiacGameHistory: zodiacGameHistories) {
           try {
               if(zodiacGameHistory.getStatus().equalsIgnoreCase("PENDING")
                       || zodiacGameHistory.getStatus().equalsIgnoreCase("NEW")
                       || zodiacGameHistory.getStatus().equalsIgnoreCase("ERROR")) {
                   totalIcoinBetting += zodiacGameHistory.getTotalIcoinBetting();
                   if(zodiacCard.getId().equals(zodiacGameHistory.getZodiacCard().getId())) {
                       long winScore = zodiacGameHistory.getTotalIcoinBetting() * zodiacCard.getMultiply();

                       User user = zodiacGameHistory.getUser();
                       userService.incrementTotalStar(user, winScore);
                       userService.saveUser(user);

                       zodiacGameHistory.setTotalIcoinWin(winScore);
                       zodiacGameHistory.setStatus("WIN");

                       totalScoreWin += winScore;
                   } else {
                       zodiacGameHistory.setStatus("LOSE");
                   }
               } else {
                   zodiacGameHistory.setStatus("ERROR");
               }
           } catch (Exception e) {
               zodiacGameHistory.setStatus("ERROR");
           }

           LuckyHistory luckyHistory = new LuckyHistory();
           luckyHistory.setGiftType("STARS");
           luckyHistory.setAddTime(new Date());
           luckyHistory.setGiftId(zodiacGameHistory.getZodiacCard().getId());

           luckyHistory.setGiftBetting(zodiacGameHistory.getZodiacCard().getName());
           luckyHistory.setGiftResult(zodiacCard.getName());

           luckyHistory.setNoItem(zodiacGameHistory.getTotalIcoinBetting());
           luckyHistory.setNoWin(zodiacGameHistory.getTotalIcoinWin());
           luckyHistory.setUser(zodiacGameHistory.getUser());

           luckyHistories.add(luckyHistory);
       }

       luckyService.saveAll(luckyHistories);

       zodiacGameHistoryService.saveList(zodiacGameHistories);

       zodiacGame.setTotalIcoinWin(totalScoreWin);
       zodiacGame.setTotalIcoinBetting(totalIcoinBetting);

       // Cập nhật status đã xử lí xong
       zodiacGame.setStatus("PROCESSED");
       save(zodiacGame);
   }


}
