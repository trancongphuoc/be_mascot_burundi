package burundi.treasure.service.zodiacgame;

import burundi.treasure.model.User;
import burundi.treasure.model.dto.UserDTO;
import burundi.treasure.model.dto.UserZodiacGameDTO;
import burundi.treasure.model.dto.zodiacgame.ZodiacCardDTO;
import burundi.treasure.model.dto.zodiacgame.ZodiacGameUserDTO;
import burundi.treasure.model.zodiacgame.ZodiacCard;
import burundi.treasure.model.zodiacgame.ZodiacGame;
import burundi.treasure.model.zodiacgame.ZodiacGameHistory;
import burundi.treasure.repository.ZodiacGameHistoryRepository;
import burundi.treasure.service.UserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Log4j2
public class ZodiacGameHistoryService {


    @Autowired
    private ZodiacGameHistoryRepository zodiacGameHistoryRepository;

    @Autowired
    private ZodiacCardService zodiacCardService;

    @Lazy
    @Autowired
    private ZodiacGameService zodiacGameService;

    @Autowired
    private UserService userService;

    public ZodiacGameHistory newZodiacGameHistory(User user, ZodiacGame zodiacGame, ZodiacCard zodiacCard, Long totalIcoinBetting) {
        ZodiacGameHistory zodiacGameHistory = new ZodiacGameHistory();
        zodiacGameHistory.setAddTime(new Date());
        zodiacGameHistory.setUser(user);
        zodiacGameHistory.setZodiacGame(zodiacGame);
        zodiacGameHistory.setZodiacCard(zodiacCard);
        zodiacGameHistory.setTotalIcoinBetting(totalIcoinBetting);
        zodiacGameHistory.setStatus("NEW");
        return zodiacGameHistoryRepository.save(zodiacGameHistory);
    }

    public ZodiacGameHistory save(ZodiacGameHistory zodiacGameHistory) {
        return zodiacGameHistoryRepository.save(zodiacGameHistory);
    }


    public List<ZodiacGameHistory> saveList(List<ZodiacGameHistory> zodiacGameHistories) {
        return zodiacGameHistoryRepository.saveAll(zodiacGameHistories);
    }
    public Long getNoBettingToday(Long userId) {
        LocalDate today = LocalDate.now();
        Date startOfDay = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endOfDay = Date.from(today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        return zodiacGameHistoryRepository.countDistinctZodiacGameByUserToday(userId, startOfDay, endOfDay);
    }

    public Long getTotalIcoinWinToday(Long userId) {
        LocalDate today = LocalDate.now();
        Date startOfDay = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endOfDay = Date.from(today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        return zodiacGameHistoryRepository.sumTotalIcoinWinTodayByUser(userId, startOfDay, endOfDay);
    }

    public List<String> getZodiacCardsBetting(Long userId, Long zodiacGameId) {
        return zodiacGameHistoryRepository.findDistinctZodiacCardIdsByUserAndZodiacGame(userId, zodiacGameId);
    }

    public List<ZodiacGameHistory> findAllByZodiacGameId(Long zodiacGameId) {
        return zodiacGameHistoryRepository.findByZodiacGameId(zodiacGameId);
    }

    public Map<Long, UserZodiacGameDTO> getTopUsersMap(ZodiacGame zodiacGame) {
        Map<Long, UserZodiacGameDTO> topUserSortedMap = null;
        try {
            // Truy vấn danh sách user đã đặt cược ván game này để xử lí thắng thua
            List<ZodiacGameHistory> zodiacGameHistories = zodiacGameHistoryRepository.findByZodiacGameId(zodiacGame.getId());
            Map<Long, Long> topUserMap = new HashMap<>();

            Long totalIcoinWin;
            ZodiacCard zodiacCard = zodiacGame.getZodiacCard();
            for(ZodiacGameHistory zodiacGameHistory: zodiacGameHistories) {
                if(zodiacGame.getZodiacCard().getId().equals(zodiacGameHistory.getZodiacCard().getId())) {
                    totalIcoinWin = zodiacGameHistory.getTotalIcoinBetting() * zodiacCard.getMultiply();
                    if(topUserMap.containsKey(zodiacGameHistory.getUser().getId())) {
                        long newValue = totalIcoinWin + topUserMap.get(zodiacGameHistory.getUser().getId());
                        topUserMap.put(zodiacGameHistory.getUser().getId(), newValue);
                    } else {
                        topUserMap.put(zodiacGameHistory.getUser().getId(), totalIcoinWin);
                    }
                }
            }

            // Sắp xếp map theo giá trị
            topUserSortedMap = topUserMap.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .limit(3)
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry ->  {
                                User user = userService.findById(entry.getKey());
                                UserZodiacGameDTO userDTO = new UserZodiacGameDTO(user);
                                userDTO.setTotalIcoin(entry.getValue());
                                return userDTO;
                            },
                            (e1, e2) -> e1, // trường hợp có key trùng
                            LinkedHashMap::new // duy trì thứ tự sắp xếp
                    ));


        } catch (Exception e) {
            log.warn("BUGS", e);
        }

        return topUserSortedMap;
    }

