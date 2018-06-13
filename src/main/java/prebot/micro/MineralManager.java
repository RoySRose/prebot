package prebot.micro;

import java.util.ArrayList;


public class MineralManager {

	private static MineralManager instance = new MineralManager();
	
	/// static singleton 객체를 리턴합니다
	public static MineralManager Instance() {
		return instance;
	}
	// 근처의 미네랄 정보 리스트
	public ArrayList<Minerals> minerals = new ArrayList<Minerals>();
}

