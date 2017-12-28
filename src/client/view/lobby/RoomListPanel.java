package client.view.lobby;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import client.model.Room;
import client.service.RoomManager;
import client.view.Observer;

public class RoomListPanel extends JPanel implements ActionListener, Observer {
	private static final long serialVersionUID = 1895452061690084656L;

	private static final int ROW = 2;
	private static final int COL = 3;
	public static final int NUM = ROW * COL;
	private static final int MAX_PAGE = 3;
	public static final int MAX_NUM = NUM * MAX_PAGE;
	private static int page;

	private RoomListItem[] roomPanels = new RoomListItem[NUM];
	private JButton leftPageBtn = new JButton("<<");
	private JLabel pageLbl = new JLabel("1");
	private JButton rightPageBtn = new JButton(">>");

	public RoomListPanel(int width, int height) {
		setPreferredSize(new Dimension(width, height));
		setLayout(new BorderLayout());

		/* ActionListener 등록 */
		leftPageBtn.addActionListener(this);
		rightPageBtn.addActionListener(this);

		/* CENTER : 방 목록 */
		JPanel centerPanel = new JPanel(new GridLayout(ROW, COL, 1, 1));
		centerPanel.setBackground(Color.WHITE);
		for (int i = 0; i < roomPanels.length; i++) {
			roomPanels[i] = new RoomListItem();
			centerPanel.add(roomPanels[i]);
		}
		add(centerPanel, BorderLayout.CENTER);

		/* SOUTH : 페이지 */
		JPanel southPanel = new JPanel();
		southPanel.add(leftPageBtn);
		southPanel.add(pageLbl);
		southPanel.add(rightPageBtn);
		add(southPanel, BorderLayout.SOUTH);

		RoomManager.getInstance().allow(this);
	}

	@Override
	public void update() {
		/* 방 목록을 다시 그린다 */
		for (RoomListItem item : roomPanels)
			item.drawRoom(null);

		int drawIndex = 0;
		RoomManager manager = RoomManager.getInstance();
		for (int index = 0; index < NUM; index++) {
			Room room = manager.getRoom(index);
			if (room != null)
				roomPanels[drawIndex++].drawRoom(room);
		}
		pageLbl.setText(String.valueOf(page + 1));
		revalidate();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == leftPageBtn) {
			if (page <= 0)
				return;
			RoomManager.getInstance().request(--page);
		} else if (e.getSource() == rightPageBtn) {
			if (page >= MAX_PAGE - 1)
				return;
			RoomManager.getInstance().request(++page);
		}
	}

	public static int getPage() {
		return page;
	}
	
}
