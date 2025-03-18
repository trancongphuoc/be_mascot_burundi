package burundi.treasure.firebase;

import burundi.treasure.model.User;
import burundi.treasure.model.dto.UserZodiacGameDTO;
import burundi.treasure.model.dto.zodiacgame.ZodiacCardDTO;
import burundi.treasure.service.UserService;
import burundi.treasure.service.zodiacgame.ZodiacCardService;
import burundi.treasure.service.zodiacgame.ZodiacGameHistoryService;
import burundi.treasure.service.zodiacgame.ZodiacGameService;
import com.google.api.core.ApiFuture;
import com.google.firebase.database.*;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

@Log
@Service
public class ZodiacGameFirebaseService {

    private static final String ZODIAC_GAME = "zodiacGame";
    private static final String PLAYERS = "players";
    private static final String PLAYERS_BETTING = "playersBetting";
    private static final String LAST_UPDATE = "lastUpdate";
    private static final String ZODIAC_CARDS = "zodiacCards";
    private static final String ZODIAC_CARD = "zodiacCard";

    private static final String ZODIAC_CARDS_RECENT = "zodiacCardsRecent";
    private static final String COUNTER = "counter";
    private static final String TOTAL_ICOIN_BETTING = "totalIcoinBetting";
    private static final String NO_BETTING_TODAY = "noBettingToday";
    private static final String NAME = "name";
    private static final String PROFILE_IMAGE_LINK = "profileImageLink";
    private static final String TOTAL_ICOIN_WIN_TODAY = "totalIcoinWinToday";
    private static final String TOTAL_ICOIN_WIN_MONTHDAY = "totalIcoinWinMonthday";

    private static final String TOTAL_ICOIN = "totalIcoin";

    private static final String STATE = "state";
    private static final String TOP_USERS = "topUsers";
    private static final String TRANSACTION_ID = "transactionId";
    private static final String NO_GAME_TO_DAY = "noGameToday";
    private static final String START_TIME = "startTime";
    private static final String BETTING_CARDS = "bettingCards";
    private static final String TOP_5 = "top5Win";

    private static final DatabaseReference ZODIAC_GAME_REF = FirebaseDatabase.getInstance().getReference(ZODIAC_GAME);

    @Autowired
    private UserService userService;

    @Autowired
    private ZodiacGameHistoryService zodiacGameHistoryService;

    public void joinGame(Long userId) {
        User user = userService.findById(userId);
        UserZodiacGameDTO userDTO = new UserZodiacGameDTO(user);
        userDTO.setNoBettingToday(zodiacGameHistoryService.getNoBettingToday(userId));
        userDTO.setTotalIcoinWinToday(zodiacGameHistoryService.getTotalIcoinWinToday(userId));
        joinGame(userDTO);
    }

    public void joinGame(UserZodiacGameDTO userDTO) {
        // Get references to the Firebase nodes
        DatabaseReference userRef = ZODIAC_GAME_REF.child(PLAYERS).child(userDTO.getIdString());

        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put(NAME, userDTO.getName());
        userUpdates.put(PROFILE_IMAGE_LINK, userDTO.getProfileImageLink());
        userUpdates.put(LAST_UPDATE, System.currentTimeMillis());
        userUpdates.put(NO_BETTING_TODAY, userDTO.getNoBettingToday());
        userUpdates.put(TOTAL_ICOIN_WIN_TODAY, userDTO.getTotalIcoinWinToday());
        userUpdates.put(TOTAL_ICOIN_WIN_MONTHDAY, userDTO.getTotalIcoinWinMonthday());

        userUpdates.put(TOTAL_ICOIN, userDTO.getTotalIcoin());

        // Chỉ cập nhật các trường được chỉ định
        userRef.updateChildrenAsync(userUpdates);

        //Ghi đè toàn bộ giá trị
//        userRef.setValueAsync(userUpdates);
    }

    public void exitGame(Long userId) {
        // Tham chiếu đến nút người dùng trong trò chơi Zodiac

        // Xóa nút người dùng
        ZODIAC_GAME_REF.child(PLAYERS).child(userId.toString()).removeValueAsync();
    }


//    public void updateTotalIcoin(Long userId, Long remainingIcoin) {
//        ZODIAC_GAME_REF.child(PLAYERS).child(userId.toString()).child(TOTAL_ICOIN).setValueAsync(remainingIcoin);
//    }

