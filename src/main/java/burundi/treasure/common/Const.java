package burundi.treasure.common;

import org.springframework.stereotype.Service;

@Service
public class Const {
	
	public static boolean PAUSE_GAME = false;
	public static boolean IS_REWARD = true;

	public static void setPauseGame(boolean b) {
		PAUSE_GAME = b;
	}

	public static void setIsReward(boolean b) {
		IS_REWARD = b;
	}
}
