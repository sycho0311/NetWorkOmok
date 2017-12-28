package client.service;

import java.awt.Component;
import java.util.Vector;

public class StoneManager {
	protected Vector<Point> points = new Vector<>();
	private Component comp;

	public StoneManager(Component comp) {
		this.points = new Vector<>();
		this.comp = comp;
	}

	public int getX(int index) {
		return points.get(index).x;
	}

	public int getY(int index) {
		return points.get(index).y;
	}

	public boolean getColor(int index) {
		return points.get(index).isBlack;
	}

	public int getSize() {
		return points.size();
	}

	public void inputStone(int x, int y, boolean color) {
		points.add(new Point(x, y, color));
		comp.repaint();
	}

	public void undoStone() {
		points.remove(points.size() - 1);
		comp.repaint();
	}

	public void clearStone() {
		points.clear();
		comp.repaint();
	}

	protected class Point {
		int x, y;
		boolean isBlack;

		public Point(int x, int y, boolean isBlack) {
			this.x = x;
			this.y = y;
			this.isBlack = isBlack;
		}
	}

}