    public void updateTotalIcoin(Long userId, Long remainingIcoin) {
        DatabaseReference playerRef = ZODIAC_GAME_REF.child(PLAYERS).child(userId.toString());

        playerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    playerRef.child(TOTAL_ICOIN).setValueAsync(remainingIcoin);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println("Error checking TOTAL_ICOIN node: " + databaseError.getMessage());
            }
        });
    }


    public void doNothing(Long userId) {
        DatabaseReference refZodiacGameUser = ZODIAC_GAME_REF.child(PLAYERS).child(userId.toString());

        // Kiểm tra sự tồn tại của refZodiacGameUser
        refZodiacGameUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    refZodiacGameUser.child(LAST_UPDATE).setValueAsync(System.currentTimeMillis());
                } else {
                    joinGame(userId);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }

    public void betting(Long userId, ZodiacCardDTO zodiacCard, Long totalIcoinBetting) {
        DatabaseReference zodiacGameRef = ZODIAC_GAME_REF;
        DatabaseReference playerRef = zodiacGameRef.child(PLAYERS).child(userId.toString());

        playerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot playerSnapshot) {
                if (!playerSnapshot.exists()) {
                    joinGame(userId);
                }
                handleBettingTransaction(playerRef, zodiacCard, zodiacCard.getId(), totalIcoinBetting);

                DatabaseReference playerBettingRef = zodiacGameRef.child(PLAYERS_BETTING).child(userId.toString());
                playerBettingRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()) {
                            User user = userService.findById(userId);
                            UserZodiacGameDTO userDTO = new UserZodiacGameDTO(user);
                            userDTO.setTotalIcoinWinToday(zodiacGameHistoryService.getTotalIcoinWinToday(userId));

                            DatabaseReference userRef = ZODIAC_GAME_REF.child(PLAYERS_BETTING).child(userId.toString());

                            Map<String, Object> userUpdates = new HashMap<>();
                            userUpdates.put(NAME, userDTO.getName());
                            userUpdates.put(PROFILE_IMAGE_LINK, userDTO.getProfileImageLink());
                            userUpdates.put(LAST_UPDATE, System.currentTimeMillis());
                            userUpdates.put(TOTAL_ICOIN_WIN_TODAY, userDTO.getTotalIcoinWinToday());
                            userUpdates.put(TOTAL_ICOIN_WIN_MONTHDAY, userDTO.getTotalIcoinWinMonthday());

                            // Chỉ cập nhật các trường được chỉ định
                            userRef.updateChildrenAsync(userUpdates);
                        }

                        DatabaseReference zodiacCardBettingRef = playerBettingRef.child(BETTING_CARDS).child(zodiacCard.getId());
                        zodiacCardBettingRef.runTransaction(new Transaction.Handler() {
                            @Override
                            public Transaction.Result doTransaction(MutableData currentData) {
                                if (currentData.getValue() == null) {
                                    zodiacCard.setTotalIcoinBetting(totalIcoinBetting);
                                    zodiacCard.setLastUpdate(new Date().getTime());
                                    currentData.setValue(zodiacCard);
                                } else {
                                    ZodiacCardDTO currentValue = currentData.getValue(ZodiacCardDTO.class);
                                    long currentTotalIcoin = currentValue.getTotalIcoinBetting();
                                    currentValue.setTotalIcoinBetting(currentTotalIcoin + totalIcoinBetting);
                                    currentData.setValue(currentValue);
                                }
                                return Transaction.success(currentData);
                            }

                            @Override
                            public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors.
            }
        });
    }

    private void handleBettingTransaction(DatabaseReference playerRef, ZodiacCardDTO zodiacCard, String zodiacCardId, Long totalIcoinBetting) {
        playerRef.child(BETTING_CARDS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot zodiacCardsBettingSnapshot) {
                processZodiacCardTransaction(playerRef, zodiacCard, zodiacCardId, totalIcoinBetting);

                updatePlayerTotalIcoin(playerRef, totalIcoinBetting);

                if (!zodiacCardsBettingSnapshot.exists()) {
                    updateNoBettingToday(playerRef);
                }

                updateLastUpdate(playerRef);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                handleError(databaseError);
            }
        });
    }

    private void processZodiacCardTransaction(DatabaseReference playerRef, ZodiacCardDTO zodiacCard,
                                              String zodiacCardId, Long totalIcoinBetting) {
        DatabaseReference zodiacCardBettingRef = playerRef.child(BETTING_CARDS).child(zodiacCardId);
        zodiacCardBettingRef.runTransaction(new Transaction.Handler() {
            boolean isFirstBet = true;

            @Override
            public Transaction.Result doTransaction(MutableData currentData) {
                if (currentData.getValue() == null) {
                    zodiacCard.setTotalIcoinBetting(totalIcoinBetting);
                    currentData.setValue(zodiacCard);
                } else {
                    isFirstBet = false;
                    ZodiacCardDTO currentValue = currentData.getValue(ZodiacCardDTO.class);
                    long currentTotalIcoin = currentValue.getTotalIcoinBetting();
                    currentValue.setTotalIcoinBetting(currentTotalIcoin + totalIcoinBetting);
                    currentData.setValue(currentValue);
                }
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {
                // If this was the first bet, update the zodiac card counter
                if (isFirstBet) {
                    updateZodiacCardCounter(zodiacCardId);
                }
            }
        });
    }

    private void updateZodiacCardCounter(String zodiacCardId) {
        DatabaseReference counterZodiacCardRef = FirebaseDatabase.getInstance().getReference(ZODIAC_GAME)
                .child(ZODIAC_CARDS).child(zodiacCardId).child(COUNTER);

        counterZodiacCardRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData currentData) {
                if (currentData.getValue() == null) {
                    currentData.setValue(1);
                } else {
                    currentData.setValue((Long) currentData.getValue() + 1);
                }
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {
                // Handle transaction completion
            }
        });
    }

    private void updatePlayerTotalIcoin(DatabaseReference playerRef, Long totalIcoinBetting) {
        DatabaseReference totalIcoinPlayerRef = playerRef.child(TOTAL_ICOIN_BETTING);
        totalIcoinPlayerRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData currentData) {
                if (currentData.getValue() == null) {
                    currentData.setValue(totalIcoinBetting);
                } else {
                    currentData.setValue((Long) currentData.getValue() + totalIcoinBetting);
                }
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {
                // Handle transaction completion
            }
        });
    }

    private void updateNoBettingToday(DatabaseReference playerRef) {
        DatabaseReference noBettingTodayPlayerRef = playerRef.child(NO_BETTING_TODAY);

        noBettingTodayPlayerRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData currentData) {
                if (currentData.getValue() == null) {
                    currentData.setValue(1);
                } else {
                    currentData.setValue((Long) currentData.getValue() + 1);
                }
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {
                // Handle transaction completion
            }
        });
    }

    private void updateLastUpdate(DatabaseReference playerRef) {
        playerRef.child(LAST_UPDATE).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                mutableData.setValue(System.currentTimeMillis());
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                // Handle transaction completion
            }
        });
    }

    private void handleError(DatabaseError error) {
        log.log(Level.WARNING, "BUGS", error.getMessage());
    }

    public void startGame(Long transactionId, Long noGameToday) throws ExecutionException, InterruptedException, TimeoutException {

        Map<String, Object> stateUpdates = new HashMap<>();
        stateUpdates.put(TRANSACTION_ID, transactionId);
        stateUpdates.put(NO_GAME_TO_DAY, noGameToday);
//        stateUpdates.put(START_TIME, System.currentTimeMillis());

        // Lấy transaction id và lưu vào node firebase
        ApiFuture<Void> future = ZODIAC_GAME_REF.child(STATE).updateChildrenAsync(stateUpdates);

        // Đợi cho đến khi hoàn thành
        future.get(10, TimeUnit.SECONDS);
    }

    public void updateTransactionId(Long transactionId) throws ExecutionException, InterruptedException, TimeoutException {

        Map<String, Object> stateUpdates = new HashMap<>();
        stateUpdates.put(TRANSACTION_ID, transactionId);

        // Lấy transaction id và lưu vào node firebase
        ApiFuture<Void> future = ZODIAC_GAME_REF.child(STATE).updateChildrenAsync(stateUpdates);

        // Đợi cho đến khi hoàn thành
        future.get(10, TimeUnit.SECONDS);
    }

    public void endGame(ZodiacCardDTO zodiacCardDTO, Map<Long, UserZodiacGameDTO> topUsersMap) throws ExecutionException, InterruptedException, TimeoutException {

        // Chuyển đổi Map với key Long sang Map với key String
        Map<String, UserZodiacGameDTO> convertedTopUsersMap = new HashMap<>();
        for (Map.Entry<Long, UserZodiacGameDTO> entry : topUsersMap.entrySet()) {
            entry.getValue().setAddTime(null);
            convertedTopUsersMap.put(String.valueOf(entry.getKey()), entry.getValue());
        }

        Map<String, Object> stateUpdates = new HashMap<>();
        stateUpdates.put(ZODIAC_CARD, zodiacCardDTO);
        stateUpdates.put(TOP_USERS, convertedTopUsersMap);

        ApiFuture<Void> future = ZODIAC_GAME_REF.child(STATE).updateChildrenAsync(stateUpdates);

        // Đợi cho đến khi hoàn thành
        future.get(10, TimeUnit.SECONDS);

        // Thêm zodiacCard vào danh sách kết quả gần đây
        addZodiacCardsRecent(zodiacCardDTO);
    }

