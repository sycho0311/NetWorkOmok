package client.view.game;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.StringTokenizer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import client.service.RoomManager;
import client.service.VoiceReceiver;
import client.service.VoiceSender;
import client.view.MainFrame;
import client.view.lobby.RoomListPanel;

public class GamePanel extends JFrame implements ActionListener, MouseListener {
	private static final long serialVersionUID = 4570829014286618373L;

	private JTextArea textArea = new JTextArea();
	private JTextField textField = new JTextField();

	private JButton UndoRequest = new JButton(new ImageIcon("img/UndoRequest.png"));
	private JButton sendBtn = new JButton(new ImageIcon("img/enter.png"));
	private JButton voiceChatBtn = new JButton(new ImageIcon("img/mute-microphone.png"));
	private JButton Exit = new JButton(new ImageIcon("img/Exit.png"));
	private JButton GameStart = new JButton(new ImageIcon("img/GameStart.png"));
	private MapPanel lbPanel;
	private UserNamePanel user1Panel;
	private UserNamePanel user2Panel;

	public static ImageIcon whiteStone = new ImageIcon("img/WhiteStone.png");
	public static ImageIcon blackStone = new ImageIcon("img/BlackStone.png");
	private ImageIcon emptyStone = new ImageIcon("");

	private String serverIP;
	private int port;
	private Socket socket;

	private BufferedReader in;
	private BufferedWriter out;
	private String UserID; // 자신의 아이디
	private boolean recv = true;

	private VoiceSender voiceSender;
	private VoiceReceiver voiceReceiver;
	private boolean voiceChatMode = false;

	public GamePanel(int roomId) {
		setTitle("GamePanel"); // 제목
		setSize(1280, 720); // 사이즈
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 종료 설정
		setLocationRelativeTo(null); // 가운데 배치
		setResizable(false); // 창 크기 고정

		// 마우스 커서
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Image image = toolkit.getImage("img/gameroom_cursor.png");
		Cursor cursor = toolkit.createCustomCursor(image, new Point(0, 0), "lobby");
		setCursor(cursor);

		JPanel contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout());
		setContentPane(contentPane);

		serverIP = RoomManager.getServerIP();
		port = RoomManager.getPort() + roomId;
		this.UserID = RoomManager.getInstance().getUserId(); // 사용자 ID 저장

		/* NORTH: 타이틀 삽입 */
		JLabel titleLbl = new JLabel();
		ImageIcon imagetitle = new ImageIcon("img/title.png");
		titleLbl = new JLabel(imagetitle);
		contentPane.add(titleLbl, BorderLayout.NORTH);

		/* EAST: 버튼, 사용자이름, 채팅로그 */
		JPanel eastPanel = new JPanel();
		eastPanel.setLayout(new BoxLayout(eastPanel, BoxLayout.Y_AXIS));

		// 버튼 패널
		JPanel btnPanel = new JPanel(new GridLayout(1, 3, 4, 0));
		Dimension btnSize = new Dimension(getWidth() / 6, 0);
		GameStart.setPreferredSize(btnSize);
		UndoRequest.setPreferredSize(btnSize);
		Exit.setPreferredSize(btnSize);
		GameStart.addActionListener(this);
		UndoRequest.addActionListener(this);
		Exit.addActionListener(this);
		btnPanel.add(GameStart);
		btnPanel.add(UndoRequest);
		btnPanel.add(Exit);
		eastPanel.add(btnPanel);

		// 사용자 ID, 상대방 ID 패널
		JPanel userNamePanel = new JPanel(new GridLayout(1, 2, 3, 0));

		// User1 이름판 삽입
		user1Panel = new UserNamePanel(getWidth() / 4, getHeight());
		user1Panel.setUserId(UserID);
		user1Panel.setStoneImage(new ImageIcon(""));
		userNamePanel.add(user1Panel);

		// User2 이름판 삽입
		user2Panel = new UserNamePanel(getWidth() / 4, getHeight());
		user2Panel.addMouseListener(this);
		user2Panel.setUserId("no user");
		user2Panel.setStoneImage(new ImageIcon(""));
		userNamePanel.add(user2Panel);

		eastPanel.add(userNamePanel);

