package myamya.other.solver.norinori;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.GeneratorResult;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Generator;
import myamya.other.solver.RoomMaker;
import myamya.other.solver.Solver;

public class NorinoriSolver implements Solver {
	public static class NorinoriGenerator implements Generator {

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		static class NorinoriSolverForGenerator extends NorinoriSolver {
			private final int limit;

			public NorinoriSolverForGenerator(Field field, int limit) {
				super(field);
				this.limit = limit;
			}

			public int solve2() {
				while (!field.isSolved()) {
					String befStr = field.getStateDump();
					if (!field.solveAndCheck()) {
						return -1;
					}
					int recursiveCnt = 0;
					while (field.getStateDump().equals(befStr) && recursiveCnt < 3) {
						if (!candSolve(field, recursiveCnt == 2 ? 999 : recursiveCnt)) {
							return -1;

						}
						recursiveCnt++;
					}
					if (recursiveCnt == 3 && field.getStateDump().equals(befStr)) {
						return -1;

					}
				}
				return count;
			}

			@Override
			protected boolean candSolve(Field field, int recursive) {
				if (this.count >= limit) {
					return false;
				} else {
					return super.candSolve(field, recursive);
				}
			}
		}

		private final int height;
		private final int width;

		public NorinoriGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new NorinoriGenerator(8, 8).generate();
		}

