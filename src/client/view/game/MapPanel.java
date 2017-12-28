package client.view.game;

import java.awt.Graphics;
import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import client.service.StoneManager;

public class MapPanel extends JPanel implements Runnable {
	private static final long serialVersionUID = 228871028090858579L;

	private StoneManager manager = new StoneManager(this);

	public MapPanel(int width, int height) {
		setSize(width, height);
	}

	@Override
	public void paintComponent(Graphics g) {
		g.drawImage(new ImageIcon("img/Pane.jpg").getImage(), 0, 0, getWidth(), getHeight(), null);
		setOpaque(false);

		int size = manager.getSize();
		for (int i = 0; i < size; i++) {
			int x = manager.getX(i);
			int y = manager.getY(i);
			boolean color = manager.getColor(i);
			ImageIcon imageIcon = (color) ? GamePanel.blackStone : GamePanel.whiteStone;
			g.drawImage(imageIcon.getImage(), x - 10, y - 10, 25, 25, null);
		}
	}

	// 바둑돌을 그린다
	public void inputStone(String str) {
		StringTokenizer token = new StringTokenizer(str);
		token.nextToken(",");

		// 받아온 x, y 좌표를 통해 돌을 놓는다
		int x = Integer.parseInt(token.nextToken(","));
		int y = Integer.parseInt(token.nextToken(","));
		boolean color = Boolean.parseBoolean(token.nextToken(","));

		manager.inputStone(x, y, color);

		new Thread(this).start(); // 돌 놓는 소리 재생
	}

	public void undoStone() {
		manager.undoStone();
	}

	public void clearStone() {
		manager.clearStone();
	}

	@Override
	public void run() {
		try {
			Clip clip = AudioSystem.getClip();
			File file = new File("sound/stone.wav");
			AudioInputStream ais = AudioSystem.getAudioInputStream(file);
			clip.open(ais);
			clip.start();
		} catch (LineUnavailableException | UnsupportedAudioFileException | IOException e) {
			e.printStackTrace();
		}
	}
}
