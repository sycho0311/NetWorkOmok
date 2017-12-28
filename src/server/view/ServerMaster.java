package server.view;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import server.model.Room;
import server.service.RoomManager;

public class ServerMaster extends JFrame implements Runnable, ActionListener {
	private static final long serialVersionUID = 1763130574336626273L;

	private JPanel logPanel = new JPanel(new GridLayout(2, 5, 5, 5));
	private JTextArea ta = new JTextArea(18, 22);
	JTextField engField = new JTextField();
	JButton sendBtn = new JButton("전송");

	private static final int PORT = 8000;
	private ServerSocket listener;
	private Vector<ServiceThread> s = new Vector<>();
	private RoomServer[] roomServers = new RoomServer[18];

	public ServerMaster() {
		setTitle("ServerMaster");
		setSize(1280, 720);
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null); // 가운데 배치

		ta.setFocusable(false);
		engField.addActionListener(this);
		sendBtn.addActionListener(this);

		/* 그리드 레이아웃: 전체서버, 룸서버 로그창 */
		JPanel serverPanel = new JPanel();
		serverPanel.add(new JLabel("메인 서버"));
		serverPanel.add(new JScrollPane(ta));
		logPanel.add(serverPanel);
		add(logPanel, BorderLayout.CENTER);

		/* 아래쪽: 공지사항 메시지 전송 */
		Box bottomBox = Box.createHorizontalBox();
		bottomBox.add(engField);
		bottomBox.add(sendBtn);
		add(bottomBox, BorderLayout.SOUTH);

