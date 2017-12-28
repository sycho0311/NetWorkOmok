package server.model;

public class Map {
	public final static int SIZE = 19;
	public final static int EMPTY = 0;
	public final static int BLACK = 1;
	public final static int WHITE = 2;

	private int[][] map = new int[SIZE + 6][SIZE + 6];

	public Map() {
		for (int i = 0; i < SIZE + 6; i++)
			for (int j = 0; j < SIZE + 6; j++)
				map[i][j] = EMPTY;
	}

	public int getColor(int x, int y) {
		return map[x][y];
	}

	public void setColor(int x, int y, int color) {
		map[x][y] = color;
	}

}
