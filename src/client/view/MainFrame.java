package client.view;

import java.awt.Cursor;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;

import javax.swing.JFrame;
import client.view.game.GamePanel;
import client.view.lobby.LobbyPanel;

public class MainFrame extends JFrame {
	private static final long serialVersionUID = 5076434233577914288L;
	private static MainFrame frame;

	private MainFrame(String title) {
		frame = this;
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle(title);
		setSize(1280, 720);
		setLocationRelativeTo(null); // 가운데 배치
		setResizable(false);

		// 커서 숨기기
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Image image = toolkit.getImage("empty");
		Cursor cursor = toolkit.createCustomCursor(image, new Point(0, 0), "empty");
		setCursor(cursor);

		setVisible(true);
	}

	public synchronized static MainFrame getInstance() {
		return frame;
	}

	public void gotoStart() {
		StartPanel startPanel = new StartPanel();
		setContentPane(startPanel);
		startPanel.requestFocusInWindow();
		revalidate();
	}

	public void gotoLogin() {
		// 마우스 커서
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Image image = toolkit.getImage("img/lobby_cursor.png");
		Cursor cursor = toolkit.createCustomCursor(image, new Point(0, 0), "lobby");
		setCursor(cursor);

		LoginPanel loginPanel = new LoginPanel();
		setContentPane(loginPanel);
		loginPanel.requestTextFieldFocus();
		revalidate();
	}

	public void gotoLobby() {
		LobbyPanel lobbyPanel = new LobbyPanel();
		setContentPane(lobbyPanel);
		lobbyPanel.requestFocusInWindow();
		revalidate();
	}

	public void gotoGameRoom(int id) {
		setVisible(false);
		new GamePanel(id);
	}

	public static void main(String args[]) {
		new MainFrame("한성 오목").gotoStart();
	}

}