		setVisible(true);
		new Thread(this).start();
	}

	@Override
	public void run() {
		for (int i = 0; i < 4; i++) {
			JPanel subPanel = new JPanel(new GridLayout(2, 1));
			roomServers[i] = new RoomServer(i + 1);
			roomServers[i + 4] = new RoomServer(i + 5);
			subPanel.add(roomServers[i]);
			subPanel.add(roomServers[i + 4]);
			logPanel.add(subPanel);
		}
		for (int i = 8; i < 13; i++) {
			JPanel subPanel = new JPanel(new GridLayout(2, 1));
			roomServers[i] = new RoomServer(i + 1);
			roomServers[i + 5] = new RoomServer(i + 6);
			subPanel.add(roomServers[i]);
			subPanel.add(roomServers[i + 5]);
			logPanel.add(subPanel);
		}
		revalidate();

		try {
			listener = new ServerSocket(PORT);
		} catch (IOException e) { // 서버 소켓 포트 번호가 사용 불가능할 때의 처리
			e.printStackTrace();
			System.exit(1);
		}

		// 전체 채널로의 사용자들 입장
		while (true) {
			try {
				Socket socket = listener.accept(); // 생성된 경우
				ServiceThread th = new ServiceThread(socket);
				s.add(th); // 사용자 저장
				th.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private class ServiceThread extends Thread {
		private Socket socket;
		private String userId;
		private BufferedReader in;
		private BufferedWriter out;

		public ServiceThread(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			RoomManager manager = RoomManager.getInstance();
			try {
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

				while (true) {
					String line = in.readLine(); // 읽어 옴 사용자 입력이 들어왔을때
					System.out.println("[SERVER:LOG] " + line);
					ta.append(line + "\n");
					ta.setCaretPosition(ta.getText().length());

					StringTokenizer token = new StringTokenizer(line, ",");
					String num = token.nextToken();
					String msg = token.nextToken();

					int protocol = Integer.parseInt(num); // 앞글자의 숫자를 보고 행동
					String[] msgv = msg.split(";"); // msg를 ;로 구분하여 가져옴

					/* 프로토콜 처리 */
					if (protocol == 1) { // 채팅 메시지 전송
						// 귓속말인지 검사
						String[] content = msgv[0].split(" : "); // 보낸 사람 제거
						int pos; // ']'의 위치
						if (content[1].startsWith("[") && (pos = content[1].indexOf("]")) != -1) { // [targetId]
							String targetId = content[1].substring(1, pos);

							// targetId를 가진 service를 찾는다
							ServiceThread targetService = null;
							for (ServiceThread service : s) {
								if (service.getUserId().equals(targetId)) {
									targetService = service;
									break;
								}
							}
							if (targetService != null) { // service 찾기 성공
								send_Message(1, msg);
								if (targetService != this)
									targetService.send_Message(1, msg);
							} else // targetId를 가진 service 찾기 실패
								send_Message(1, targetId + " 님이 로비에 없습니다.");
						} else // 귓속말이 아니면 브로드캐스트
							broad_cast(1, msg);
					}

					else if (protocol == 2) { // 방 정보 요청
						int page = Integer.parseInt(msgv[0]);
						for (int i = 0; i < 6; i++) {
							Room room = manager.getRoom(page * 6 + i + 1);
							if (room != null) // roomId를 포함한 방 정보를 보낸다
								send_Message(2, room.toString());
							else // null상태인 roomId만 보낸다
								send_Message(2, String.valueOf(page * 6 + i + 1));
						}
					}

					else if (protocol == 3) { // 방 생성 요청
						int roomId = manager.openRoom(msgv[0]);
						send_Message(3, String.valueOf(roomId)); // -1을 보내면 게임방에 입장하지 못한다
						if (roomId != -1)
							broad_cast(22, ""); // 유저에게 방목록 갱신됨을 알림
					}

					else if (protocol == 4) { // 방 들어가기 요청
						int roomId = Integer.valueOf(msgv[0]);
						Room room = manager.getRoom(roomId);
						if (room != null && room.addParty(msgv[1]))
							send_Message(4, String.valueOf(roomId));
						else
							send_Message(4, "-1");
					}

					else if (protocol == 5) { // 방 나가기 요청
						int roomId = Integer.valueOf(msgv[0]);
						Room room = manager.getRoom(roomId);
						room.leaveParty(msgv[1]);
						if (room.getNum() <= 0) {
							manager.closeRoom(room.getId());
							broad_cast(22, ""); // 유저에게 방목록 갱신됨을 알림
						}
					}

					else if (protocol == 6) { // userId 리스트 요청
						for (int i = 0; i < s.size(); i++)
							broad_cast(6, i + ";" + s.get(i).getUserId());
					}

					else if (protocol == 66) { // userId 받기 (사용자가 로비 진입 시 보냄)
						userId = msgv[0];
					}

					else if (protocol == 7) {
						broad_cast(7, msg);
					}
				}
			} catch (IOException | NullPointerException e) {
				try {
					s.remove(this);
					for (int i = 0; i < s.size(); i++)
						broad_cast(6, i + ";" + s.get(i).getUserId());
					socket.close();
					// Client 중 하나가 종료할 경우 그 Thread의 socket만 종료
				} catch (IOException e1) {
					e1.getMessage();
				}
				return;
			}
		}

		private void send_Message(int protocol, String... msgs) {
			StringBuilder builder = new StringBuilder();
			for (String msg : msgs)
				builder.append(msg + ";");
			try {
				out.write(protocol + "," + builder.toString() + "\n");
				out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void broad_cast(int protocol, String str) {
			for (int i = 0; i < s.size(); i++)
				s.get(i).send_Message(protocol, str);
		}

		public String getUserId() {
			return userId;
		}
	}

	// 전체 유저들에 대한 서버 알림
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == engField || e.getSource() == sendBtn) {
			for (int i = 0; i < s.size(); i++) {
				ServiceThread imsi = s.get(i);
				imsi.send_Message(0, "공지사항 : " + engField.getText());
			}
			engField.setText("");
		}
	}

	public static int getPort() {
		return PORT;
	}

	public static void main(String[] args) {
		new ServerMaster();
	}

}
