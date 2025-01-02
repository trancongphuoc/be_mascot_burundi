package burundi.treasure.controller;

import burundi.treasure.firebase.ZodiacGameFirebaseService;
import burundi.treasure.model.User;
import burundi.treasure.model.dto.UserDTO;
import burundi.treasure.model.dto.UserZodiacGameDTO;
import burundi.treasure.model.dto.zodiacgame.ZodiacCardDTO;
import burundi.treasure.model.dto.zodiacgame.ZodiacGameDTO;
import burundi.treasure.model.dto.zodiacgame.ZodiacGameUserDTO;
import burundi.treasure.model.zodiacgame.ZodiacCard;
import burundi.treasure.model.zodiacgame.ZodiacGame;
import burundi.treasure.model.zodiacgame.ZodiacGameHistory;
import burundi.treasure.payload.BettingRequest;
import burundi.treasure.payload.EndRequest;
import burundi.treasure.payload.Response;
import burundi.treasure.service.LuckyService;
import burundi.treasure.service.UserService;
import burundi.treasure.service.zodiacgame.ZodiacCardService;
import burundi.treasure.service.zodiacgame.ZodiacGameHistoryService;
import burundi.treasure.service.zodiacgame.ZodiacGameService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@RestController
@Log4j2
@RequestMapping("/api/mascot")
public class ZodiacGameController {

    @Autowired
    private UserService userService;

    @Autowired
    private ZodiacGameService zodiacGameService;

    @Autowired
    private ZodiacGameFirebaseService zodiacGameFirebaseService;

    @Autowired
    private ZodiacGameHistoryService zodiacGameHistoryService;

    @Autowired
    private ZodiacCardService zodiacCardService;

    @Autowired
    private LuckyService luckyService;