//    public void addZodiacCardsRecent(ZodiacCardDTO zodiacCard) {
//        try {
//            DatabaseReference zodiacGameRef = FirebaseDatabase.getInstance().getReference(ZODIAC_GAME);
//
//            // Lấy danh sách các zodiacCard hiện có trong zodiacCardsRecent
//            DatabaseReference recentCardsRef = zodiacGameRef.child(STATE).child(ZODIAC_CARDS_RECENT);
//            List<ZodiacCardDTO> recentZodiacCards = new ArrayList<>();
//
//            // Đọc dữ liệu hiện tại từ Firebase
//            DataSnapshot snapshot = recentCardsRef.get().getResult();
//            if (snapshot.exists()) {
//                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
//                    ZodiacCardDTO card = childSnapshot.getValue(ZodiacCardDTO.class);
//                    if (card != null) {
//                        recentZodiacCards.add(card);
//                    }
//                }
//            }
//
//            // Thêm zodiacCard mới vào danh sách
//            zodiacCard.setLastUpdate(System.currentTimeMillis());
//            recentZodiacCards.add(zodiacCard);
//
//            // Sắp xếp danh sách theo thứ tự thời gian giảm dần
//            Collections.sort(recentZodiacCards, (a, b) -> Long.compare(b.getLastUpdate(), a.getLastUpdate()));
//
//            // Chỉ giữ lại 4 zodiacCard mới nhất
//            List<ZodiacCardDTO> latestZodiacCards = recentZodiacCards.subList(0, Math.min(recentZodiacCards.size(), 4));
//
//            // Cập nhật vào Firebase
//            recentCardsRef.setValueAsync(latestZodiacCards);
//        } catch (DatabaseException e) {
//            System.err.println("Error add zodiac cards recent: " + e.getMessage());
//        }
//    }


    public void addZodiacCardsRecent(ZodiacCardDTO zodiacCard) {
        try {
            DatabaseReference recentCardsRef = ZODIAC_GAME_REF.child(STATE).child(ZODIAC_CARDS_RECENT);

            // Lấy danh sách các zodiacCard hiện có trong zodiacCardsRecent
            recentCardsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    List<ZodiacCardDTO> recentZodiacCards = new ArrayList<>();

                    // Đọc dữ liệu hiện tại từ Firebase
                    if (snapshot.exists()) {
                        for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                            ZodiacCardDTO card = childSnapshot.getValue(ZodiacCardDTO.class);
                            if (card != null) {
                                recentZodiacCards.add(card);
                            }
                        }
                    }

                    // Thêm zodiacCard mới vào danh sách
                    zodiacCard.setLastUpdate(System.currentTimeMillis());
                    recentZodiacCards.add(zodiacCard);

                    // Sắp xếp danh sách theo thứ tự thời gian giảm dần
                    recentZodiacCards.sort((a, b) -> Long.compare(b.getLastUpdate(), a.getLastUpdate()));

                    // Chỉ giữ lại 4 zodiacCard mới nhất
                    List<ZodiacCardDTO> latestZodiacCards = recentZodiacCards.subList(0, Math.min(recentZodiacCards.size(), 4));

                    // Cập nhật vào Firebase
                    recentCardsRef.setValueAsync(latestZodiacCards);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    log.log(Level.WARNING, "BUGS", error);
                }
            });
        } catch (DatabaseException e) {
            log.log(Level.WARNING, "BUGS", e);
        }
    }

    public void updateZodiacCards(Map<String, ZodiacCardDTO> zodiacCardMap) {
        ZODIAC_GAME_REF.child(ZODIAC_CARDS).setValueAsync(zodiacCardMap);
    }

//    public static void updateTop5WinTotalByDate(List<ZodiacGameUserDTO> zodiacGameUsers) {
//        DatabaseReference refZodiacGame = ZODIAC_GAME_REF;
//        refZodiacGame.child(TOP_5).setValueAsync(zodiacGameUsers);
//    }

}
