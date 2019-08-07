package myamya.other.solver.scrin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Solver;

public class ScrinSolver implements Solver {

	public static class Field {
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		// マスの情報
		private Masu[][] masu;
		// 数字の情報
		private final Integer[][] numbers;
		// 確定した部屋の位置情報。再調査しないことでスピードアップ
		private Set<Position> fixedPosSet;

		public Masu[][] getMasu() {
			return masu;
		}

		public int getYLength() {
			return masu.length;
		}

		public int getXLength() {
			return masu[0].length;
		}

		public Field(int height, int width, String param) {
			masu = new Masu[height][width];
			numbers = new Integer[height][width];
			fixedPosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
			int index = 0;
			for (int i = 0; i < param.length(); i++) {
				char ch = param.charAt(i);
				int interval = ALPHABET_FROM_G.indexOf(ch);
				if (interval != -1) {
					index = index + interval + 1;
				} else {
					//16 - 255は '-'
					//256 - 999は '+'
					int capacity;
					if (ch == '.') {
						Position pos = new Position(index / getXLength(), index % getXLength());
						numbers[pos.getyIndex()][pos.getxIndex()] = -1;
						masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
					} else {
						if (ch == '-') {
							capacity = Integer.parseInt("" + param.charAt(i + 1) + param.charAt(i + 2), 16);
							i++;
							i++;
						} else if (ch == '+') {
							capacity = Integer.parseInt(
									"" + param.charAt(i + 1) + param.charAt(i + 2) + param.charAt(i + 3),
									16);
							i++;
							i++;
							i++;
						} else {
							capacity = Integer.parseInt(String.valueOf(ch), 16);
						}
						Position pos = new Position(index / getXLength(), index % getXLength());
						masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
						numbers[pos.getyIndex()][pos.getxIndex()] = capacity;
					}
					index++;
				}
			}
		}

