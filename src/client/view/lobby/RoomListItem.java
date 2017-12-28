package client.view.lobby;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import client.model.Room;
import client.service.RoomManager;

public class RoomListItem extends JPanel implements MouseListener {
	private static final long serialVersionUID = 8528868958390798696L;

	private Room room;
	private static Color color = new Color(0, 57, 132);
	private static ImageIcon bg = new ImageIcon("img/room.png");
	private static Font idFont = new Font(Font.SERIF, Font.BOLD, 40);
	private static Font nameFont = new Font(Font.SERIF, Font.BOLD, 15);
	private static Font numFont = new Font(Font.SERIF, Font.BOLD, 25);

	public RoomListItem() {
		setOpaque(false);

		/* MouseListener 등록 */
		addMouseListener(this);

		/* 레이블 삽입 */
		drawRoom(null);
	}

	public void drawRoom(Room room) {
		this.room = room;
		repaint();
	}

	@Override
	public void paintComponent(Graphics g) {
		g.drawImage(bg.getImage(), 0, 0, getWidth(), getHeight(), null);
		if (room != null) {

			g.setColor(Color.WHITE);
			g.setFont(idFont);
			g.drawString(String.valueOf(room.getId()), 50, 85);

			g.setColor(color);

			g.setFont(nameFont);
			g.drawString(room.getName(), 130, 52);

			g.setFont(numFont);
			g.drawString(String.valueOf(room.getNum() + " / " + Room.MAX_NUM), 250, 120);
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (room == null)
			return;
		RoomManager.getInstance().enterRoom(room.getId());
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

}
