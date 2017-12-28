package server.service;

import server.model.Room;

public class RoomManager {
	public final static int MAX_NUM = 18;
	private static RoomManager instance;

	private Room[] rooms;

	private RoomManager() {
		rooms = new Room[MAX_NUM];
	}

	public synchronized static RoomManager getInstance() {
		if (instance == null)
			instance = new RoomManager();
		return instance;
	}

	public Room getRoom(int roomId) {
		return rooms[roomId - 1];
	}

	public int openRoom(String roomName) {
		for (int i = 0; i < MAX_NUM; i++) {
			if (rooms[i] == null) { // 비었으면 새로운 방을 만든다
				rooms[i] = new Room(i + 1, roomName);
				return i + 1;
			}
		}
		return -1;
	}

	public void closeRoom(int roomId) {
		rooms[roomId - 1] = null;
	}

}
