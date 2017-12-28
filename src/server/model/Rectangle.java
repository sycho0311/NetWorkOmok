package server.model;

public class Rectangle {
	private int x, y;
	private int row, col;

	public Rectangle(int x, int y, int row, int col) {
		this.x = x;
		this.y = y;
		this.row = row;
		this.col = col;
	}

	public boolean isContain(int x2, int y2) {
		int dx = x - x2;
		int dy = y - y2;

		if (Math.abs(dx) <= 16 && Math.abs(dy) <= 14.6)
			return true;
		return false;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getRow() {
		return row;
	}

	public int getCol() {
		return col;
	}

}