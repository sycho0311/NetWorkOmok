package client.view;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import client.service.RoomManager;

public class LoginPanel extends JPanel implements ActionListener, KeyListener {
	private static final long serialVersionUID = 2607264486146393893L;

	private JTextField idTf = new JTextField(15);
	private JButton loginBtn = new JButton("로그인");
	private JButton exitBtn = new JButton("종료");

	public LoginPanel() {
		int width = MainFrame.getInstance().getWidth();
		int height = MainFrame.getInstance().getHeight();

		/* 이벤트 리스너 추가 */
		idTf.addActionListener(this);
		idTf.addKeyListener(this);
		loginBtn.addActionListener(this);
		exitBtn.addActionListener(this);

		JPanel emptyPanel = new JPanel(); // 로그인 패널을 아래로 옮기기 위한 투명한 빈 패널
		emptyPanel.setPreferredSize(new Dimension(width, height * 2 / 3));
		emptyPanel.setOpaque(false);
		JPanel loginPanel = new JPanel();

		// ID 입력 공간
		loginPanel.add(idTf);

		// 로그인버튼
		Dimension size = new Dimension(80, 22);
		loginBtn.setPreferredSize(size);
		exitBtn.setPreferredSize(size);
		loginBtn.setEnabled(false);
		loginPanel.add(loginBtn);
		loginPanel.add(exitBtn);

		add(emptyPanel);
		add(loginPanel);
	}
	
	public void requestTextFieldFocus() {
		idTf.requestFocusInWindow();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == idTf || e.getSource() == loginBtn) {
			String userId = idTf.getText().trim();
			RoomManager.getInstance().setUserId(userId);
			MainFrame.getInstance().gotoLobby();
		} else if (e.getSource() == exitBtn) {
			System.exit(0);
		}
	}

	@Override
	public void paint(Graphics g) {
		/* 로그인화면 배경을 그린다 */
		ImageIcon bg = new ImageIcon("img/Pane.jpg");
		int pwidth = MainFrame.getInstance().getWidth();
		int pheight = MainFrame.getInstance().getHeight();

		// width resize
		int resizedWidth = (int) pheight;
		int x = (pwidth - resizedWidth) / 2;

		// width 늘어난 배율
		float m = (float) resizedWidth / bg.getIconWidth();

		// height resize
		int resizedHeight = (int) (pheight * m);
		int y = (pheight - resizedHeight) / 2;

		g.drawImage(bg.getImage(), x - resizedWidth, y, resizedWidth, resizedHeight, null);
		g.drawImage(bg.getImage(), x + resizedWidth, y, resizedWidth, resizedHeight, null);
		g.drawImage(bg.getImage(), x, y, resizedWidth, resizedHeight, null);

		ImageIcon title = new ImageIcon("img/start_title.png");
		x = (pwidth - title.getIconWidth()) / 2;
		y = (pheight - title.getIconHeight()) / 5;
		g.drawImage(title.getImage(), x, y, title.getIconWidth(), title.getIconHeight(), null);
		setOpaque(false);
		super.paint(g);
	}

	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getSource() == idTf) {
			if (idTf.getText().trim().equals(""))
				loginBtn.setEnabled(false);
			else
				loginBtn.setEnabled(true);
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}
}
