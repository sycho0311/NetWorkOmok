package client.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import client.model.Room;
import client.view.Observer;
import client.view.lobby.RoomListPanel;

public class RoomManager {
	private static RoomManager instance;

	private static final String SERVER_IP = "localhost";
	private static final int PORT = 8000;

	private Socket socket;
	private BufferedReader in;
	private BufferedWriter out;

	private String userId;
	private Room[] rooms;
	private Vector<String> userList = new Vector<>();
	private List<Observer> observerList = new ArrayList<>();

	private RoomManager() {
		try {
			socket = new Socket(SERVER_IP, PORT); // 연결이 되면 읽고 쓰고 할 수 있는 스트림 생성
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			rooms = new Room[RoomListPanel.NUM];
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public synchronized static RoomManager getInstance() {
		if (instance == null)
			instance = new RoomManager();
		return instance;
	}

	public static String getServerIP() {
		return SERVER_IP;
	}

	public static int getPort() {
		return PORT;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Room getRoom(int index) {
		return rooms[index];
	}

	public void add(int index, Room room) {
		rooms[index] = room;
	}

	public String getUser(int index) {
		return userList.get(index);
	}

	public void addUser(int index, String userId) {
		if (index == 0) // 유저목록이 0부터 들어오므로 index가 0이면 userList를 초기화시킨다
			userList.clear();
		userList.add(userId);
	}

	public int getNumOfUsers() {
		return userList.size();
	}

	public void allow(Observer observer) {
		observerList.add(observer);
	}

	public void notifyDataChanged() {
		for (Observer o : observerList)
			o.update();
	}

	/* 프로토콜 처리함수 */
	public String recvMessage() {
		try {
			String line = in.readLine();
			System.out.println(String.format("[LOG:%s] %s", userId, line));
			return line;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void sendMessage(int protocol, String... msgs) {
		StringBuilder builder = new StringBuilder();
		for (String msg : msgs)
			builder.append(msg + ";");
		try {
			out.write(protocol + "," + builder.toString() + "\n");
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void chat(String text) {
		sendMessage(1, userId + " : " + text);
	}

	public void request(int page) {
		sendMessage(2, String.valueOf(page));
	}

	public void creatRoom(String roomName) {
		sendMessage(3, roomName);
	}

	public void enterRoom(int roomId) {
		sendMessage(4, String.valueOf(roomId), userId);
	}

	public void exitRoom(int roomId) {
		sendMessage(5, String.valueOf(roomId), userId);
	}

	public void requestUserList() {
		sendMessage(6, "");
	}

	public void sendMyUserId() {
		sendMessage(66, userId);
	}

	public synchronized void destroy() {
		if (instance != null) {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			instance = null;
		}
	}

	public void sendEmot(int index) { // 이모티콘 송신
		sendMessage(7, userId, String.valueOf(index));
	}

}
