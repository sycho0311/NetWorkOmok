package client.view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JPanel;

public class StartPanel extends JPanel implements Runnable, KeyListener {
	private static final long serialVersionUID = 801287905024196768L;

	private XLiner xliner = new XLiner(this);
	private YLiner yliner = new YLiner(this);
	private Thread animation = new Thread(this);

	public StartPanel() {
		MainFrame.getInstance().setBackground(Color.BLACK);

		// animation skip listener
		addKeyListener(this);

		animation.start();
	}

	@Override
	public void run() {
		int width = MainFrame.getInstance().getWidth();
		int height = MainFrame.getInstance().getHeight();

		try {
			// line animation
			xliner.stretch((int) (width * 1.6));
			yliner.stretch((int) (height * 2.8));

			// 배경이 점점 밝아지는 animation
			for (int i = 0; i < 255; i++) {
				if (i == 210) {
					xliner.clear();
					yliner.clear();
				}
				MainFrame.getInstance().setBackground(new Color(i, i, i));
				repaint();
				Thread.sleep(8);
			}

			MainFrame.getInstance().gotoLogin();
		} catch (InterruptedException e) {
			return;
		}
	}

	@Override
	public void paint(Graphics g) {

		g.setColor(Color.ORANGE);
		for (int i = 0; i < xliner.getSize(); i++) {
			int x = xliner.getX(i);
			int y = xliner.getY(i);
			g.drawLine(0, y, x, y);
		}
		for (int i = 0; i < yliner.getSize(); i++) {
			int x = yliner.getX(i);
			int y = yliner.getY(i);
			g.drawLine(x, 0, x, y);
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		animation.interrupt();
		MainFrame.getInstance().gotoLogin();
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

}
