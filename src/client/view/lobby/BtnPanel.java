package client.view.lobby;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import client.service.RoomManager;
import client.view.MainFrame;

public class BtnPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = -8523217645732633290L;

	private JButton creatRoomBtn = new JButton("방만들기");
	private JButton logoutBtn = new JButton("로그아웃");
	private JButton exitBtn = new JButton("종료");

	public BtnPanel(int width, int height) {
		// =FlowLayout

		// Listener
		creatRoomBtn.addActionListener(this);
		logoutBtn.addActionListener(this);
		exitBtn.addActionListener(this);

		// Size
		Dimension size = new Dimension(width / 8, height);
		creatRoomBtn.setPreferredSize(size);
		logoutBtn.setPreferredSize(size);
		exitBtn.setPreferredSize(size);

		add(creatRoomBtn);
		add(logoutBtn);
		add(exitBtn);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == creatRoomBtn) {
			new CreatRoomDialog();
		} else if (e.getSource() == logoutBtn) {
			MainFrame.getInstance().gotoLogin();
			RoomManager.getInstance().destroy();
		} else if (e.getSource() == exitBtn) {
			System.exit(0);
		}
	}

}