		// 채팅창 삽입
		JPanel chatPanel = new JPanel();
		chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
		// 채팅 로그
		textArea.setRows(100);
		textArea.setDisabledTextColor(new Color(0, 0, 0));
		textArea.setEnabled(false);
		JScrollPane scrollPane = new JScrollPane(textArea);
		chatPanel.add(scrollPane);
		chatPanel.add(new JScrollPane(textArea), BorderLayout.CENTER);

		// 채팅 입력 필드
		Box inputBox = Box.createHorizontalBox();
		textField.addActionListener(this);
		sendBtn.addActionListener(this);
		sendBtn.setPreferredSize(new Dimension(40, 0));
		voiceChatBtn.addActionListener(this);
		voiceChatBtn.setPreferredSize(new Dimension(30, 0));
		inputBox.add(textField);
		inputBox.add(sendBtn);
		inputBox.add(voiceChatBtn);
		chatPanel.add(inputBox);

		eastPanel.add(chatPanel);

		contentPane.add(eastPanel, BorderLayout.EAST);

		/* CENTER: 바둑판 */
		lbPanel = new MapPanel(getWidth() / 2, getHeight() - titleLbl.getHeight());
		lbPanel.addMouseListener(this);
		contentPane.add(lbPanel, BorderLayout.CENTER);

		setVisible(true);