    public List<ZodiacGameHistory> getLatest200Records(Long userId) {
        Pageable pageable = PageRequest.of(0, 200);
        return zodiacGameHistoryRepository.findTop200ByOrderByAddTimeDesc(pageable, userId);
    }


    public List<ZodiacGameUserDTO> entityToDTO(List<ZodiacGameHistory> zodiacGameHistories) {

        Map<Long, ZodiacGameUserDTO> groupedMap = new LinkedHashMap<>();
        for(ZodiacGameHistory zodiacGameHistory: zodiacGameHistories) {
            Long zodiacGameId = zodiacGameHistory.getZodiacGame().getId();
            Long userId = zodiacGameHistory.getUser().getId();
            ZodiacCard zodiacCard = zodiacGameHistory.getZodiacCard();
            ZodiacGameUserDTO zodiacGameUserDTO;
            if(!groupedMap.containsKey(zodiacGameId)) {
                zodiacGameUserDTO = new ZodiacGameUserDTO();
                zodiacGameUserDTO.setId(zodiacGameId + "#" + userId);
                zodiacGameUserDTO.setAddTime(zodiacGameHistory.getAddTime());

                zodiacGameUserDTO.setFacebookUserId(userId);
                zodiacGameUserDTO.setUser(new UserDTO(zodiacGameHistory.getUser()));

                zodiacGameUserDTO.setZodiacGameId(zodiacGameId);
                zodiacGameUserDTO.setZodiacGame(zodiacGameService.parseToDTOFromEntity(zodiacGameHistory.getZodiacGame()));

                zodiacGameUserDTO.setZodiacCardId(zodiacGameHistory.getZodiacGame().getZodiacCard().getId());
                zodiacGameUserDTO.setZodiacCard(zodiacCardService.parseToDTOFromEntity(zodiacGameHistory.getZodiacGame().getZodiacCard()));

                zodiacGameUserDTO.setTotalIcoinBetting(zodiacGameHistory.getTotalIcoinBetting());
                zodiacGameUserDTO.setTotalIcoinWin(zodiacGameHistory.getTotalIcoinWin());
                zodiacGameUserDTO.setNoGame(zodiacGameHistory.getZodiacGame().getNoGame());

                List<String> zodiacCardIds = new ArrayList<>();
                zodiacCardIds.add(zodiacCard.getId());
                zodiacGameUserDTO.setZodiacCardIds(zodiacCardIds);

                List<ZodiacCardDTO> zodiacCardDTOList = new ArrayList<>();
                zodiacCardDTOList.add(zodiacCardService.parseToDTOFromEntity(zodiacCard));
                zodiacGameUserDTO.setZodiacCards(zodiacCardDTOList);

                groupedMap.put(zodiacGameId, zodiacGameUserDTO);
            } else {
                zodiacGameUserDTO = groupedMap.get(zodiacGameId);
                if(zodiacGameUserDTO.getTotalIcoinBetting() != null) {
                    zodiacGameUserDTO.setTotalIcoinBetting(zodiacGameUserDTO.getTotalIcoinBetting() + zodiacGameHistory.getTotalIcoinBetting());
                }

                if(zodiacGameUserDTO.getTotalIcoinWin() != null) {
                    zodiacGameUserDTO.setTotalIcoinWin(zodiacGameUserDTO.getTotalIcoinWin() + zodiacGameHistory.getTotalIcoinWin());
                }

                if(!zodiacGameUserDTO.getZodiacCardIds().contains(zodiacCard.getId())) {
                    zodiacGameUserDTO.getZodiacCardIds().add(zodiacCard.getId());
                    zodiacGameUserDTO.getZodiacCards().add(zodiacCardService.parseToDTOFromEntity(zodiacCard));
                }
            }

            for (ZodiacCardDTO cardDTO : zodiacGameUserDTO.getZodiacCards()) {
                if (cardDTO.getId().equals(zodiacCard.getId())) {
                    cardDTO.setTotalIcoinBetting(
                            cardDTO.getTotalIcoinBetting() == null
                                    ? zodiacGameHistory.getTotalIcoinBetting()
                                    : cardDTO.getTotalIcoinBetting() + zodiacGameHistory.getTotalIcoinBetting()
                    );
                }
            }
        }

        return new ArrayList<>(groupedMap.values());
    }
}
