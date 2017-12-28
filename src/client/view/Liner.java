package client.view;

import java.awt.Component;
import java.util.Vector;

public abstract class Liner {
	protected Vector<Point> points = new Vector<>();
	private Component comp;

	public Liner(Component comp) {
		this.comp = comp;
	}

	public final void stretch(int length) throws InterruptedException {

		for (int i = 0; i < length; i++) {
			if (i % 45 == 0)
				createPoint(i);

			for (Point p : points)
				stretchPoint(p);

			comp.repaint();
			Thread.sleep(1);
		}
	}

	protected abstract void createPoint(int pos);

	protected abstract void stretchPoint(Point p);

	public int getX(int index) {
		return points.get(index).x;
	}

	public int getY(int index) {
		return points.get(index).y;
	}

	public int getSize() {
		return points.size();
	}

	public void clear() {
		points.clear();
	}

	protected class Point {
		int x, y;

		Point(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}

}

class XLiner extends Liner {

	public XLiner(Component comp) {
		super(comp);
	}

	@Override
	protected void createPoint(int pos) {
		points.add(new Point(0, pos));
	}

	@Override
	protected void stretchPoint(Point p) {
		p.x += 1;
	}

}

class YLiner extends Liner {

	public YLiner(Component comp) {
		super(comp);
	}

	@Override
	protected void createPoint(int pos) {
		points.add(new Point(pos, 0));
	}

	@Override
	protected void stretchPoint(Point p) {
		p.y += 1;
	}

}
