package server.view;

import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.*;

import server.service.RoomManager;
import server.model.Rectangle;
import server.model.Room;
import server.model.Map;
import server.service.OmokJudgement;
import server.service.StoneManager;

import java.awt.*;
import java.io.*;
import java.net.*;

public class RoomServer extends JPanel {
	private static final long serialVersionUID = -8872638214981277216L;

	private JTextArea ta = new JTextArea(8, 22);

	private boolean isPlaying = false;
	private boolean turn; // 누가 돌을 놓을 차례인지 턴 true: 흑, false: 백
	private Rectangle lastStone;
	private Rectangle recentStone;
	Vector<ServiceThread> s2 = new Vector<>();
	private final int roomId;
	private int port;

	private Map map = new Map();
	private StoneManager stoneManager = new StoneManager();

	public RoomServer(int roomId) {
		setLayout(new FlowLayout());

		this.roomId = roomId;
		add(new JLabel(roomId + "번 방"));
		ta.setFocusable(false);
		add(new JScrollPane(ta));
		this.port = ServerMaster.getPort() + roomId;
		setVisible(true);

		startService();
	}

	public void startService() {
		new ServerThread(port).start();
	}

	public void initGame() {
		map = new Map();
		isPlaying = false;
	}

	/* 사용자를 받아들이는 스레드 */
	private class ServerThread extends Thread {
		ServerSocket listener = null;
		Socket socket = null;
		int portNum;

		public ServerThread(int portNum) {
			this.portNum = portNum;
		}

		public void run() {
			try {
				listener = new ServerSocket(portNum);

			} catch (IOException e) {
				// handleError(e.getMessage()); // 서버 소켓 포트 번호가 사용 불가능할 때의 처리
			}
			// 만들어 진 경우 사용자를 기다림
			while (true) {
				try {
					socket = listener.accept(); // 생성된 경우
					ServiceThread th = new ServiceThread(socket);
					s2.add(th); // 관리할 사용자 저장
					th.start();
				} catch (IOException e) {
					try {
						listener.close();
						socket.close(); // 접근이 안된 경우 서버소켓, 소켓을 모두 close
					} catch (IOException e1) {
						// handleError(e1.getMessage());
					}
					// handleError(e.getMessage());
				}
			}
		}
	}

	/* 게임진행 처리 스레드 */
	private class ServiceThread extends Thread {
		private Socket socket;
		private String userId;
		private boolean stone;
		// 돌을 놓을 차례를 검사

		public ServiceThread(Socket socket) {
			this.socket = socket;
			user_network();
		}

		// 접속한 사용자들의 아이디 수신
		public void user_network() {
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

				userId = in.readLine();
				// 맨처음 ID를 읽어온다.
				// 들어온 유저의 아이디 저장
				int num = RoomManager.getInstance().getRoom(roomId).getNum();

				ta.append(userId + "접속\n");
				ta.setCaretPosition(ta.getText().length());
				ta.append(userId + num + "갱신\n");
				ta.setCaretPosition(ta.getText().length());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

				// 자신이 서비스 중인 클라이언트의 상대 클라이언트에게 아이디 전송
				Room room = RoomManager.getInstance().getRoom(roomId);
				if (room.getNum() == 2)
					broad_cast("6," + room.getParty()[0] + "," + room.getParty()[1]);

				while (true) {
					String engText = in.readLine(); // 읽어 옴 사용자 입력이 들어왔을때
					System.out.println("[RoomServer:LOG" + userId + "] " + engText);
					ta.append(engText + "\n");
					ta.setCaretPosition(ta.getText().length());

					int tmp = checkProtocol(engText);
					if (tmp == 1) {
						broad_cast(engText);
					}

					else if (tmp == 2) { // 무르기 요청이 들어 왔을 때
						send_UndoRequest(this, engText);
					}

					else if (tmp == 20) { // 무르기 거절
						send_Message_Opponent(this, engText);
					}

					else if (tmp == 21) {
						send_undoStone(this, engText);
					}

					else if (tmp == 23) {
						send_undoStone(this, engText);
					}

					else if (tmp == 3) {// 게임 시작 제안
						send_GameStart(this, engText);
					}

					else if (tmp == 7) {// 게임 수락 거절
						GameReject(this, engText);
					}

					else if (tmp == 8) {// 게임 시작 수락
						GameStart(this, engText);
					}

					else if (tmp == 9) {
						if (isPlaying && turn == stone)
							checkStone(this, engText);
					}

					else if (tmp == 11) {
						s2.remove(this);
					}

					else if (tmp == 12) {
						s2.add(this);
					}

					else if (tmp == 13) { // 음성채팅 요청
						String[] msgv = engText.split(",");
						send_Message_Opponent(this, "13," + msgv[1] + "," + socket.getInetAddress().getHostAddress());
					}

					else if (tmp == 14) { // 음성채팅 수락
						send_Message_Opponent(this, "14," + socket.getInetAddress().getHostAddress());
					}
				}
			} catch (Exception e) {
				// 방에서 나감 처리
				RoomManager manager = RoomManager.getInstance();
				Room room = manager.getRoom(roomId);
				if (room != null) {
					broad_cast("0," + room.getParty()[0] + "," + room.getParty()[1]);
					room.leaveParty(userId);
					if (room.getNum() == 0)
						manager.closeRoom(room.getId());
				}

				s2.remove(this);
				if (isPlaying) { // 게임중에 나가면 패배처리
					if (turn) // 흑이면
						broad_cast("92,흰 돌 승리!");
					else
						broad_cast("92,검은 돌 승리!");
					initGame();
				}

				try {
					socket.close();
				} catch (IOException e1) {
					e1.getMessage();
				}
				return;
			}
		}

