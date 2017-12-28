package server.service;

import server.model.Map;

public class OmokJudgement {
	/* 5목 방향 상수 */
	// UP/RIGHT, RIGHT, DOWN/RIGHT, DOWN방향으로만 5목을 검사한다
	// dx[0], dy[0] UP/RIGHT // dx[1], dy[1] RIGHT
	// dx[2], dy[2] DOWN/RIGHT // dx[3], dy[3] DOWN
	private final int[] dx = { -1, 0, 1, 1 };
	private final int[] dy = { 1, 1, 1, 0 };

	/* 바둑판, 현재 놓여진 돌 정보 */
	private Map map;

	/* 승리한 돌 정보 */
	private int winner; // 이긴 돌의 색 (-1:이미돌이 있음, -2:금수, 0:승부안남, 1:검은 돌 승리, 2:흰 돌 승리)
	private int wx; // 이긴 돌의 x (행번호)
	private int wy; // 이긴 돌의 y (열번호)
	private int dir; // 이긴 돌의 5목 방향

	/* 생성자에서 judge를 호출한다 */
	public OmokJudgement(Map map, int x, int y, int color) { // map: 맵, xy: 가장 최근에 둔 돌의 위치, color: 가장 최근에 둔 돌
		this.map = map;

		// 금수 검사
		// 이미 돌이 있는 곳에 둘 수 없다
		if (map.getColor(x, y) != 0) {
			winner = -1;
			return;
		}
		map.setColor(x, y, color);

		// 33검사 || 44검사
		if (check33() || check44()) {
			winner = -2;
			map.setColor(x, y, 0);
			return;
		}

		// 판정: map의 모든 돌을 검사한다
		for (int i = 0; i < Map.SIZE; i++)
			for (int j = 0; j < Map.SIZE; j++)
				for (int k = 0; k < 4; k++) {
					winner = judge(i, j, k);
					if (winner != 0) {
						wx = i;
						wy = j;
						dir = k;
						return;
					}
				}
		winner = 0;
	}

	/* 현재 Map을 보고 승패를 판정한다 */
	// xy: 현재 돌의 위치
	// d : 5목을 검사할 '방향상수'의 인덱스
	private int judge(int x, int y, int d) {
		// 검사할 돌의 색
		int color = map.getColor(x, y);

		// 현재 위치에 돌이 없으면 0 반환
		if (color == Map.EMPTY)
			return 0;

		// 돌이 Map의 가장자리에 위치하여 5목을 검사할 필요가 없으면 0 반환
		if (x + dx[d] * 4 < 0 || y + dy[d] * 4 < 0)
			return 0;
		if (x + dx[d] * 4 > Map.SIZE || y + dy[d] * 4 > Map.SIZE)
			return 0;

		// count를 증가시키면서 5목 검사
		int count = 1;
		while (map.getColor(x + dx[d] * count, y + dy[d] * count) == color)
			count++;

		// 결과반환
		if (count == 5) // d 방향으로 5목이면
			if (map.getColor(x - dx[d], y - dy[d]) != color) // d 반대방향 1개의 돌을
				// 검사하여 6목이상인지
				// 검사한다
				return color; // color 승리

		return 0; // 승부 안남
	}

	/* 33 */
	// 열린 3이 두개 생기는 경우
	private boolean check33() {
		/* T형 */
		/* r형 */
		/* ㅅ형 */
		/* ㅅ2형 */
		/* +형 */

		return false;
	}

	/* 44 */
	// 한쪽이 막혀도 44면 금수... 반대로 모두 막혀있으면 금수가 아니다
	private boolean check44() {
		/* 1형 */
		/* 222형 */
		/* 131형 */
		/* ㄱ형 */
		/* +형 */

		return false;
	}

	/* 승리한 돌 정보 */
	public int getWinner() {
		return winner;
	}

	public int getWx() {
		return wx;
	}

	public int getWy() {
		return wy;
	}

	public int getDir() {
		return dir;
	}

}