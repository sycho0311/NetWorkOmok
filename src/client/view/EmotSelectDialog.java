package client.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import client.service.EmoticonManager;
import client.service.RoomManager;

public class EmotSelectDialog extends JDialog implements ActionListener {
	private static final long serialVersionUID = -4720107538374703908L;

	private Vector<JButton> emotBtns = new Vector<>();
	private JButton cancelBtn = new JButton("취소");

	public EmotSelectDialog(JFrame frame, Component location) {
		super(frame);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(location);
		setTitle("이모티콘 선택");
		setAlwaysOnTop(true);
		setModal(true);

		initEmotBtn();

		JPanel emotPanel = new JPanel(new GridLayout(2, 3, 2, 2));
		for (JButton emotBtn : emotBtns) {
			emotPanel.add(emotBtn);
			emotBtn.addActionListener(this);
		}
		add(emotPanel, BorderLayout.CENTER);

		cancelBtn.addActionListener(this);
		add(cancelBtn, BorderLayout.SOUTH);

		pack();
		setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		for (int i = 0; i < emotBtns.size(); i++) {
			if (e.getSource() == emotBtns.get(i)) {
				RoomManager.getInstance().sendEmot(i);
				dispose();
			}
		}
		if (e.getSource() == cancelBtn)
			dispose();
	}

	private void initEmotBtn() {
		EmoticonManager manager = EmoticonManager.getInstance();
		int size = manager.getSize();
		for (int i = 0; i < size; i++)
			emotBtns.add(new JButton(manager.getEmoticon(i)));
	}
}
