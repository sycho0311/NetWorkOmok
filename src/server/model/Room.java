package server.model;

public class Room {
	public final static int WAIT = 0;
	public final static int START = 1;
	public final static int MAX_NUM = 2;
	public final static String NO_USER = "no user";

	private int id; // 방 번호
	private String name; // 방 제목
	private int state; // 게임 시작여부
	private int num; // 참여자 수
	private String[] party = new String[MAX_NUM]; // 참여자 목록

	public Room(int id, String name) {
		this.id = id;
		this.name = name;
		state = WAIT;
		for (int i = 0; i < MAX_NUM; i++)
			party[i] = NO_USER;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String[] getParty() {
		return party;
	}

	public boolean addParty(String userId) {
		if (num < MAX_NUM) {
			party[num++] = userId;
			return true;
		} else {
			return false;
		}
	}

	public void leaveParty(String userId) {
		// userId가 저장된 i를 찾는다
		int i;
		for (i = 0; i < num; i++)
			if (party[i].equals(userId))
				break;
		if (i == num) // 못 찾으면 종료
			return;

		// i 이후의 유저이름들을 현재 i쪽으로 한칸 씩 당긴다
		for (; i < num - 1; i++)
			party[i] = party[i + 1];

		// 이외의 사람들은 no user로 초기화
		for (; i < MAX_NUM; i++)
			party[i] = NO_USER;

		num--;
	}

	public int getNum() {
		return num;
	}

	@Override
	public String toString() {
		return String.format("%d;%s;%d;%d", id, name, state, num);
	}

}
