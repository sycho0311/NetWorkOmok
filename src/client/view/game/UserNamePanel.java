package client.view.game;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class UserNamePanel extends JPanel {
	private static final long serialVersionUID = -8947349938498208651L;

	private ImageIcon bg = new ImageIcon("img/Blank.png");
	private Font font = new Font(Font.SERIF, Font.HANGING_BASELINE, 30);
	private String userId;
	private ImageIcon stoneImage;

	public UserNamePanel(int width, int height) {
		setLayout(new FlowLayout(FlowLayout.CENTER));
		setSize(width, height);
		setPreferredSize(new Dimension(width, height));
	}

	public void setUserId(String userId) {
		this.userId = userId;
		repaint();
	}

	public void setStoneImage(ImageIcon stoneImage) {
		this.stoneImage = stoneImage;
		repaint();
	}

	@Override
	public void paintComponent(Graphics g) {
		g.drawImage(bg.getImage(), 0, 0, bg.getIconWidth(), bg.getIconHeight(), null);
		setOpaque(false);

		g.drawImage(stoneImage.getImage(), 10, getHeight() / 2 - 14, 25, 25, null);

		g.setFont(font);
		g.drawString(userId + " 님", 50, getHeight() / 2);
	}
}
