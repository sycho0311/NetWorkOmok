package server.service;

import java.util.Vector;

import server.model.Rectangle;

public class StoneManager {
	private static final int ROW = 19;
	private static final int COL = 19;

	private Vector<Rectangle> recs = new Vector<>();

	public StoneManager() {
		for (int i = 0; i < ROW; i++)
			for (int j = 0; j < COL; j++)
				recs.add(new Rectangle(23 + (int) (i * 32), 19 + (int) (j * 29.2), i, j));
	}

	public Rectangle getRectangleAt(int x, int y) {
		for (Rectangle rec : recs)
			if (rec.isContain(x, y))
				return rec;
		return null;
	}
}