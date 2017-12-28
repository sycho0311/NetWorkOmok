package client.model;

public class Room {
	public final static int WAIT = 0;
	public final static int START = 1;
	public final static int MAX_NUM = 2;

	private int id; // 방 번호
	private String name; // 방 제목
	private int state; // 방 상태
	private int num; // 참여자 수

	public Room(int id) {
		this.id = id;
		state = WAIT;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}
}
