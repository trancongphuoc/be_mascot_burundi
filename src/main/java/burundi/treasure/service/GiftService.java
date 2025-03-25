package burundi.treasure.service;

import burundi.treasure.common.Utils;
import burundi.treasure.config.ConfigProperties;
import burundi.treasure.model.Config;
import burundi.treasure.model.Gift;
import burundi.treasure.repository.ConfigRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class GiftService {

    public static Map<String, Gift> gifts;
    @Autowired
    private ConfigProperties properties;

    @Autowired
    private ConfigRepository configRepository;

    private static Long FBU_1_M = 500_000L;
    private static Long FBU_2_M = 20_000L;
    private static Long FBU_3_M = 5_000L;

    private static Long FBU_1_D = 50_000L;
    private static Long FBU_2_D = 10_000L;
    private static Long FBU_3_D = 5_000L;

    @PostConstruct
    public void init() {
//        configRepository.save(new Config("FBU_1_M", FBU_1_M.toString()));
//        configRepository.save(new Config("FBU_2_M", FBU_2_M.toString()));
//        configRepository.save(new Config("FBU_3_M", FBU_3_M.toString()));
//        configRepository.save(new Config("FBU_1_D", FBU_1_D.toString()));
//        configRepository.save(new Config("FBU_2_D", FBU_2_D.toString()));
//        configRepository.save(new Config("FBU_3_D", FBU_3_D.toString()));

        FBU_1_M = Utils.getLong((configRepository.findById("FBU_1_M").map(Config::getCodeValue).orElse(null)), 500_000L);
        FBU_2_M = Utils.getLong((configRepository.findById("FBU_2_M").map(Config::getCodeValue).orElse(null)), 20_000L);
        FBU_3_M = Utils.getLong((configRepository.findById("FBU_3_M").map(Config::getCodeValue).orElse(null)), 5_000L);

        FBU_1_D = Utils.getLong((configRepository.findById("FBU_1_D").map(Config::getCodeValue).orElse(null)), 50_000L);
        FBU_2_D = Utils.getLong((configRepository.findById("FBU_2_D").map(Config::getCodeValue).orElse(null)), 10_000L);
        FBU_3_D = Utils.getLong((configRepository.findById("FBU_3_D").map(Config::getCodeValue).orElse(null)), 5_000L);

        Map<String, Double> probabilities = properties.getProbabilities();

        gifts = new HashMap<>();
        gifts.put("FBU_1_M", new Gift("FBU_1_M", "500.000 Fbu", FBU_1_M, "FBU",probabilities.getOrDefault("FBU_1_M", 0.0d)));
        gifts.put("FBU_2_M", new Gift("FBU_2_M", "20.000 Fbu", FBU_2_M, "FBU",probabilities.getOrDefault("FBU_2_M", 0.0d)));
        gifts.put("FBU_3_M", new Gift("FBU_3_M", "5.000 Fbu", FBU_3_M, "FBU",probabilities.getOrDefault("FBU_3_M", 0.0)));

        gifts.put("FBU_1_D", new Gift("FBU_1_D", "20.000 Fbu", FBU_1_D, "FBU",probabilities.getOrDefault("FBU_1_D", 0.0d)));
        gifts.put("FBU_2_D", new Gift("FBU_2_D", "5.000 Fbu", FBU_2_D, "FBU",probabilities.getOrDefault("FBU_2_D", 0.0d)));
        gifts.put("FBU_3_D", new Gift("FBU_3_D", "1.000 Fbu", FBU_3_D, "FBU",probabilities.getOrDefault("FBU_3_D", 0.0)));
    }

    public static Gift randomize() {
        // Tính tổng xác suất của tất cả các phần tử
        double totalProbability = gifts.values().stream().mapToDouble(Gift::getProbability).sum();

        // Sinh một số ngẫu nhiên từ 0 đến tổng xác suất
        double randomProbability = new Random().nextDouble() * totalProbability;

        // Lặp qua danh sách các phần tử và cộng dần giá trị xác suất
        double cumulativeProbability = 0;
        for (Gift gift : gifts.values()) {
            cumulativeProbability += gift.getProbability();
            if (randomProbability <= cumulativeProbability) {
                return gift;
            }
        }

        // Trả về phần tử cuối cùng nếu không tìm thấy phần tử thích hợp
        return gifts.get("UNLUCKY");
    }

    public static void main(String[] args) {
        for (int i = 0; i < 1000; i++) {
            Gift g = randomize();
            System.out.println(g.getId());
        }
    }
}