		setUp();
	}

	public void setUp() {
		try {
			socket = new Socket(serverIP, port);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // 소켓으로부터 읽어올수있는Reader생성
			out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

			endGame(); // 게임 초기화

			// 접속후 자신의 아이디 전송
			try {
				out.write(UserID + "\n");
				out.flush();
			} catch (IOException e1) {
			}

			Thread th = new Thread(new Runnable() { // 스레드를 돌려서 서버로부터 메세지를 수신

				@Override
				public void run() {
					while (true) {
						try {
							String text = in.readLine();
							System.out.println("[ClientRoom:LOG:" + UserID + "] " + text);
							if (text == null)
								continue;

							int tmp = checkProtocol(text);
							String[] msgv = text.split(",");

							if (tmp == 1) {
								textArea.append(msgv[1] + "\n");
								textArea.setCaretPosition(textArea.getText().length());
							}

							else if (tmp == 2) {
								// 무르기 1회 자신의 돌만
								int result = JOptionPane.showConfirmDialog(GamePanel.this, msgv[1], "무르기 요청",
										JOptionPane.OK_CANCEL_OPTION);
								// 무르기 요청 수락
								if (result == 0) {
									out.write("21," + UserID + "무르기 수락" + "\n");
									out.flush();
									undoStone();
								} else {
									out.write("20," + UserID + "무르기 거절" + "\n");
									out.flush();
								}
							}

							else if (tmp == 20) {
								JOptionPane.showMessageDialog(GamePanel.this, msgv[1], "무르기 요청 거절",
										JOptionPane.WARNING_MESSAGE);
							}

							else if (tmp == 21) {
								// 무르기 1회 자신의 돌만
								JOptionPane.showMessageDialog(GamePanel.this, msgv[1], "무르기 요청 수락",
										JOptionPane.WARNING_MESSAGE);
								undoStone();
							}

							else if (tmp == 22) {
								// 무르기 2회 상대방의 돌까지
								int result = JOptionPane.showConfirmDialog(GamePanel.this, msgv[1], "무르기 요청",
										JOptionPane.OK_CANCEL_OPTION);
								// 무르기 요청 수락
								if (result == 0) {
									out.write("23," + UserID + "무르기 수락" + "\n");
									out.flush();
									undoStone();
									undoStone();
								} else {
									out.write("20," + UserID + "무르기 거절" + "\n");
									out.flush();
								}
							}

							else if (tmp == 23) {
								JOptionPane.showMessageDialog(GamePanel.this, msgv[1], "무르기 요청 수락",
										JOptionPane.WARNING_MESSAGE);
								undoStone();
								undoStone();
							}

							else if (tmp == 3) {
								// 게임 시작 제안 받음
								int result = JOptionPane.showConfirmDialog(GamePanel.this, msgv[1], "게임 시작 제안",
										JOptionPane.OK_CANCEL_OPTION);
								System.out.println(result);

								// 게임 시작 제안 수락
								if (result == 0) {
									out.write("8," + UserID + "게임 제안 수락" + "\n");
									out.flush();
									endGame();
									user1Panel.setStoneImage(whiteStone);
									user2Panel.setStoneImage(blackStone);
									repaint();
								}
								// 게임 제안 거절
								else {
									out.write("7," + UserID + "게임 제안 거절" + "\n");
									out.flush();
								}
							}

							// 상대방의 아이디를 받아옴
							else if (tmp == 6) {
								recvID(text);
							}

							else if (tmp == 0) {
								removeID(text);
							}

							else if (tmp == 7) {
								// 게임 제안 거절 메세지를 받는 경우
								JOptionPane.showMessageDialog(GamePanel.this, msgv[1], "게임 제안 거절",
										JOptionPane.WARNING_MESSAGE);
							}

							else if (tmp == 8) {
								// 게임 제안 수락 메세지를 받는 경우
								JOptionPane.showMessageDialog(GamePanel.this, msgv[1], "게임 제안 수락",
										JOptionPane.WARNING_MESSAGE);
								endGame();
								user1Panel.setStoneImage(blackStone);
								user2Panel.setStoneImage(whiteStone);
								repaint();
							}

							// 돌을 놓은 좌표를 받아와서 착수, 돌을 놓음
							else if (tmp == 9) {
								lbPanel.inputStone(text);
							}

							else if (tmp == 91) { // 돌을 두면 안되는 위치에 두었을 경우
								JOptionPane.showMessageDialog(GamePanel.this, msgv[1], "알림",
										JOptionPane.WARNING_MESSAGE);
							}

							else if (tmp == 92) { // 승패 판정났을 경우
								JOptionPane.showMessageDialog(GamePanel.this, msgv[1], "알림",
										JOptionPane.WARNING_MESSAGE);
								user1Panel.setStoneImage(emptyStone);
								user2Panel.setStoneImage(emptyStone);
							}

							else if (tmp == 13) {
								if (voiceChatMode) // 이미 음성채팅 모드라면 요청을 무시한다
									continue;

								// 음성채팅 요청
								int result = JOptionPane.showConfirmDialog(GamePanel.this, msgv[1], "음성채팅 요청",
										JOptionPane.OK_CANCEL_OPTION);
								// 음성채팅 수락
								if (result == 0) {
									out.write("14,\n");
									out.flush();
									startVoiceChat(msgv[2], port, port + RoomListPanel.MAX_NUM);
								}
							}

							else if (tmp == 14) { // 상대가 음성채팅을 수락한 경우
								startVoiceChat(msgv[1], port + RoomListPanel.MAX_NUM, port);
							}
						} catch (IOException e) {
							textArea.append("메세지 수신 에러!!\n");
							// 서버와 소켓 통신에 문제가 생겼을 경우 소켓을 닫는다
							try {
								socket.close();
								endGame();
								break; // 에러 발생하면 while문 종료
							} catch (IOException e1) {
							}
						}
					} // while문 끝
				} // run메소드 끝

			});
			th.start();

		} catch (IOException e) {
		}
	}

	public int checkProtocol(String str) {
		StringTokenizer token = new StringTokenizer(str);
		String num = token.nextToken(",");

		return Integer.parseInt(num);
	}

	public void endGame() {
		lbPanel.clearStone();
		user1Panel.setStoneImage(emptyStone);
		user2Panel.setStoneImage(emptyStone);
		repaint();
	}

	// 아이디를 받아온다.
	public void recvID(String str) {
		StringTokenizer token = new StringTokenizer(str);
		token.nextToken(",");

		// 받아온 id를 userid를 세팅한다
		String user1 = token.nextToken(",");
		String user2 = token.nextToken(",");

		String user = (UserID.equals(user1)) ? user2 : user1;
		textArea.append(user + " 님이 입장하셨습니다\n");
		textArea.setCaretPosition(textArea.getText().length());

		user2Panel.setUserId(user);

		repaint();
	}

	// 퇴장한 상대의 id를 제거한다
	public void removeID(String str) {
		StringTokenizer token = new StringTokenizer(str);
		token.nextToken(",");

		// 받아온 id를 userid를 세팅한다
		String user1 = token.nextToken(",");
		String user2 = token.nextToken(",");

		String user = (UserID.equals(user1)) ? user2 : user1;
		textArea.append(user + " 님이 퇴장하셨습니다\n");
		textArea.setCaretPosition(textArea.getText().length());

		user2Panel.setUserId("no user");

		repaint();

		stopVoiceChat();
	}

	// 가장 마지막 인덱스 lb.remove 가장 최근에 놓았던 돌을 무르기
	public void undoStone() {
		lbPanel.undoStone();
		repaint();
	}

	private void startVoiceChat(String ip, int inPort, int outPort) {
		voiceChatMode = true;
		voiceChatBtn.setIcon(new ImageIcon("img/microphone.png"));
		voiceSender = new VoiceSender(ip, outPort);
		voiceReceiver = new VoiceReceiver(inPort);
		new Thread(voiceSender).start();
		new Thread(voiceReceiver).start();
	}

	private void stopVoiceChat() {
		voiceChatMode = false;
		voiceChatBtn.setIcon(new ImageIcon("img/mute-microphone.png"));
		if (voiceSender != null)
			voiceSender.stop();
		if (voiceReceiver != null)
			voiceReceiver.stop();
	}

	public void actionPerformed(ActionEvent e) {
		// 채팅 전송
		if (e.getSource() == textField || e.getSource() == sendBtn) {
			try {
				out.write("1," + UserID + " : " + textField.getText() + "\n");
				out.flush();
				textField.setText("");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} else if (e.getSource() == voiceChatBtn) { // 음성채팅
			if (!voiceChatMode) { // 활성화
				try {
					out.write("13," + UserID + " 님이 음성채팅을 요청하셨습니다\n");
					out.flush();
					textField.setText("");
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			} else { // 비활성화
				stopVoiceChat();
			}
		}
		// 게임 방 나가기
		else if (e.getSource() == Exit) {
			try {
				socket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			stopVoiceChat();
			MainFrame.getInstance().setVisible(true);
			dispose();
			RoomManager.getInstance().request(RoomListPanel.getPage());
		}
		// 무르기
		else if (e.getSource() == UndoRequest) {
			try {
				out.write("2," + UserID + "가 무르기 요청을 시도" + "\n");
				out.flush();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		// 게임 시작
		else if (e.getSource() == GameStart) {
			try {
				out.write("3," + UserID + "가 게임 시작을 제안" + "\n");
				out.flush();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	// 바둑판 좌표 클릭시 좌표 전송
	public void mouseClicked(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();

		// 자신이 놓은 좌표를 전송
		try {
			out.write("9," + x + "," + y + "\n");
			out.flush();
		} catch (IOException e1) {
		}
	}

	// 사용자 아이디를 클릭시 수신거부 설정
	public void mousePressed(MouseEvent e) {
		if (recv == true) {
			if (e.getSource() == user2Panel) {
				int result = JOptionPane.showConfirmDialog(GamePanel.this, "대화를 차단하시겠습니까?", "수신 거부",
						JOptionPane.OK_CANCEL_OPTION);
				System.out.println(result);

				// 수신 거부 설정
				if (result == 0) {
					try {
						out.write("11," + UserID + "수신 거부 설정" + "\n");
						out.flush();
					} catch (IOException e1) {
					}
				}
				recv = false;
			}
		} else {
			if (e.getSource() == user2Panel) {
				int result = JOptionPane.showConfirmDialog(GamePanel.this, "대화 차단을 해제하시겠습니까?", "수신 거부 해제",
						JOptionPane.OK_CANCEL_OPTION);
				System.out.println(result);

				// 수신 거부 설정
				if (result == 0) {
					try {
						out.write("12," + UserID + "수신 거부 해제" + "\n");
						out.flush();
					} catch (IOException e1) {
					}
				}
				recv = true;
			}
		}
	}

	public void mouseReleased(MouseEvent e) {

	}

	public void mouseEntered(MouseEvent e) {

	}

	public void mouseExited(MouseEvent e) {

	}

}