		public int checkProtocol(String str) {
			StringTokenizer token = new StringTokenizer(str);
			String num = token.nextToken(",");

			return Integer.parseInt(num);
		}

		public void broad_cast(String str) {
			for (int i = 0; i < s2.size(); i++) {
				ServiceThread imsi = (ServiceThread) s2.elementAt(i);
				imsi.send_Message(str);
			}
		}

		public void send_Message(String str) {
			try {
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

				out.write(str + "\n");
				out.flush();
			} catch (IOException e) {
			}
		}

		public void send_Message_Opponent(ServiceThread st, String str) {
			for (int i = 0; i < s2.size(); i++) {
				ServiceThread imsi = (ServiceThread) s2.elementAt(i);

				if (!st.equals(imsi)) {
					imsi.send_Message(str);
				}
			}
		}

		public void send_UndoRequest(ServiceThread st, String str) {
			// 무르기 요청
			if (turn != stone) { // 무르기 undo 1회 시행
				send_Message_Opponent(st, str);
			} else { // 무르기 undo 2회 시행
				send_Message_Opponent(st, "2" + str);
			}
		}

		public void send_undoStone(ServiceThread st, String str) {
			// 무르기 수행 메소드
			if (turn != stone) { // 무르기 undo 2회 시행
				map.setColor(lastStone.getRow(), lastStone.getCol(), Map.EMPTY); // 무르기한 위치 비움
				map.setColor(recentStone.getRow(), recentStone.getCol(), Map.EMPTY); // 무르기한 위치 비움
				send_Message_Opponent(st, str);
			} else { // 무르기 undo 1회 시행
				turn = !turn; // 현재 차례를 바꾼다

				map.setColor(lastStone.getRow(), lastStone.getCol(), Map.EMPTY); // 무르기한 위치 비움

				send_Message_Opponent(st, str);
			}
		}

		public void send_GameStart(ServiceThread st, String str) {
			stone = true; // 게임을 제안한 사용자가 true(흑)
			send_Message_Opponent(st, str);
		}

		public void GameStart(ServiceThread st, String str) {
			stone = false; // 게임 제안을 받아들인 사용자가 false(백)
			turn = true; // 선공 흑
			isPlaying = true; // 게임 시작

			send_Message_Opponent(st, str);
		}

		public void GameReject(ServiceThread st, String str) {
			// 게임 시작하는 것을 거절
			send_Message_Opponent(st, str);
		}

		// 게임 판정
		public void checkStone(ServiceThread st, String str) {
			StringTokenizer token = new StringTokenizer(str);
			token.nextToken(",");

			// 받아온 x, y 좌표를 통해 돌을 놓는다
			String x = token.nextToken(",");
			String y = token.nextToken(",");

			// 마우스클릭 지점(x,y)을 stoneManager를 통해 정확한 위치(circle.x, circle.y)를 얻어온다
			Rectangle rec = stoneManager.getRectangleAt(Integer.parseInt(x), Integer.parseInt(y));
			if (rec == null) {
				st.send_Message("91,다시 놓아주세요");
				return;
			}

			int row = rec.getRow();
			int col = rec.getCol();

			int color = (stone) ? 1 : 2; // 무슨 색 돌을 놓을지

			OmokJudgement judge = new OmokJudgement(map, row, col, color); // 판정!!

			int result = judge.getWinner(); // 판정결과를 받아온다

			if (result == -1) { // 이미 x,y에 돌이 있음
				st.send_Message("91,이미 돌이 존재하는 곳입니다");
			}

			else if (result == -2) { // 금수 (구현 안됨...)
				st.send_Message("91,금수위치에 돌을 놓으셨습니다");
			}

			else if (result == 1) { // 검은 돌 승리
				broad_cast("9," + rec.getX() + "," + rec.getY() + "," + stone); // 서버가 받은 돌을 플레이어들에게 리다이렉션 해줌
				st.broad_cast("92,검은 돌 승리!");
				initGame();
			}

			else if (result == 2) { // 흰 돌 승리
				broad_cast("9," + rec.getX() + "," + rec.getY() + "," + stone); // 서버가 받은 돌을 플레이어들에게 리다이렉션 해줌
				st.broad_cast("92,흰 돌 승리!");
				initGame();
			}

			else { // 아직 승부 안남
				map.setColor(row, col, color); // 서버의 map에 돌 위치와 색을 기록한다
				broad_cast("9," + rec.getX() + "," + rec.getY() + "," + stone); // 서버가 받은 돌을 플레이어들에게 리다이렉션 해줌
				turn = !turn; // 다음 차례는 다른 색
				recentStone = lastStone;
				lastStone = rec; // 현재 둔 돌을 저장해둔다 (무르기 대비)
			}
		}

	}
}
