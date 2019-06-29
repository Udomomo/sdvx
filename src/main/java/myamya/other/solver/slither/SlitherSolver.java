package myamya.other.solver.slither;

import java.util.HashSet;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Solver;

public class SlitherSolver implements Solver {
	public enum Wall {
		SPACE("　"), NOT_EXISTS("・"), EXISTS("■");

		String str;

		Wall(String str) {
			this.str = str;
		}

		@Override
		public String toString() {
			return str;
		}
	}

	public static class Field {
		static final String ALPHABET = "abcde";
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		// 数字の情報
		private final Integer[][] numbers;
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,-1と0,0の間に壁があるという意味。外壁有無も考慮が必要なので注意
		private Wall[][] yokoExtraWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、-1,0と0,0の間に壁があるという意味。外壁有無も考慮が必要なので注意
		private Wall[][] tateExtraWall;

		public Integer[][] getNumbers() {
			return numbers;
		}

		public int getYLength() {
			return numbers.length;
		}

		public int getXLength() {
			return numbers[0].length;
		}

		public Wall[][] getYokoExtraWall() {
			return yokoExtraWall;
		}

		public Wall[][] getTateExtraWall() {
			return tateExtraWall;
		}

		public Field(int height, int width, String param) {
			numbers = new Integer[height][width];
			yokoExtraWall = new Wall[height][width + 1];
			tateExtraWall = new Wall[height + 1][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
					yokoExtraWall[yIndex][xIndex] = Wall.SPACE;
				}
			}
			for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					tateExtraWall[yIndex][xIndex] = Wall.SPACE;
				}
			}
			int index = 0;
			for (int i = 0; i < param.length(); i++) {
				char ch = param.charAt(i);
				Position pos = new Position(index / getXLength(), index % getXLength());
				if (ch == '.') {
					index++;
				} else {
					int interval = ALPHABET_FROM_G.indexOf(ch);
					if (interval != -1) {
						index = index + interval + 1;
					} else {
						if (ch == 'a' || ch == 'b' || ch == 'c' || ch == 'd' || ch == 'e') {
							numbers[pos.getyIndex()][pos.getxIndex()] = ALPHABET.indexOf(ch);
							index++;
							index++;
						} else if (ch == '5' || ch == '6' || ch == '7' || ch == '8' || ch == '9') {
							numbers[pos.getyIndex()][pos.getxIndex()] = Character.getNumericValue(ch) - 5;
							index++;
						} else if (ch == '0' || ch == '1' || ch == '2' || ch == '3' || ch == '4') {
							numbers[pos.getyIndex()][pos.getxIndex()] = Character.getNumericValue(ch);
						}
						index++;
					}
				}
			}
		}

		public Field(Field other) {
			numbers = other.numbers;
			yokoExtraWall = new Wall[other.getYLength()][other.getXLength() + 1];
			tateExtraWall = new Wall[other.getYLength() + 1][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
					yokoExtraWall[yIndex][xIndex] = other.yokoExtraWall[yIndex][xIndex];
				}
			}
			for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					tateExtraWall[yIndex][xIndex] = other.tateExtraWall[yIndex][xIndex];
				}
			}
		}

		private static final String FULL_NUMS = "０１２３";

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
				sb.append("□");
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					sb.append(tateExtraWall[yIndex][xIndex]);
					sb.append("□");
				}
				sb.append(System.lineSeparator());
				if (yIndex != getYLength()) {
					for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
						sb.append(yokoExtraWall[yIndex][xIndex]);
						if (xIndex != getXLength()) {
							if (numbers[yIndex][xIndex] != null) {
								sb.append(FULL_NUMS.substring(numbers[yIndex][xIndex],
										numbers[yIndex][xIndex] + 1));
							} else {
								sb.append("　");
							}
						}
					}
				}
				sb.append(System.lineSeparator());
			}
			return sb.toString();
		}

		public String getStateDump() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
					sb.append(yokoExtraWall[yIndex][xIndex]);
				}
			}
			for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					sb.append(tateExtraWall[yIndex][xIndex]);
				}
			}
			return sb.toString();
		}

		/**
		 * 数字の周りに壁がいつくあるか調べる。
		 * 矛盾したらfalseを返す。
		 */
		public boolean numberSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						int existsCount = 0;
						int notExistsCount = 0;
						Wall wallUp = tateExtraWall[yIndex][xIndex];
						if (wallUp == Wall.EXISTS) {
							existsCount++;
						} else if (wallUp == Wall.NOT_EXISTS) {
							notExistsCount++;
						}
						Wall wallRight = yokoExtraWall[yIndex][xIndex + 1];
						if (wallRight == Wall.EXISTS) {
							existsCount++;
						} else if (wallRight == Wall.NOT_EXISTS) {
							notExistsCount++;
						}
						Wall wallDown = tateExtraWall[yIndex + 1][xIndex];
						if (wallDown == Wall.EXISTS) {
							existsCount++;
						} else if (wallDown == Wall.NOT_EXISTS) {
							notExistsCount++;
						}
						Wall wallLeft = yokoExtraWall[yIndex][xIndex];
						if (wallLeft == Wall.EXISTS) {
							existsCount++;
						} else if (wallLeft == Wall.NOT_EXISTS) {
							notExistsCount++;
						}
						if (existsCount > numbers[yIndex][xIndex] ||
								notExistsCount > 4 - numbers[yIndex][xIndex]) {
							return false;
						} else {
							if (existsCount == numbers[yIndex][xIndex]) {
								if (wallUp == Wall.SPACE) {
									tateExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
								}
								if (wallRight == Wall.SPACE) {
									yokoExtraWall[yIndex][xIndex + 1] = Wall.NOT_EXISTS;
								}
								if (wallDown == Wall.SPACE) {
									tateExtraWall[yIndex + 1][xIndex] = Wall.NOT_EXISTS;
								}
								if (wallLeft == Wall.SPACE) {
									yokoExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
								}
							}
							if (notExistsCount == 4 - numbers[yIndex][xIndex]) {
								if (wallUp == Wall.SPACE) {
									tateExtraWall[yIndex][xIndex] = Wall.EXISTS;
								}
								if (wallRight == Wall.SPACE) {
									yokoExtraWall[yIndex][xIndex + 1] = Wall.EXISTS;
								}
								if (wallDown == Wall.SPACE) {
									tateExtraWall[yIndex + 1][xIndex] = Wall.EXISTS;
								}
								if (wallLeft == Wall.SPACE) {
									yokoExtraWall[yIndex][xIndex] = Wall.EXISTS;
								}
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 柱につながる壁は必ず0か2になる。ならない場合falseを返す。
		 */
		private boolean nextSolve() {
			for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
					int existsCount = 0;
					int notExistsCount = 0;
					Wall wallUp = yIndex == 0 ? Wall.NOT_EXISTS : yokoExtraWall[yIndex - 1][xIndex];
					if (wallUp == Wall.EXISTS) {
						existsCount++;
					} else if (wallUp == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
					Wall wallDown = yIndex == getYLength() ? Wall.NOT_EXISTS
							: yokoExtraWall[yIndex][xIndex];
					if (wallDown == Wall.EXISTS) {
						existsCount++;
					} else if (wallDown == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
					Wall wallRight = xIndex == getXLength() ? Wall.NOT_EXISTS
							: tateExtraWall[yIndex][xIndex];
					if (wallRight == Wall.EXISTS) {
						existsCount++;
					} else if (wallRight == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
					Wall wallLeft = xIndex == 0 ? Wall.NOT_EXISTS
							: tateExtraWall[yIndex][xIndex - 1];
					if (wallLeft == Wall.EXISTS) {
						existsCount++;
					} else if (wallLeft == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
					if (existsCount > 2 || (existsCount == 1 && notExistsCount == 3)) {
						return false;
					}
					if (existsCount == 2) {
						if (wallUp == Wall.SPACE) {
							yokoExtraWall[yIndex - 1][xIndex] = Wall.NOT_EXISTS;
						}
						if (wallDown == Wall.SPACE) {
							yokoExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
						}
						if (wallRight == Wall.SPACE) {
							tateExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
						}
						if (wallLeft == Wall.SPACE) {
							tateExtraWall[yIndex][xIndex - 1] = Wall.NOT_EXISTS;
						}
					} else if (notExistsCount == 3) {
						if (wallUp == Wall.SPACE) {
							yokoExtraWall[yIndex - 1][xIndex] = Wall.NOT_EXISTS;
						}
						if (wallDown == Wall.SPACE) {
							yokoExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
						}
						if (wallRight == Wall.SPACE) {
							tateExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
						}
						if (wallLeft == Wall.SPACE) {
							tateExtraWall[yIndex][xIndex - 1] = Wall.NOT_EXISTS;
						}
					} else if (existsCount == 1 && notExistsCount == 2) {
						if (wallUp == Wall.SPACE) {
							yokoExtraWall[yIndex - 1][xIndex] = Wall.EXISTS;
						}
						if (wallDown == Wall.SPACE) {
							yokoExtraWall[yIndex][xIndex] = Wall.EXISTS;
						}
						if (wallRight == Wall.SPACE) {
							tateExtraWall[yIndex][xIndex] = Wall.EXISTS;
						}
						if (wallLeft == Wall.SPACE) {
							tateExtraWall[yIndex][xIndex - 1] = Wall.EXISTS;
						}
					}
				}
			}
			return true;
		}

		/**
		 * ルール上、各列をふさぐ壁は必ず偶数になる。
		 * 偶数になっていない場合falseを返す。
		 */
		private boolean oddSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				int existsCount = 0;
				for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
					if (yokoExtraWall[yIndex][xIndex] == Wall.SPACE) {
						existsCount = 0;
						break;
					} else if (yokoExtraWall[yIndex][xIndex] == Wall.EXISTS) {
						existsCount++;
					}
				}
				if (existsCount % 2 != 0) {
					return false;
				}
			}
			for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
				int existsCount = 0;
				for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
					if (tateExtraWall[yIndex][xIndex] == Wall.SPACE) {
						existsCount = 0;
						break;
					} else if (tateExtraWall[yIndex][xIndex] == Wall.EXISTS) {
						existsCount++;
					}
				}
				if (existsCount % 2 != 0) {
					return false;
				}
			}
			return true;
		}

		/**
		 * 壁が1つながりになっていない場合falseを返す。
		 */
		public boolean connectWhiteSolve() {
			Set<Position> yokoBlackWallPosSet = new HashSet<>();
			Set<Position> tateBlackWallPosSet = new HashSet<>();
			Position typicalExistPos = null;
			boolean isYokoWall = false;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
					if (yokoExtraWall[yIndex][xIndex] == Wall.EXISTS) {
						Position existPos = new Position(yIndex, xIndex);
						yokoBlackWallPosSet.add(existPos);
						if (typicalExistPos == null) {
							typicalExistPos = existPos;
							isYokoWall = true;
						}
					}
				}
			}
			for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (tateExtraWall[yIndex][xIndex] == Wall.EXISTS) {
						Position existPos = new Position(yIndex, xIndex);
						tateBlackWallPosSet.add(existPos);
						if (typicalExistPos == null) {
							typicalExistPos = existPos;
						}
					}
				}
			}
			if (typicalExistPos == null) {
				return true;
			} else {
				Set<Position> continueYokoWallPosSet = new HashSet<>();
				Set<Position> continueTateWallPosSet = new HashSet<>();
				if (isYokoWall) {
					continueYokoWallPosSet.add(typicalExistPos);
				} else {
					continueTateWallPosSet.add(typicalExistPos);
				}
				setContinueExistWallPosSet(typicalExistPos, continueYokoWallPosSet, continueTateWallPosSet, isYokoWall,
						null);
				yokoBlackWallPosSet.removeAll(continueYokoWallPosSet);
				tateBlackWallPosSet.removeAll(continueTateWallPosSet);
				return yokoBlackWallPosSet.isEmpty() && tateBlackWallPosSet.isEmpty();
			}
		}

		private void setContinueExistWallPosSet(Position pos, Set<Position> continueYokoWallPosSet,
				Set<Position> continueTateWallPosSet, boolean isYoko, Direction from) {
			if (isYoko) {
				if (pos.getxIndex() != 0 && from != Direction.UP) {
					Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
					if (!continueTateWallPosSet.contains(nextPos)
							&& tateExtraWall[nextPos.getyIndex()][nextPos.getxIndex()] != Wall.NOT_EXISTS) {
						continueTateWallPosSet.add(nextPos);
						setContinueExistWallPosSet(nextPos, continueYokoWallPosSet, continueTateWallPosSet, false,
								Direction.RIGHT);
					}
				}
				if (pos.getyIndex() != 0 && from != Direction.UP) {
					Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
					if (!continueYokoWallPosSet.contains(nextPos)
							&& yokoExtraWall[nextPos.getyIndex()][nextPos.getxIndex()] != Wall.NOT_EXISTS) {
						continueYokoWallPosSet.add(nextPos);
						setContinueExistWallPosSet(nextPos, continueYokoWallPosSet, continueTateWallPosSet, true,
								Direction.DOWN);
					}
				}
				if (pos.getxIndex() != getXLength() && from != Direction.UP) {
					Position nextPos = new Position(pos.getyIndex(), pos.getxIndex());
					if (!continueTateWallPosSet.contains(nextPos)
							&& tateExtraWall[nextPos.getyIndex()][nextPos.getxIndex()] != Wall.NOT_EXISTS) {
						continueTateWallPosSet.add(nextPos);
						setContinueExistWallPosSet(nextPos, continueYokoWallPosSet, continueTateWallPosSet, false,
								Direction.LEFT);
					}
				}
				if (pos.getxIndex() != getXLength() && from != Direction.DOWN) {
					Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
					if (!continueTateWallPosSet.contains(nextPos)
							&& tateExtraWall[nextPos.getyIndex()][nextPos.getxIndex()] != Wall.NOT_EXISTS) {
						continueTateWallPosSet.add(nextPos);
						setContinueExistWallPosSet(nextPos, continueYokoWallPosSet, continueTateWallPosSet, false,
								Direction.LEFT);
					}
				}
				if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
					Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
					if (!continueYokoWallPosSet.contains(nextPos)
							&& yokoExtraWall[nextPos.getyIndex()][nextPos.getxIndex()] != Wall.NOT_EXISTS) {
						continueYokoWallPosSet.add(nextPos);
						setContinueExistWallPosSet(nextPos, continueYokoWallPosSet, continueTateWallPosSet, true,
								Direction.UP);
					}
				}
				if (pos.getxIndex() != 0 && from != Direction.DOWN) {
					Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex() - 1);
					if (!continueTateWallPosSet.contains(nextPos)
							&& tateExtraWall[nextPos.getyIndex()][nextPos.getxIndex()] != Wall.NOT_EXISTS) {
						continueTateWallPosSet.add(nextPos);
						setContinueExistWallPosSet(nextPos, continueYokoWallPosSet, continueTateWallPosSet, false,
								Direction.RIGHT);
					}
				}
			} else {
				if (pos.getyIndex() != 0 && from != Direction.LEFT) {
					Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
					if (!continueYokoWallPosSet.contains(nextPos)
							&& yokoExtraWall[nextPos.getyIndex()][nextPos.getxIndex()] != Wall.NOT_EXISTS) {
						continueYokoWallPosSet.add(nextPos);
						setContinueExistWallPosSet(nextPos, continueYokoWallPosSet, continueTateWallPosSet, true,
								Direction.DOWN);
					}
				}
				if (pos.getyIndex() != 0 && from != Direction.RIGHT) {
					Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex() + 1);
					if (!continueYokoWallPosSet.contains(nextPos)
							&& yokoExtraWall[nextPos.getyIndex()][nextPos.getxIndex()] != Wall.NOT_EXISTS) {
						continueYokoWallPosSet.add(nextPos);
						setContinueExistWallPosSet(nextPos, continueYokoWallPosSet, continueTateWallPosSet, true,
								Direction.DOWN);
					}
				}
				if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
					Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
					if (!continueTateWallPosSet.contains(nextPos)
							&& tateExtraWall[nextPos.getyIndex()][nextPos.getxIndex()] != Wall.NOT_EXISTS) {
						continueTateWallPosSet.add(nextPos);
						setContinueExistWallPosSet(nextPos, continueYokoWallPosSet, continueTateWallPosSet, false,
								Direction.LEFT);
					}
				}
				if (pos.getyIndex() != getYLength() && from != Direction.RIGHT) {
					Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
					if (!continueYokoWallPosSet.contains(nextPos)
							&& yokoExtraWall[nextPos.getyIndex()][nextPos.getxIndex()] != Wall.NOT_EXISTS) {
						continueYokoWallPosSet.add(nextPos);
						setContinueExistWallPosSet(nextPos, continueYokoWallPosSet, continueTateWallPosSet, true,
								Direction.UP);
					}
				}
				if (pos.getyIndex() != getYLength() && from != Direction.LEFT) {
					Position nextPos = new Position(pos.getyIndex(), pos.getxIndex());
					if (!continueYokoWallPosSet.contains(nextPos)
							&& yokoExtraWall[nextPos.getyIndex()][nextPos.getxIndex()] != Wall.NOT_EXISTS) {
						continueYokoWallPosSet.add(nextPos);
						setContinueExistWallPosSet(nextPos, continueYokoWallPosSet, continueTateWallPosSet, true,
								Direction.UP);
					}
				}
				if (pos.getxIndex() != 0 && from != Direction.LEFT) {
					Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
					if (!continueTateWallPosSet.contains(nextPos)
							&& tateExtraWall[nextPos.getyIndex()][nextPos.getxIndex()] != Wall.NOT_EXISTS) {
						continueTateWallPosSet.add(nextPos);
						setContinueExistWallPosSet(nextPos, continueYokoWallPosSet, continueTateWallPosSet, false,
								Direction.RIGHT);
					}
				}
			}
		}

		/**
		 * フィールドに1つは壁が必要。
		 */
		private boolean finalSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
					if (yokoExtraWall[yIndex][xIndex] != Wall.NOT_EXISTS) {
						return true;
					}
				}
			}
			for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (tateExtraWall[yIndex][xIndex] != Wall.NOT_EXISTS) {
						return true;
					}
				}
			}
			return false;
		}

		/**
		 * 各種チェックを1セット実行
		 * @param recursive
		 */
		public boolean solveAndCheck() {
			String str = getStateDump();
			if (!numberSolve()) {
				return false;
			}
			if (!nextSolve()) {
				return false;
			}
			if (!oddSolve()) {
				return false;
			}
			if (!connectWhiteSolve()) {
				return false;
			}
			if (!finalSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			}
			return true;
		}

		public boolean isSolved() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
					if (yokoExtraWall[yIndex][xIndex] == Wall.SPACE) {
						return false;
					}
				}
			}
			for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (tateExtraWall[yIndex][xIndex] == Wall.SPACE) {
						return false;
					}
				}
			}
			return solveAndCheck();
		}

	}

	private final Field field;
	private int count = 0;

	public SlitherSolver(int height, int width, String param) {
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
		System.out.println(new SlitherSolver(height, width, param).solve());
	}

	@Override
	public String solve() {
		long start = System.nanoTime();
		while (!field.isSolved()) {
			System.out.println(field);
			String befStr = field.getStateDump();
			if (!field.solveAndCheck()) {
				return "問題に矛盾がある可能性があります。途中経過を返します。";
			}
			int recursiveCnt = 0;
			while (field.getStateDump().equals(befStr) && recursiveCnt < 2) {
				if (!candSolve(field, recursiveCnt * 3)) {
					return "問題に矛盾がある可能性があります。途中経過を返します。";
				}
				recursiveCnt++;
			}
			if (recursiveCnt == 2 && field.getStateDump().equals(befStr)) {
				return "解けませんでした。途中経過を返します。";
			}
		}
		System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
		System.out.println("難易度:" + count * 2);
		System.out.println(field);
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count * 2).toString();
	}

	/**
	 * 仮置きして調べる
	 */
	private boolean candSolve(Field field, int recursive) {
		String str = field.getStateDump();
		for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength() + 1; xIndex++) {
				if (field.yokoExtraWall[yIndex][xIndex] == Wall.SPACE) {
					count++;
					if (!oneCandYokoWallSolve(field, yIndex, xIndex, recursive)) {
						return false;
					}
				}
			}
		}
		for (int yIndex = 0; yIndex < field.getYLength() + 1; yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				if (field.tateExtraWall[yIndex][xIndex] == Wall.SPACE) {
					count++;
					if (!oneCandTateWallSolve(field, yIndex, xIndex, recursive)) {
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

	private boolean oneCandYokoWallSolve(Field field, int yIndex, int xIndex, int recursive) {
		Field virtual = new Field(field);
		virtual.yokoExtraWall[yIndex][xIndex] = Wall.EXISTS;
		boolean allowBlack = virtual.solveAndCheck();
		if (allowBlack && recursive > 0) {
			if (!candSolve(virtual, recursive - 1)) {
				allowBlack = false;
			}
		}
		Field virtual2 = new Field(field);
		virtual2.yokoExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
		boolean allowNotBlack = virtual2.solveAndCheck();
		if (allowNotBlack && recursive > 0) {
			if (!candSolve(virtual2, recursive - 1)) {
				allowNotBlack = false;
			}
		}
		if (!allowBlack && !allowNotBlack) {
			return false;
		} else if (!allowBlack) {
			field.tateExtraWall = virtual2.tateExtraWall;
			field.yokoExtraWall = virtual2.yokoExtraWall;
		} else if (!allowNotBlack) {
			field.tateExtraWall = virtual.tateExtraWall;
			field.yokoExtraWall = virtual.yokoExtraWall;
		}
		return true;
	}

	private boolean oneCandTateWallSolve(Field field, int yIndex, int xIndex, int recursive) {
		Field virtual = new Field(field);
		virtual.tateExtraWall[yIndex][xIndex] = Wall.EXISTS;
		boolean allowBlack = virtual.solveAndCheck();
		if (allowBlack && recursive > 0) {
			if (!candSolve(virtual, recursive - 1)) {
				allowBlack = false;
			}
		}
		Field virtual2 = new Field(field);
		virtual2.tateExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
		boolean allowNotBlack = virtual2.solveAndCheck();
		if (allowNotBlack && recursive > 0) {
			if (!candSolve(virtual2, recursive - 1)) {
				allowNotBlack = false;
			}
		}
		if (!allowBlack && !allowNotBlack) {
			return false;
		} else if (!allowBlack) {
			field.tateExtraWall = virtual2.tateExtraWall;
			field.yokoExtraWall = virtual2.yokoExtraWall;
		} else if (!allowNotBlack) {
			field.tateExtraWall = virtual.tateExtraWall;
			field.yokoExtraWall = virtual.yokoExtraWall;
		}
		return true;
	}
}