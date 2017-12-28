package client.view.lobby;

import java.awt.*;
import java.awt.event.*;
import java.util.StringTokenizer;

import javax.swing.*;

import client.model.Room;
import client.service.EmoticonManager;
import client.service.RoomManager;
import client.view.EmotSelectDialog;
import client.view.MainFrame;

public class LobbyPanel extends JPanel implements Runnable, ActionListener {
	private static final long serialVersionUID = -648930873270643818L;

	private JTextPane ta = new JTextPane();
	private JTextField tf = new JTextField();
	private JButton emotBtn = new JButton(new ImageIcon("img/happy.png"));
	private JButton sendBtn = new JButton(new ImageIcon("img/Transport_mini.png"));

	public LobbyPanel() {
		MainFrame frame = MainFrame.getInstance();
		int width = frame.getWidth();
		int height = frame.getHeight();
		setLayout(new BorderLayout());

		/* NORTH : 버튼 Box -방만들기, 빠른시작, 종료 버튼 */
		add(new BtnPanel(width, height / 20), BorderLayout.NORTH);

		/* EAST : 유저목록 패널 추가 */
		add(new UserListPanel(width * 1 / 5, height * 11 / 20), BorderLayout.WEST);

		/* CENTER : 방 목록, 채팅 패널 추가 */
		add(new RoomListPanel(width * 4 / 5, height * 11 / 20), BorderLayout.CENTER);

		/* SOUTH : 채팅 패널 추가 */
		JPanel chatPanel = new JPanel();
		chatPanel.setPreferredSize(new Dimension(width, height * 8 / 20));
		chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
		// 채팅 로그
		ta.setPreferredSize(new Dimension(width - 30, (int) (height * 0.34)));
		ta.setDisabledTextColor(new Color(0, 0, 0));
		ta.setEnabled(false);
		JScrollPane scrollPane = new JScrollPane(ta);
		chatPanel.add(scrollPane);
		// 입력
		Box inputBox = Box.createHorizontalBox();
		tf.addActionListener(this);
		emotBtn.addActionListener(this);
		emotBtn.setPreferredSize(new Dimension(44, 0));
		sendBtn.addActionListener(this);
		sendBtn.setPreferredSize(new Dimension(88, 0));
		inputBox.add(tf);
		inputBox.add(emotBtn);
		inputBox.add(sendBtn);
		chatPanel.add(inputBox);
		add(chatPanel, BorderLayout.SOUTH);

		new Thread(this).start();
	}

	@Override
	public void run() {
		// 소켓 생성, BufferedReader, Writer 생성
		RoomManager manager = RoomManager.getInstance();
		manager.sendMyUserId();
		manager.request(0); // 화면에 표시할 방 정보 요청
		manager.requestUserList(); // 유저목록 요청

		while (true) {
			String line = manager.recvMessage(); // 읽어 옴 사용자 입력이 들어왔을때
			if (line == null) {
				MainFrame.getInstance().gotoLogin();
				break;
			}

			StringTokenizer token = new StringTokenizer(line, ",");
			String num = token.nextToken();
			String msg = token.nextToken();
			String[] msgv = msg.split(";");
			int protocol = Integer.parseInt(num); // 앞글자의 숫자를 보고 행동

			/* 프로토콜 처리 */
			if (protocol == 0) {
				// 공지사항
				JOptionPane.showMessageDialog(null, msg, "공지사항", JOptionPane.WARNING_MESSAGE);
				ta.setCaretPosition(ta.getDocument().getLength());
				ta.replaceSelection(msgv[0] + "\n");
			} else if (protocol == 1) { // 채팅
				ta.setCaretPosition(ta.getDocument().getLength());
				ta.replaceSelection(msgv[0] + "\n");
			} else if (protocol == 2) { // 방 목록 갱신
				int roomId = Integer.valueOf(msgv[0]);
				Room room = null;
				if (msgv.length > 1) { // Room 정보가 있으면
					room = new Room(roomId);
					room.setName(msgv[1]);
					room.setState(Integer.valueOf(msgv[2]));
					room.setNum(Integer.valueOf(msgv[3]));
				}
				manager.add((roomId + 5) % 6, room);
				manager.notifyDataChanged(); // observer가 UI를 update하도록 한다
			} else if (protocol == 22) { // 서버가 방 목록이 갱신됨을 알림
				manager.request(RoomListPanel.getPage());
			} else if (protocol == 3) { // 자신이 만든 게임방에 입장
				int roomId = Integer.valueOf(msgv[0]);
				if (roomId != -1)
					manager.enterRoom(roomId);
			} else if (protocol == 4) { // 게임방 입장
				int roomId = Integer.valueOf(msgv[0]);
				if (roomId != -1)
					MainFrame.getInstance().gotoGameRoom(roomId);
				else
					manager.request(0);
			} else if (protocol == 6) { // 유저목록을 받는다
				int index = Integer.valueOf(msgv[0]);
				String userId = msgv[1];
				manager.addUser(index, userId);
				manager.notifyDataChanged();
			} else if (protocol == 7) { // 이모티콘 인덱스를 받는다
				ImageIcon icon = EmoticonManager.getInstance().getEmoticon(Integer.valueOf(msgv[1]));
				ta.setCaretPosition(ta.getDocument().getLength());
				ta.replaceSelection(msgv[0] + " : ");
				ta.setCaretPosition(ta.getDocument().getLength());
				ta.insertIcon(icon);
				ta.setCaretPosition(ta.getDocument().getLength());
				ta.replaceSelection("\n");
			}
		} // while문 끝
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == tf || e.getSource() == sendBtn) {
			RoomManager.getInstance().chat(tf.getText());
			tf.setText("");
		} else if (e.getSource() == emotBtn) {
			new EmotSelectDialog(MainFrame.getInstance(), ta);
		}

	}

	public JTextField getChatTextField() {
		return tf;
	}

}