		public Field(Field other) {
			numbers = other.numbers;
			masu = new Masu[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
				}
			}
			fixedPosSet = new HashSet<>(other.fixedPosSet);
		}

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						if (numbers[yIndex][xIndex] > 99) {
							sb.append("99");
						} else if (numbers[yIndex][xIndex] == -1) {
							sb.append("？");
						} else {
							String capacityStr = String.valueOf(numbers[yIndex][xIndex]);
							int index = HALF_NUMS.indexOf(capacityStr);
							if (index >= 0) {
								sb.append(FULL_NUMS.substring(index / 2, index / 2 + 1));
							} else {
								sb.append(capacityStr);
							}
						}
					} else {
						sb.append(masu[yIndex][xIndex]);
					}
				}
				sb.append(System.lineSeparator());
			}
			return sb.toString();
		}

		public String getStateDump() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					sb.append(masu[yIndex][xIndex]);
				}
			}
			return sb.toString();
		}

		/**
		 * 餅は長方形になるように配置する。
		 * 長方形にできなかった場合はfalseを返す。
		 * // TODO なんか無駄な処理が多い気がする。
		 */
		public boolean rectSolve() {
			Set<Position> whitePosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
						Position pos = new Position(yIndex, xIndex);
						if (!fixedPosSet.contains(pos)) {
							whitePosSet.add(pos);
						}
					}
				}
			}
			while (!whitePosSet.isEmpty()) {
				Position typicalWhitePos = new ArrayList<>(whitePosSet).get(0);
				Set<Position> continuePosSet = new HashSet<>();
				continuePosSet.add(typicalWhitePos);
				setContinueWhitePosSet(typicalWhitePos, continuePosSet, null);
				int minY = getYLength() - 1;
				int maxY = 0;
				int minX = getXLength() - 1;
				int maxX = 0;
				for (Position pos : continuePosSet) {
					if (pos.getyIndex() < minY) {
						minY = pos.getyIndex();
					}
					if (pos.getyIndex() > maxY) {
						maxY = pos.getyIndex();
					}
					if (pos.getxIndex() < minX) {
						minX = pos.getxIndex();
					}
					if (pos.getxIndex() > maxX) {
						maxX = pos.getxIndex();
					}
				}
				int number = 0;
				for (int yIndex = minY; yIndex <= maxY; yIndex++) {
					for (int xIndex = minX; xIndex <= maxX; xIndex++) {
						if (masu[yIndex][xIndex] == Masu.BLACK) {
							return false;
						}
						masu[yIndex][xIndex] = Masu.NOT_BLACK;
						if (numbers[yIndex][xIndex] != null && numbers[yIndex][xIndex] != -1) {
							if (number != 0) {
								// 2個以上の数字取り込み禁止
								return false;
							}
							number = numbers[yIndex][xIndex];
						}
					}
				}
				// 最大でどこまで膨らむか
				int maxYsize = getYLength();
				int maxXsize = getXLength();
				if (number != 0 && (maxY - minY + 1) * (maxX - minX + 1) == number) {
					maxYsize = maxY - minY + 1;
					maxXsize = maxX - minX + 1;
				} else {
					for (int candY = minY; candY <= maxY; candY++) {
						int xHukurami = 0;
						int targetX = minX - 1;
						while (targetX >= 0 && masu[candY][targetX] != Masu.BLACK) {
							targetX--;
							xHukurami++;
						}
						targetX = maxX + 1;
						while (targetX < getXLength() && masu[candY][targetX] != Masu.BLACK) {
							targetX++;
							xHukurami++;
						}
						if (maxX - minX + 1 + xHukurami < maxXsize) {
							maxXsize = maxX - minX + 1 + xHukurami;
						}
					}
					for (int candX = minX; candX <= maxX; candX++) {
						int yHukurami = 0;
						int targetY = minY - 1;
						while (targetY >= 0 && masu[targetY][candX] != Masu.BLACK) {
							targetY--;
							yHukurami++;
						}
						targetY = maxY + 1;
						while (targetY < getYLength() && masu[targetY][candX] != Masu.BLACK) {
							targetY++;
							yHukurami++;
						}
						if (maxY - minY + 1 + yHukurami < maxYsize) {
							maxYsize = maxY - minY + 1 + yHukurami;
						}
					}
					if (number != 0) {
						// 膨らむ候補との突合せ
						boolean isOkMochi = false;
						for (int candY = maxY - minY + 1; candY <= maxYsize; candY++) {
							for (int candX = maxX - minX + 1; candX <= maxXsize; candX++) {
								if (candY * candX == number) {
									isOkMochi = true;
									break;
								}
								if (candY * candX > number) {
									break;
								}
							}
							if (isOkMochi) {
								break;
							}
						}
						if (!isOkMochi) {
							return false;
						}
					}
				}
				// 餅の形状が確定。周囲を黒で囲み、頂点接続判定に進む
				if (maxYsize == maxY - minY + 1 && maxXsize == maxX - minX + 1) {
					for (int candY = minY; candY <= maxY; candY++) {
						if (minX > 0) {
							masu[candY][minX - 1] = Masu.BLACK;
						}
						if (maxX < getXLength() - 1) {
							masu[candY][maxX + 1] = Masu.BLACK;
						}
					}
					for (int candX = minX; candX <= maxX; candX++) {
						if (minY > 0) {
							masu[minY - 1][candX] = Masu.BLACK;
						}
						if (maxY < getYLength() - 1) {
							masu[maxY + 1][candX] = Masu.BLACK;
						}
					}
					int blackCnt = 0;
					int whiteCnt = 0;
					Masu masuUL1 = minY <= 0 || minX <= 0 ? Masu.BLACK : masu[minY - 1][minX - 1];
					if (masuUL1 == Masu.BLACK) {
						blackCnt++;
					} else if (masuUL1 == Masu.NOT_BLACK) {
						whiteCnt++;
					}
					Masu masuUR1 = minY <= 0 || maxX >= getXLength() - 1 ? Masu.BLACK : masu[minY - 1][maxX + 1];
					if (masuUR1 == Masu.BLACK) {
						blackCnt++;
					} else if (masuUR1 == Masu.NOT_BLACK) {
						whiteCnt++;
					}
					Masu masuDR1 = maxY >= getYLength() - 1 || maxX >= getXLength() - 1 ? Masu.BLACK
							: masu[maxY + 1][maxX + 1];
					if (masuDR1 == Masu.BLACK) {
						blackCnt++;
					} else if (masuDR1 == Masu.NOT_BLACK) {
						whiteCnt++;
					}
					Masu masuDL1 = maxY >= getYLength() - 1 || minX <= 0 ? Masu.BLACK : masu[maxY + 1][minX - 1];
					if (masuDL1 == Masu.BLACK) {
						blackCnt++;
					} else if (masuDL1 == Masu.NOT_BLACK) {
						whiteCnt++;
					}
					// 頂点は必ず白マス2個、黒マス2個になる
					if (2 < blackCnt) {
						// 黒マス過剰
						return false;
					}
					if (2 > 4 - whiteCnt) {
						// 黒マス不足
						return false;
					}
					if (2 == blackCnt) {
						for (int candY = minY; candY <= maxY; candY++) {
							for (int candX = minX; candX <= maxX; candX++) {
								fixedPosSet.add(new Position(candY, candX));
							}
						}
						if (masuUL1 == Masu.SPACE) {
							masu[minY - 1][minX - 1] = Masu.NOT_BLACK;
						}
						if (masuUR1 == Masu.SPACE) {
							masu[minY - 1][maxX + 1] = Masu.NOT_BLACK;
						}
						if (masuDR1 == Masu.SPACE) {
							masu[maxY + 1][maxX + 1] = Masu.NOT_BLACK;
						}
						if (masuDL1 == Masu.SPACE) {
							masu[maxY + 1][minX - 1] = Masu.NOT_BLACK;
						}
					}
					if (2 == 4 - whiteCnt) {
						for (int candY = minY; candY <= maxY; candY++) {
							for (int candX = minX; candX <= maxX; candX++) {
								fixedPosSet.add(new Position(candY, candX));
							}
						}
						if (masuUL1 == Masu.SPACE) {
							masu[minY - 1][minX - 1] = Masu.BLACK;
						}
						if (masuUR1 == Masu.SPACE) {
							masu[minY - 1][maxX + 1] = Masu.BLACK;
						}
						if (masuDR1 == Masu.SPACE) {
							masu[maxY + 1][maxX + 1] = Masu.BLACK;
						}
						if (masuDL1 == Masu.SPACE) {
							masu[maxY + 1][minX - 1] = Masu.BLACK;
						}
					}
				}
				whitePosSet.removeAll(continuePosSet);
			}
			return true;
		}

		/**
		 * posを起点に上下左右に白確定マスを無制限につなげていく。
		 */
		private void setContinueWhitePosSet(Position pos, Set<Position> continuePosSet, Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.NOT_BLACK
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setContinueWhitePosSet(nextPos, continuePosSet, Direction.DOWN);
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.NOT_BLACK
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setContinueWhitePosSet(nextPos, continuePosSet, Direction.LEFT);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.NOT_BLACK
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setContinueWhitePosSet(nextPos, continuePosSet, Direction.UP);
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.NOT_BLACK
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setContinueWhitePosSet(nextPos, continuePosSet, Direction.RIGHT);
				}
			}
		}

		/**
		 * 餅を斜めにたどってもひとつながりにならない場合Falseを返す。
		 */
		public boolean connectSolve() {
			Set<Position> whitePosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
						Position whitePos = new Position(yIndex, xIndex);
						if (whitePosSet.size() == 0) {
							whitePosSet.add(whitePos);
							setContinueDiagonalPosSet(whitePos, whitePosSet, null);
						} else {
							if (!whitePosSet.contains(whitePos)) {
								return false;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * posを起点に上下左右ななめに黒確定でないマスを無制限につなげていく。
		 */
		private void setContinueDiagonalPosSet(Position pos, Set<Position> continuePosSet, Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueDiagonalPosSet(nextPos, continuePosSet, Direction.DOWN);
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueDiagonalPosSet(nextPos, continuePosSet, Direction.LEFT);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueDiagonalPosSet(nextPos, continuePosSet, Direction.UP);
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueDiagonalPosSet(nextPos, continuePosSet, Direction.RIGHT);
				}
			}
			if (pos.getyIndex() != 0 && pos.getxIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueDiagonalPosSet(nextPos, continuePosSet, null);
				}
			}
			if (pos.getyIndex() != 0 && pos.getxIndex() != getXLength() - 1) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueDiagonalPosSet(nextPos, continuePosSet, null);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && pos.getxIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueDiagonalPosSet(nextPos, continuePosSet, null);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && pos.getxIndex() != getXLength() - 1) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueDiagonalPosSet(nextPos, continuePosSet, null);
				}
			}
		}

		/**
		 * 各種チェックを1セット実行
		 * @param recursive
		 */
		private boolean solveAndCheck() {
			String str = getStateDump();
			if (!rectSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			} else {
				if (!connectSolve()) {
					return false;
				}
			}
			return true;
		}

		public boolean isSolved() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.SPACE) {
						return false;
					}
				}
			}
			return solveAndCheck();
		}

	}

	private final Field field;
	private int count = 0;

	public ScrinSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		String url = ""; //urlを入れれば試せる
		String[] params = url.split("/");
		int height = Integer.parseInt(params[params.length - 2]);
		int width = Integer.parseInt(params[params.length - 3]);
		String param = params[params.length - 1];
		System.out.println(new ScrinSolver(height, width, param).solve());
	}

	@Override
	public String solve() {
		long start = System.nanoTime();
		while (!field.isSolved()) {
			System.out.println(field);
			String befStr = field.getStateDump();
			if (!field.solveAndCheck()) {
				System.out.println(field);
				return "問題に矛盾がある可能性があります。途中経過を返します。";
			}
			int recursiveCnt = 0;
			while (field.getStateDump().equals(befStr) && recursiveCnt < 3) {
				if (!candSolve(field, recursiveCnt == 2 ? 999 : recursiveCnt)) {
					System.out.println(field);
					return "問題に矛盾がある可能性があります。途中経過を返します。";
				}
				recursiveCnt++;
			}
			if (recursiveCnt == 3 && field.getStateDump().equals(befStr)) {
				System.out.println(field);
				return "解けませんでした。途中経過を返します。";
			}
		}
		System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
		System.out.println("難易度:" + (count / 5));
		System.out.println(field);
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count / 5).toString();
	}

	/**
	 * 仮置きして調べる
	 */
	private boolean candSolve(Field field, int recursive) {
		String str = field.getStateDump();
		for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				if (field.masu[yIndex][xIndex] == Masu.SPACE) {
					count++;
					if (!oneCandSolve(field, yIndex, xIndex, recursive)) {
						return false;
					}
				}
			}
		}
		if (!field.getStateDump().equals(str)) {
			return candSolve(field, recursive);
		}
		return true;
	}

	/**
	 * 1つのマスに対する仮置き調査
	 */
	private boolean oneCandSolve(Field field, int yIndex, int xIndex, int recursive) {
		Field virtual = new Field(field);
		virtual.masu[yIndex][xIndex] = Masu.BLACK;
		boolean allowBlack = virtual.solveAndCheck();
		if (allowBlack && recursive > 0) {
			if (!candSolve(virtual, recursive - 1)) {
				allowBlack = false;
			}
		}
		Field virtual2 = new Field(field);
		virtual2.masu[yIndex][xIndex] = Masu.NOT_BLACK;
		boolean allowNotBlack = virtual2.solveAndCheck();
		if (allowNotBlack && recursive > 0) {
			if (!candSolve(virtual2, recursive - 1)) {
				allowNotBlack = false;
			}
		}
		if (!allowBlack && !allowNotBlack) {
			return false;
		} else if (!allowBlack) {
			field.masu = virtual2.masu;
		} else if (!allowNotBlack) {
			field.masu = virtual.masu;
		}

		return true;
	}
}