    @PostMapping("/update-zodiac-cards")
    public ResponseEntity<?> updateZodiacCards() {
        Response response = new Response("OK", "OK");
        Map<String, Object> data = new HashMap<>();
        try {
            List<ZodiacCard> zodiacCards = zodiacCardService.findAll();
            Map<String, ZodiacCardDTO> zodiacCardDTOMap = zodiacCardService.parseToDTOFromEntitiesMap(zodiacCards);

            zodiacGameFirebaseService.updateZodiacCards(zodiacCardDTOMap);
        } catch (Exception e) {
            log.warn("BUGS",e);;
            response.setStatus("FAILED");
            response.setMessage(e.getMessage());
        }

        response.setData(data);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/join-game")
    public ResponseEntity<?> joinGame(@AuthenticationPrincipal UserDetails userDetails) {
        Response response = new Response("OK", "OK");
        Map<String, Object> data = new HashMap<>();
        try {
            User user = userService.findByUserName(userDetails.getUsername());

            UserZodiacGameDTO userDTO = new UserZodiacGameDTO(user);
            userDTO.setNoBettingToday(zodiacGameHistoryService.getNoBettingToday(user.getId())); //
            userDTO.setTotalIcoinWinToday(user.getTotalStar()); //
            userDTO.setTotalIcoinWinMonthday(user.getTotalStarMonth()); //

            // Đồng bộ sang firebase
            zodiacGameFirebaseService.joinGame(userDTO);

            data.put("user", userDTO);
        } catch (Exception e) {
            log.warn("BUGS",e);;
            response.setStatus("FAILED");
            response.setMessage(e.getMessage());
        }

        response.setData(data);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/exit-game")
    public ResponseEntity<?> exitGame(@AuthenticationPrincipal UserDetails userDetails,
                                      @RequestParam(value = "userId", required = false) Long userId) {
        Response response = new Response("OK", "OK");
        Map<String, Object> data = new HashMap<>();
        try {
            User user;
            if(userDetails == null) {
                user = userService.findById(userId);
            } else {
                user = userService.findByUserName(userDetails.getUsername());
            }
            zodiacGameFirebaseService.exitGame(user.getId());

        } catch (Exception e) {
            log.warn("BUGS",e);;
            response.setStatus("FAILED");
            response.setMessage(e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/do-nothing")
    public ResponseEntity<?> exitGame(@AuthenticationPrincipal UserDetails userDetails) {
        Response response = new Response("OK", "OK");
        Map<String, Object> data = new HashMap<>();
        try {
            User user = userService.findByUserName(userDetails.getUsername());
            zodiacGameFirebaseService.doNothing(user.getId());
        } catch (Exception e) {
            log.warn("BUGS",e);;
            response.setStatus("FAILED");
            response.setMessage(e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/betting")
    public ResponseEntity<?> betting(@AuthenticationPrincipal UserDetails userDetails,
                                     @RequestBody BettingRequest bettingRequest) {
        Response response = new Response("OK", "OK");
        Map<String, Object> data = new HashMap<>();
        try {
            User user = userService.findByUserName(userDetails.getUsername());

            Long zodiacGameId = bettingRequest.getZodiacGameId();
            String zodiacCardIdBetting = bettingRequest.getZodiacCardId();
            Long totalIcoinBetting = bettingRequest.getTotalIcoin();
            Long facebookUserIdBetting = user.getId();

            ZodiacGame zodiacGame = zodiacGameService.findById(zodiacGameId);

            // Kiểm tra xem game còn đang ở trong trạng thái cho phép đặt cược không
            if("NEW".equals(zodiacGame.getStatus())) {

                List<String> zodiacCardBetting = zodiacGameHistoryService.getZodiacCardsBetting(facebookUserIdBetting, zodiacGameId);

                // Kiểm tra nếu đặt chưa quá 4 lá thì cho phép đặt tiếp
                if(zodiacCardBetting != null && (zodiacCardBetting.size() < 4 || zodiacCardBetting.contains(zodiacCardIdBetting))) {
                    Long remainingIcoin = user.getTotalPlay();

                    // Kiểm tra xem còn đủ tiền để đặt cược không
                    if(remainingIcoin >= totalIcoinBetting && totalIcoinBetting > 0) {

                        ZodiacCard zodiacCard = zodiacCardService.findByIdAndCache(zodiacCardIdBetting);
                        ZodiacGameHistory zodiacGameHistory = zodiacGameHistoryService.newZodiacGameHistory(user, zodiacGame, zodiacCard, totalIcoinBetting);

                        user.setTotalPlay(remainingIcoin - totalIcoinBetting);
                        userService.saveUser(user);

                        // Đồng bộ icoin sang firebase
                        zodiacGameFirebaseService.updateTotalIcoin(user.getId(), user.getTotalPlay());

                        zodiacGameHistory.setStatus("PENDING");
                        zodiacGameHistoryService.save(zodiacGameHistory);

                        ZodiacCardDTO zodiacCardDTO = zodiacCardService.parseToDTOFromEntity(zodiacCard);
                        zodiacGameFirebaseService.betting(facebookUserIdBetting, zodiacCardDTO, totalIcoinBetting);

                        data.put("remainingIcoin", user.getTotalPlay());
                    } else {
                        response.setStatus("NOTENOUGH");
                        response.setMessage("Not enough");
                    }
                } else {
                    response.setStatus("FAILED");
                    response.setMessage("Tigira gushika kukarata 4 za Mascot");
                }

            } else {
                response.setStatus("FAILED");
                response.setMessage("The game has ended");
            }
        } catch (Exception e) {
            response.setStatus("FAILED");
            response.setMessage(e.getMessage());
            log.warn("BUGS", e);
        }

        response.setData(data);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/start")
    public ResponseEntity<?> start() {
        Response response = new Response("OK", "OK");
        Map<String, Object> data = new HashMap<>();
        try {
            ZodiacGame zodiacGame;
            if(!zodiacGameService.isGameInProcess()) {
                zodiacGame = zodiacGameService.newZodiacGame();

                // Sync transactionId
                zodiacGameFirebaseService.startGame(zodiacGame.getId(), zodiacGame.getNoGame());
            } else {
                zodiacGame = zodiacGameService.getCurrentGame();

                // Sync transactionId
                zodiacGameFirebaseService.updateTransactionId(zodiacGame.getId());
                response.setStatus("FAILED");
                response.setMessage("The game in progress");
            }
            data.put("transactionId", zodiacGame.getId());
            data.put("noGameToday", zodiacGame.getNoGame());
        } catch (Exception e) {
            log.warn("BUGS",e);;
            response.setStatus("FAILED");
            response.setMessage("FAILED");
        }
        response.setData(data);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/end")
    public ResponseEntity<?> end(@RequestBody EndRequest request) {
        Response response = new Response("OK", "OK");
        Map<String, Object> data = new HashMap<>();
        try {
            ZodiacGame zodiacGame =
                    zodiacGameService.findById(request.getZodiacGameId());
            Map<Long, UserZodiacGameDTO> topUserSortedMap;
            ZodiacCardDTO zodiacCardDTO;

            if("NEW".equals(zodiacGame.getStatus())) {
                List<ZodiacCard> zodiacCards = zodiacCardService.findAllAndCache();

                ZodiacCard zodiacCard = zodiacCardService.randomize(zodiacCards);

                zodiacGame.setZodiacCard(zodiacCard);

                // Đưa vào trạng thái đang xử lí
                zodiacGame.setStatus("PROCESSING");

                zodiacGameService.save(zodiacGame);

                zodiacCardDTO = zodiacCardService.parseToDTOFromEntity(zodiacCard);
                topUserSortedMap = zodiacGameHistoryService.getTopUsersMap(zodiacGame);

                // Sync to firebase
                zodiacGameFirebaseService.endGame(zodiacCardDTO, topUserSortedMap);

//                CompletableFuture.runAsync(() -> zodiacGameService.processResult(zodiacGame));
            }

        } catch (Exception e) {
            response.setStatus("FAILED");
            response.setMessage("FAILED");
            log.warn("BUGS",e);
        }

        return ResponseEntity.ok(response);
    }


    @PostMapping("/process-result")
    public ResponseEntity<?> processResult(@RequestBody EndRequest request) {
        Response response = new Response("OK", "OK");
        try {
            zodiacGameService.processResult(request.getZodiacGameId());
        } catch (Exception e) {
            log.warn("BUGS",e);;
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<?> history() {
        Response response = new Response("OK", "OK");
        Map<String, Object> data = new HashMap<>();
        List<ZodiacGameDTO> zodiacGameDTOList = new ArrayList<>();
        try {
            List<ZodiacGame> zodiacGameList = zodiacGameService.getLatest50Records();
            zodiacGameDTOList = zodiacGameService.parseToDTOFromEntity(zodiacGameList);

        } catch (Exception e) {
            response.setStatus("FAILED");
            response.setMessage("FAILED");
            log.warn("BUGS",e);;
        }

        data.put("zodiacGameList", zodiacGameDTOList);
        response.setData(data);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user-history")
    public ResponseEntity<?> userHistory(@AuthenticationPrincipal UserDetails userDetails) {
        Response response = new Response("OK", "OK");
        Map<String, Object> data = new HashMap<>();
        List<ZodiacGameUserDTO> zodiacGameUserDTOList = new ArrayList<>();
        try {
            User user = userService.findByUserName(userDetails.getUsername());
            List<ZodiacGameHistory> zodiacGameHistories = zodiacGameHistoryService.getLatest200Records(user.getId());

            zodiacGameUserDTOList = zodiacGameHistoryService.entityToDTO(zodiacGameHistories);
        } catch (Exception e) {
            log.warn("BUGS",e);;
        }
        data.put("zodiacGameUserList", zodiacGameUserDTOList);
        response.setData(data);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/top-daily")
    public ResponseEntity<?> topDaily() {
        Response response = new Response("OK", "OK");
        Map<String, Object> data = new HashMap<>();
        List<UserDTO> userDTOs = new ArrayList<>();
        try {
            List<User> users = userService.getTop50UsersWithMaxTotalStar();
            userDTOs = userService.convertUserDTOForTop(users);
        } catch (Exception e) {
            log.warn("BUGS",e);
            response.setStatus("FAILED");
            response.setMessage("FAILED");
        }

        data.put("users", userDTOs);
        response.setData(data);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/top-monthly")
    public ResponseEntity<?> topMonthly() {
        Response response = new Response("OK", "OK");
        Map<String, Object> data = new HashMap<>();
        List<UserDTO> userDTOs = new ArrayList<>();
        try {
            List<User> users = userService.getTop50UsersWithMaxTotalStarMonth();
            userDTOs = userService.convertUserDTOForTop(users);
        } catch (Exception e) {
            log.warn("BUGS",e);
            response.setStatus("FAILED");
            response.setMessage("FAILED");
        }

        data.put("users", userDTOs);
        response.setData(data);
        return ResponseEntity.ok(response);
    }

}
