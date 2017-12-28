package client.view.lobby;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import client.service.RoomManager;
import client.view.MainFrame;

public class CreatRoomDialog extends JDialog implements ActionListener, KeyListener {
	private static final long serialVersionUID = 7721284482007023122L;
	private JTextField nameTf = new JTextField(15);
	private JButton creatBtn = new JButton("만들기");
	private JButton cancelBtn = new JButton("취소");

	public CreatRoomDialog() {
		super(MainFrame.getInstance());
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null); // 가운데 배치
		setTitle("방 만들기");
		setAlwaysOnTop(true);
		setModal(true);

		creatBtn.addActionListener(this);
		cancelBtn.addActionListener(this);
		nameTf.addActionListener(this);
		nameTf.addKeyListener(this);

		Box inputBox = Box.createHorizontalBox();
		inputBox.add(new JLabel("방 제목"));
		inputBox.add(nameTf);
		add(inputBox, BorderLayout.CENTER);
		JPanel btnPanel = new JPanel();
		btnPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		creatBtn.setEnabled(false);
		btnPanel.add(creatBtn);
		btnPanel.add(cancelBtn);
		add(btnPanel, BorderLayout.SOUTH);

		pack();
		setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == nameTf || e.getSource() == creatBtn) {
			RoomManager.getInstance().creatRoom(nameTf.getText().trim());
			dispose();
		} else if (e.getSource() == cancelBtn) {
			dispose();
		}
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getSource() == nameTf) {
			if (nameTf.getText().trim().equals(""))
				creatBtn.setEnabled(false);
			else
				creatBtn.setEnabled(true);
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}
}