		@Override
		public GeneratorResult generate() {
			NorinoriSolver.Field wkField = new NorinoriSolver.Field(height, width, RoomMaker.roomMake(height, width));
			int level = 0;
			long start = System.nanoTime();
			while (true) {
				// 解けるかな？
				System.out.println(wkField);
				level = new NorinoriSolverForGenerator(wkField, 1000).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new NorinoriSolver.Field(height, width, RoomMaker.roomMake(height, width));
				} else {
					break;
				}
			}
			level = (int) Math.sqrt(level * 5 / 3);
			String status = "Lv:" + level + "の問題を獲得！(数字/四角：" + wkField.getHintCount().split("/")[0] + "/"
					+ wkField.getHintCount().split("/")[1] + ")";
			String url = wkField.getPuzPreURL();
			String link = "<a href=\"" + url + "\" target=\"_blank\">ぱずぷれv3で解く</a>";
			StringBuilder sb = new StringBuilder();
			//			int baseSize = 20;
			//			int margin = 5;
			//			sb.append(
			//					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
			//							+ "height=\"" + (wkField.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
			//							+ (wkField.getXLength() * baseSize + 2 * baseSize) + "\" >");
			//			// 数字描画
			//			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
			//				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
			//					if (wkField.getNumbers()[yIndex][xIndex] != null) {
			//						sb.append("<rect y=\"" + (yIndex * baseSize + 2 + margin)
			//								+ "\" x=\""
			//								+ (xIndex * baseSize + baseSize + 2)
			//								+ "\" width=\""
			//								+ (baseSize - 4)
			//								+ "\" height=\""
			//								+ (baseSize - 4)
			//								+ "\" fill=\"white\" stroke-width=\"1\" stroke=\"black\">"
			//								+ "\">"
			//								+ "</rect>");
			//						if (wkField.getNumbers()[yIndex][xIndex] != -1) {
			//							String numberStr = String.valueOf(wkField.getNumbers()[yIndex][xIndex]);
			//							int numIdx = HALF_NUMS.indexOf(numberStr);
			//							String masuStr = null;
			//							if (numIdx >= 0) {
			//								masuStr = FULL_NUMS.substring(numIdx / 2, numIdx / 2 + 1);
			//							} else {
			//								masuStr = numberStr;
			//							}
			//							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4 + margin)
			//									+ "\" x=\""
			//									+ (xIndex * baseSize + baseSize + 2)
			//									+ "\" font-size=\""
			//									+ (baseSize - 5)
			//									+ "\" textLength=\""
			//									+ (baseSize - 5)
			//									+ "\" lengthAdjust=\"spacingAndGlyphs\">"
			//									+ masuStr
			//									+ "</text>");
			//						}
			//					}
			//				}
			//			}
			//			// 横壁描画
			//			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
			//				for (int xIndex = -1; xIndex < wkField.getXLength(); xIndex++) {
			//					boolean oneYokoWall = xIndex == -1 || xIndex == wkField.getXLength() - 1;
			//					sb.append("<line y1=\""
			//							+ (yIndex * baseSize + margin)
			//							+ "\" x1=\""
			//							+ (xIndex * baseSize + 2 * baseSize)
			//							+ "\" y2=\""
			//							+ (yIndex * baseSize + baseSize + margin)
			//							+ "\" x2=\""
			//							+ (xIndex * baseSize + 2 * baseSize)
			//							+ "\" stroke-width=\"1\" fill=\"none\"");
			//					if (oneYokoWall) {
			//						sb.append("stroke=\"#000\" ");
			//					} else {
			//						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
			//					}
			//					sb.append(">"
			//							+ "</line>");
			//				}
			//			}
			//			// 縦壁描画
			//			for (int yIndex = -1; yIndex < wkField.getYLength(); yIndex++) {
			//				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
			//					boolean oneTateWall = yIndex == -1 || yIndex == wkField.getYLength() - 1;
			//					sb.append("<line y1=\""
			//							+ (yIndex * baseSize + baseSize + margin)
			//							+ "\" x1=\""
			//							+ (xIndex * baseSize + baseSize)
			//							+ "\" y2=\""
			//							+ (yIndex * baseSize + baseSize + margin)
			//							+ "\" x2=\""
			//							+ (xIndex * baseSize + baseSize + baseSize)
			//							+ "\" stroke-width=\"1\" fill=\"none\"");
			//					if (oneTateWall) {
			//						sb.append("stroke=\"#000\" ");
			//					} else {
			//						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
			//					}
			//					sb.append(">"
			//							+ "</line>");
			//				}
			//			}
			//			sb.append("</svg>");
			System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
			System.out.println(level);
			System.out.println(wkField.getHintCount());
			System.out.println(wkField);
			return new GeneratorResult(status, sb.toString(), link, url, level, "");

		}

	}

	public static class Field {
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";
		static final int BLACK_CNT = 2;

		// マスの情報
		private Masu[][] masu;
		// 同一グループに属するマスの情報
		private final List<Set<Position>> rooms;

		public Masu[][] getMasu() {
			return masu;
		}

		public String getPuzPreURL() {
			// TODO 自動生成されたメソッド・スタブ
			return null;
		}

		public String getHintCount() {
			// TODO 自動生成されたメソッド・スタブ
			return "0/0";
		}

		public int getYLength() {
			return masu.length;
		}

		public int getXLength() {
			return masu[0].length;
		}

		public Field(int height, int width, String param) {
			masu = new Masu[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
			// パラメータを解釈して壁の有無を入れる
			boolean[][] yokoWall = new boolean[height][width - 1];
			boolean[][] tateWall = new boolean[height - 1][width];
			int readPos = 0;
			int bit = 0;
			for (int cnt = 0; cnt < getYLength() * (getXLength() - 1); cnt++) {
				int mod = cnt % 5;
				if (mod == 0) {
					bit = Character.getNumericValue(param.charAt(readPos));
					readPos++;
				}
				if (mod == 4 || cnt == (getYLength() * (getXLength() - 1)) - 1) {
					if (mod >= 0) {
						yokoWall[(cnt - mod + 0) / (getXLength() - 1)][(cnt - mod + 0) % (getXLength() - 1)] = bit / 16
								% 2 == 1;
					}
					if (mod >= 1) {
						yokoWall[(cnt - mod + 1) / (getXLength() - 1)][(cnt - mod + 1) % (getXLength() - 1)] = bit / 8
								% 2 == 1;
					}
					if (mod >= 2) {
						yokoWall[(cnt - mod + 2) / (getXLength() - 1)][(cnt - mod + 2) % (getXLength() - 1)] = bit / 4
								% 2 == 1;
					}
					if (mod >= 3) {
						yokoWall[(cnt - mod + 3) / (getXLength() - 1)][(cnt - mod + 3) % (getXLength() - 1)] = bit / 2
								% 2 == 1;
					}
					if (mod >= 4) {
						yokoWall[(cnt - mod + 4) / (getXLength() - 1)][(cnt - mod + 4) % (getXLength() - 1)] = bit / 1
								% 2 == 1;
					}
				}
			}
			for (int cnt = 0; cnt < (getYLength() - 1) * getXLength(); cnt++) {
				int mod = cnt % 5;
				if (mod == 0) {
					bit = Character.getNumericValue(param.charAt(readPos));
					readPos++;
				}
				if (mod == 4 || cnt == ((getYLength() - 1) * getXLength()) - 1) {
					if (mod >= 0) {
						tateWall[(cnt - mod + 0) / getXLength()][(cnt - mod + 0) % getXLength()] = bit / 16 % 2 == 1;
					}
					if (mod >= 1) {
						tateWall[(cnt - mod + 1) / getXLength()][(cnt - mod + 1) % getXLength()] = bit / 8 % 2 == 1;
					}
					if (mod >= 2) {
						tateWall[(cnt - mod + 2) / getXLength()][(cnt - mod + 2) % getXLength()] = bit / 4 % 2 == 1;
					}
					if (mod >= 3) {
						tateWall[(cnt - mod + 3) / getXLength()][(cnt - mod + 3) % getXLength()] = bit / 2 % 2 == 1;
					}
					if (mod >= 4) {
						tateWall[(cnt - mod + 4) / getXLength()][(cnt - mod + 4) % getXLength()] = bit / 1 % 2 == 1;
					}
				}
			}
			// 縦と横の壁の関係からにょろっと部屋を決めていく
			rooms = new ArrayList<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					Position pos = new Position(yIndex, xIndex);
					boolean alreadyRoomed = false;
					for (Set<Position> room : rooms) {
						if (room.contains(pos)) {
							alreadyRoomed = true;
							break;
						}
					}
					if (!alreadyRoomed) {
						Set<Position> continuePosSet = new HashSet<>();
						continuePosSet.add(pos);
						setContinuePosSet(pos, continuePosSet, yokoWall, tateWall);
						rooms.add(continuePosSet);
					}
				}
			}
		}

		public Field(Field other) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
				}
			}
			// 壁・部屋は参照渡しで使い回し(一度Fieldができたら変化しないはずなので。)
			rooms = other.rooms;
		}

		public Field(int height, int width, List<Set<Position>> rooms) {
			masu = new Masu[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
			this.rooms = rooms;
		}

		// posを起点に上下左右に壁または白確定でないマスを無制限につなげていく。
		private void setContinuePosSet(Position pos, Set<Position> continuePosSet, boolean[][] yokoWall,
				boolean[][] tateWall) {
			if (pos.getyIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && !tateWall[pos.getyIndex() - 1][pos.getxIndex()]
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, yokoWall, tateWall);
				}
			}
			if (pos.getxIndex() != getXLength() - 1) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos) && !yokoWall[pos.getyIndex()][pos.getxIndex()]
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, yokoWall, tateWall);
				}
			}
			if (pos.getyIndex() != getYLength() - 1) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && !tateWall[pos.getyIndex()][pos.getxIndex()]
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, yokoWall, tateWall);
				}
			}
			if (pos.getxIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos) && !yokoWall[pos.getyIndex()][pos.getxIndex() - 1]
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, yokoWall, tateWall);
				}
			}
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int xIndex = 0; xIndex < getXLength() * 2 + 1; xIndex++) {
				sb.append("□");
			}
			sb.append(System.lineSeparator());
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				sb.append("□");
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					sb.append(masu[yIndex][xIndex]);
					if (xIndex != getXLength() - 1) {
						sb.append(isExistYokoWall(yIndex, xIndex) ? "□" : "　");
					}
				}
				sb.append("□");
				sb.append(System.lineSeparator());
				if (yIndex != getYLength() - 1) {
					sb.append("□");
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						sb.append(isExistTateWall(yIndex, xIndex) ? "□" : "　");
						if (xIndex != getXLength() - 1) {
							sb.append("□");
						}
					}
					sb.append("□");
					sb.append(System.lineSeparator());
				}
			}
			for (int xIndex = 0; xIndex < getXLength() * 2 + 1; xIndex++) {
				sb.append("□");
			}
			sb.append(System.lineSeparator());
			return sb.toString();
		}

		public boolean isExistYokoWall(int yIndex, int xIndex) {
			Position pos = new Position(yIndex, xIndex);
			for (Set<Position> room : rooms) {
				if (room.contains(pos)) {
					Position rightPos = new Position(yIndex, xIndex + 1);
					if (room.contains(rightPos)) {
						return false;
					}
				}
			}
			return true;
		}

		public boolean isExistTateWall(int yIndex, int xIndex) {
			Position pos = new Position(yIndex, xIndex);
			for (Set<Position> room : rooms) {
				if (room.contains(pos)) {
					Position downPos = new Position(yIndex + 1, xIndex);
					if (room.contains(downPos)) {
						return false;
					}
				}
			}
			return true;
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
		 * 部屋のマスを埋める。黒マス不足・過剰はfalseを返す。
		 */
		public boolean roomSolve() {
			for (Set<Position> room : rooms) {
				// 部屋に対する調査
				int blackCnt = 0;
				int spaceCnt = 0;
				for (Position pos : room) {
					if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK) {
						blackCnt++;
					} else if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
						spaceCnt++;
					}
				}
				if (blackCnt + spaceCnt < BLACK_CNT) {
					// 黒マス不足
					return false;
				}
				// 置かねばならない黒マスの数
				int retainBlackCnt = BLACK_CNT - blackCnt;
				if (retainBlackCnt < 0) {
					// 黒マス超過
					return false;
				} else if (retainBlackCnt == 0) {
					// 黒マス数が既に部屋の黒マス数に等しければ、部屋の他のマスは白マス
					for (Position pos : room) {
						if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
							masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
						}
					}
				} else if (spaceCnt == retainBlackCnt) {
					// 未確定マスが置かねばならない黒マスの数に等しければ、未確定マスは黒マス
					for (Position pos : room) {
						if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
							masu[pos.getyIndex()][pos.getxIndex()] = Masu.BLACK;
						}
					}
				}
			}
			return true;
		}

		/**
		 * のりの周りを白マスに確定する。
		 * のりの周りに黒マスがあった場合falseを返す。
		 */
		public boolean noriRoundSolve() {
			// 縦ののり
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					Masu masuPivot = masu[yIndex][xIndex];
					Masu masuDown = masu[yIndex + 1][xIndex];
					if (masuPivot == Masu.BLACK && masuDown == Masu.BLACK) {
						if (yIndex != 0) {
							if (masu[yIndex - 1][xIndex] == Masu.BLACK) {
								return false;
							} else {
								masu[yIndex - 1][xIndex] = Masu.NOT_BLACK;
							}
						}
						if (xIndex != 0) {
							if (masu[yIndex][xIndex - 1] == Masu.BLACK) {
								return false;
							} else {
								masu[yIndex][xIndex - 1] = Masu.NOT_BLACK;
							}
							if (masu[yIndex + 1][xIndex - 1] == Masu.BLACK) {
								return false;
							} else {
								masu[yIndex + 1][xIndex - 1] = Masu.NOT_BLACK;
							}
						}
						if (xIndex != getXLength() - 1) {
							if (masu[yIndex][xIndex + 1] == Masu.BLACK) {
								return false;
							} else {
								masu[yIndex][xIndex + 1] = Masu.NOT_BLACK;
							}
							if (masu[yIndex + 1][xIndex + 1] == Masu.BLACK) {
								return false;
							} else {
								masu[yIndex + 1][xIndex + 1] = Masu.NOT_BLACK;
							}
						}
						if (yIndex != getYLength() - 2) {
							if (masu[yIndex + 2][xIndex] == Masu.BLACK) {
								return false;
							} else {
								masu[yIndex + 2][xIndex] = Masu.NOT_BLACK;
							}
						}

					}
				}
			}

			// 横ののり
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					Masu masuPivot = masu[yIndex][xIndex];
					Masu masuRight = masu[yIndex][xIndex + 1];
					if (masuPivot == Masu.BLACK && masuRight == Masu.BLACK) {
						if (yIndex != 0) {
							if (masu[yIndex - 1][xIndex] == Masu.BLACK) {
								return false;
							} else {
								masu[yIndex - 1][xIndex] = Masu.NOT_BLACK;
							}
							if (masu[yIndex - 1][xIndex + 1] == Masu.BLACK) {
								return false;
							} else {
								masu[yIndex - 1][xIndex + 1] = Masu.NOT_BLACK;
							}
						}
						if (xIndex != 0) {
							if (masu[yIndex][xIndex - 1] == Masu.BLACK) {
								return false;
							} else {
								masu[yIndex][xIndex - 1] = Masu.NOT_BLACK;
							}
						}
						if (xIndex != getXLength() - 2) {
							if (masu[yIndex][xIndex + 2] == Masu.BLACK) {
								return false;
							} else {
								masu[yIndex][xIndex + 2] = Masu.NOT_BLACK;
							}
						}
						if (yIndex != getYLength() - 1) {
							if (masu[yIndex + 1][xIndex] == Masu.BLACK) {
								return false;
							} else {
								masu[yIndex + 1][xIndex] = Masu.NOT_BLACK;
							}
							if (masu[yIndex + 1][xIndex + 1] == Masu.BLACK) {
								return false;
							} else {
								masu[yIndex + 1][xIndex + 1] = Masu.NOT_BLACK;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 1方向だけ伸ばせる黒マスをのりに、どの方向にも伸ばせない空白マスを白に確定する。
		 * どの方向にも伸ばせない黒マスががあった場合falseを返す。
		 */
		public boolean noriSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					Masu masuPivot = masu[yIndex][xIndex];
					if (masuPivot == Masu.BLACK) {
						Masu masuUp = yIndex == 0 ? Masu.NOT_BLACK : masu[yIndex - 1][xIndex];
						Masu masuRight = xIndex == getXLength() - 1 ? Masu.NOT_BLACK : masu[yIndex][xIndex + 1];
						Masu masuDown = yIndex == getYLength() - 1 ? Masu.NOT_BLACK : masu[yIndex + 1][xIndex];
						Masu masuLeft = xIndex == 0 ? Masu.NOT_BLACK : masu[yIndex][xIndex - 1];
						if (masuUp == Masu.NOT_BLACK && masuRight == Masu.NOT_BLACK && masuDown == Masu.NOT_BLACK
								&& masuLeft == Masu.NOT_BLACK) {
							return false;
						}
						if (masuUp == Masu.SPACE && masuRight == Masu.NOT_BLACK && masuDown == Masu.NOT_BLACK
								&& masuLeft == Masu.NOT_BLACK) {
							masu[yIndex - 1][xIndex] = Masu.BLACK;
						}
						if (masuUp == Masu.NOT_BLACK && masuRight == Masu.SPACE && masuDown == Masu.NOT_BLACK
								&& masuLeft == Masu.NOT_BLACK) {
							masu[yIndex][xIndex + 1] = Masu.BLACK;
						}
						if (masuUp == Masu.NOT_BLACK && masuRight == Masu.NOT_BLACK && masuDown == Masu.SPACE
								&& masuLeft == Masu.NOT_BLACK) {
							masu[yIndex + 1][xIndex] = Masu.BLACK;
						}
						if (masuUp == Masu.NOT_BLACK && masuRight == Masu.NOT_BLACK && masuDown == Masu.NOT_BLACK
								&& masuLeft == Masu.SPACE) {
							masu[yIndex][xIndex - 1] = Masu.BLACK;
						}
					}
					if (masuPivot == Masu.SPACE) {
						Masu masuUp = yIndex == 0 ? Masu.NOT_BLACK : masu[yIndex - 1][xIndex];
						Masu masuRight = xIndex == getXLength() - 1 ? Masu.NOT_BLACK : masu[yIndex][xIndex + 1];
						Masu masuDown = yIndex == getYLength() - 1 ? Masu.NOT_BLACK : masu[yIndex + 1][xIndex];
						Masu masuLeft = xIndex == 0 ? Masu.NOT_BLACK : masu[yIndex][xIndex - 1];
						if (masuUp == Masu.NOT_BLACK && masuRight == Masu.NOT_BLACK && masuDown == Masu.NOT_BLACK
								&& masuLeft == Masu.NOT_BLACK) {
							masu[yIndex][xIndex] = Masu.NOT_BLACK;
						}
					}
				}
			}
			return true;
		}

		/**
		 * 各種チェックを1セット実行
		 */
		private boolean solveAndCheck() {
			String str = getStateDump();
			if (!roomSolve()) {
				return false;
			}
			if (!noriSolve()) {
				return false;
			}
			if (!noriRoundSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
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

	protected final Field field;
	protected int count = 0;

	public NorinoriSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public NorinoriSolver(Field field) {
		this.field = new Field(field);
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
		System.out.println(new NorinoriSolver(height, width, param).solve());
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
			while (field.getStateDump().equals(befStr) && recursiveCnt < 3) {
				if (!candSolve(field, recursiveCnt == 2 ? 999 : recursiveCnt)) {
					return "問題に矛盾がある可能性があります。途中経過を返します。";
				}
				recursiveCnt++;
			}
			if (recursiveCnt == 3 && field.getStateDump().equals(befStr)) {
				return "解けませんでした。途中経過を返します。";
			}
		}
		System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
		System.out.println("難易度:" + (count * 5));
		System.out.println(field);
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count * 5).toString();
	}

	/**
	 * 仮置きして調べる
	 */
	protected boolean candSolve(Field field, int recursive) {
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
		String befStr = virtual.getStateDump();
		boolean allowBlack = virtual.solveAndCheck()
				&& (befStr.equals(virtual.getStateDump()) || virtual.solveAndCheck());
		if (allowBlack && recursive > 0) {
			if (!candSolve(virtual, recursive - 1)) {
				allowBlack = false;
			}
		}
		Field virtual2 = new Field(field);
		virtual2.masu[yIndex][xIndex] = Masu.NOT_BLACK;
		befStr = virtual2.getStateDump();
		boolean allowNotBlack = virtual2.solveAndCheck()
				&& (befStr.equals(virtual2.getStateDump()) || virtual2.solveAndCheck());
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
