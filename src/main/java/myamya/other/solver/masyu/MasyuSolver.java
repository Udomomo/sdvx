package myamya.other.solver.masyu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.GeneratorResult;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Common.Wall;
import myamya.other.solver.Generator;
import myamya.other.solver.Solver;

public class MasyuSolver implements Solver {

	public static class MasyuGenerator implements Generator {

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		static class MasyuSolverForGenerator extends MasyuSolver {
			private final int limit;

			public MasyuSolverForGenerator(Field field, int limit) {
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

		public MasyuGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new MasyuGenerator(10, 10).generate();
		}

		@Override
		public GeneratorResult generate() {
			MasyuSolver.Field wkField = new MasyuSolver.Field(height, width);
			List<Integer> indexList = new ArrayList<>();
			for (int i = 0; i < (height * (width - 1)) + ((height - 1) * width); i++) {
				indexList.add(i);
			}
			Collections.shuffle(indexList);
			int index = 0;
			int level = 0;
			long start = System.nanoTime();
			while (true) {
				// 問題生成部
				while (!wkField.isSolved()) {
					int posBase = indexList.get(index);
					boolean toYokoWall;
					int yIndex, xIndex;
					if (posBase < height * (width - 1)) {
						toYokoWall = true;
						yIndex = posBase / (width - 1);
						xIndex = posBase % (width - 1);
					} else {
						toYokoWall = false;
						posBase = posBase - (height * (width - 1));
						yIndex = posBase / width;
						xIndex = posBase % width;
					}
					if ((toYokoWall && wkField.yokoWall[yIndex][xIndex] == Wall.SPACE)
							|| (!toYokoWall && wkField.tateWall[yIndex][xIndex] == Wall.SPACE)) {
						boolean isOk = false;
						MasyuSolver.Field virtual = new MasyuSolver.Field(wkField, true);
						if (toYokoWall) {
							if ((yIndex != 0 && (wkField.tateWall[yIndex - 1][xIndex] == Wall.NOT_EXISTS ||
									wkField.tateWall[yIndex - 1][xIndex + 1] == Wall.NOT_EXISTS))
									|| (yIndex != wkField.getYLength() - 1
											&& (wkField.tateWall[yIndex][xIndex] == Wall.NOT_EXISTS ||
													wkField.tateWall[yIndex][xIndex + 1] == Wall.NOT_EXISTS))) {
								// カーブができにくくする処理
								virtual.yokoWall[yIndex][xIndex] = Wall.EXISTS;
							} else {
								virtual.yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
							}
						} else {
							if (xIndex != 0 && (virtual.yokoWall[yIndex][xIndex - 1] == Wall.NOT_EXISTS ||
									virtual.yokoWall[yIndex + 1][xIndex - 1] == Wall.NOT_EXISTS)
									|| (xIndex != wkField.getXLength() - 1
											&& (virtual.yokoWall[yIndex][xIndex] == Wall.NOT_EXISTS ||
													virtual.yokoWall[yIndex + 1][xIndex] == Wall.NOT_EXISTS))) {
								// カーブができにくくする処理
								virtual.tateWall[yIndex][xIndex] = Wall.EXISTS;
							} else {
								virtual.tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
							}
						}
						if (virtual.solveAndCheck()) {
							isOk = true;
							wkField.masu = virtual.masu;
							wkField.yokoWall = virtual.yokoWall;
							wkField.tateWall = virtual.tateWall;
						} else {
							virtual = new MasyuSolver.Field(wkField, true);
							if (toYokoWall) {
								virtual.yokoWall[yIndex][xIndex] = Wall.EXISTS;
							} else {
								virtual.tateWall[yIndex][xIndex] = Wall.EXISTS;
							}
							if (virtual.solveAndCheck()) {
								isOk = true;
								wkField.masu = virtual.masu;
								wkField.yokoWall = virtual.yokoWall;
								wkField.tateWall = virtual.tateWall;
							}
						}
						if (!isOk) {
							// 破綻したら0から作り直す。
							wkField = new MasyuSolver.Field(height, width);
							Collections.shuffle(indexList);
							index = 0;
							continue;
						}
					}
					index++;
				}
				// 数字埋め＆マス初期化
				// できるだけ真珠を埋める。
				List<Position> numberPosList = new ArrayList<>();
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						if (wkField.toStraightCheck(yIndex, xIndex)) {
							//白になれるかチェック
							Wall wallUp = yIndex == 0 ? Wall.EXISTS : wkField.tateWall[yIndex - 1][xIndex];
							Wall wallRight = xIndex == wkField.getXLength() - 1 ? Wall.EXISTS
									: wkField.yokoWall[yIndex][xIndex];
							Wall wallDown = yIndex == wkField.getYLength() - 1 ? Wall.EXISTS
									: wkField.tateWall[yIndex][xIndex];
							Wall wallLeft = xIndex == 0 ? Wall.EXISTS : wkField.yokoWall[yIndex][xIndex - 1];
							if (wallUp == Wall.NOT_EXISTS || wallDown == Wall.NOT_EXISTS) {
								if (wkField.toCurveCheck(yIndex - 1, xIndex)) {
									wkField.pearl[yIndex][xIndex] = Pearl.SIRO;
									numberPosList.add(new Position(yIndex, xIndex));
								} else if (wkField.toCurveCheck(yIndex + 1, xIndex)) {
									wkField.pearl[yIndex][xIndex] = Pearl.SIRO;
									numberPosList.add(new Position(yIndex, xIndex));
								}
							} else if (wallRight == Wall.NOT_EXISTS || wallLeft == Wall.NOT_EXISTS) {
								if (wkField.toCurveCheck(yIndex, xIndex + 1)) {
									wkField.pearl[yIndex][xIndex] = Pearl.SIRO;
									numberPosList.add(new Position(yIndex, xIndex));
								} else if (wkField.toCurveCheck(yIndex, xIndex - 1)) {
									wkField.pearl[yIndex][xIndex] = Pearl.SIRO;
									numberPosList.add(new Position(yIndex, xIndex));
								}
							}
						} else if (wkField.toCurveCheck(yIndex, xIndex)) {
							// 黒になれるかチェック
							Wall wallUp = yIndex == 0 ? Wall.EXISTS : wkField.tateWall[yIndex - 1][xIndex];
							Wall wallRight = xIndex == wkField.getXLength() - 1 ? Wall.EXISTS
									: wkField.yokoWall[yIndex][xIndex];
							Wall wallDown = yIndex == wkField.getYLength() - 1 ? Wall.EXISTS
									: wkField.tateWall[yIndex][xIndex];
							Wall wallLeft = xIndex == 0 ? Wall.EXISTS : wkField.yokoWall[yIndex][xIndex - 1];
							boolean isOk = true;
							if (wallUp == Wall.NOT_EXISTS) {
								if (!wkField.toStraightCheck(yIndex - 1, xIndex)) {
									isOk = false;
								}
							}
							if (wallRight == Wall.NOT_EXISTS) {
								if (!wkField.toStraightCheck(yIndex, xIndex + 1)) {
									isOk = false;
								}
							}
							if (wallDown == Wall.NOT_EXISTS) {
								if (!wkField.toStraightCheck(yIndex + 1, xIndex)) {
									isOk = false;
								}
							}
							if (wallLeft == Wall.NOT_EXISTS) {
								if (!wkField.toStraightCheck(yIndex, xIndex - 1)) {
									isOk = false;
								}
							}
							if (isOk) {
								wkField.pearl[yIndex][xIndex] = Pearl.KURO;
								numberPosList.add(new Position(yIndex, xIndex));
							}
						}
					}
				}
				// マスを戻す
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						if (wkField.pearl[yIndex][xIndex] == null) {
							wkField.masu[yIndex][xIndex] = Masu.SPACE;
						}
					}
				}
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength() - 1; xIndex++) {
						wkField.yokoWall[yIndex][xIndex] = Wall.SPACE;
					}
				}
				for (int yIndex = 0; yIndex < wkField.getYLength() - 1; yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						wkField.tateWall[yIndex][xIndex] = Wall.SPACE;
					}
				}
				// 解けるかな？
				level = new MasyuSolverForGenerator(wkField, 100).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new MasyuSolver.Field(height, width);
					Collections.shuffle(indexList);
					index = 0;
				} else {
					// ヒントを限界まで減らす
					Collections.shuffle(numberPosList);
					for (Position numberPos : numberPosList) {
						MasyuSolver.Field virtual = new MasyuSolver.Field(wkField, true);
						virtual.pearl[numberPos.getyIndex()][numberPos.getxIndex()] = null;
						virtual.masu[numberPos.getyIndex()][numberPos.getxIndex()] = Masu.SPACE;
						// TODO もっとでかくしたい
						int solveResult = new MasyuSolverForGenerator(virtual, 4000).solve2();
						if (solveResult != -1) {
							wkField.pearl[numberPos.getyIndex()][numberPos.getxIndex()] = null;
							wkField.masu[numberPos.getyIndex()][numberPos.getxIndex()] = Masu.SPACE;
							level = solveResult;
						}
					}
					break;
				}
			}
			level = (int) Math.sqrt(level * 2 / 3) + 1;
			String status = "Lv:" + level + "の問題を獲得！(白：" + wkField.getHintCount().split("/")[0] + "、黒："
					+ wkField.getHintCount().split("/")[1] + ")";
			String url = wkField.getPuzPreURL();
			String link = "<a href=\"" + url + "\" target=\"_blank\">ぱずぷれv3で解く</a>";
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			int margin = 5;
			sb.append(
					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
							+ "height=\"" + (wkField.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
							+ (wkField.getXLength() * baseSize + 2 * baseSize) + "\" >");
			// 横壁描画
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < wkField.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == wkField.getXLength() - 1;
					sb.append("<line y1=\""
							+ (yIndex * baseSize + margin)
							+ "\" x1=\""
							+ (xIndex * baseSize + 2 * baseSize)
							+ "\" y2=\""
							+ (yIndex * baseSize + baseSize + margin)
							+ "\" x2=\""
							+ (xIndex * baseSize + 2 * baseSize)
							+ "\" stroke-width=\"1\" fill=\"none\"");
					if (oneYokoWall) {
						sb.append("stroke=\"#000\" ");
					} else {
						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
					}
					sb.append(">"
							+ "</line>");
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == wkField.getYLength() - 1;
					sb.append("<line y1=\""
							+ (yIndex * baseSize + baseSize + margin)
							+ "\" x1=\""
							+ (xIndex * baseSize + baseSize)
							+ "\" y2=\""
							+ (yIndex * baseSize + baseSize + margin)
							+ "\" x2=\""
							+ (xIndex * baseSize + baseSize + baseSize)
							+ "\" stroke-width=\"1\" fill=\"none\"");
					if (oneTateWall) {
						sb.append("stroke=\"#000\" ");
					} else {
						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
					}
					sb.append(">"
							+ "</line>");
				}
			}

			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					Pearl onePearl = wkField.getPearl()[yIndex][xIndex];
					if (onePearl == Pearl.SIRO) {
						sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2) + margin)
								+ "\" cx=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2))
								+ "\" r=\""
								+ (baseSize / 2 - 3)
								+ "\" fill=\"white\", stroke=\"black\">"
								+ "</circle>");

					} else if (onePearl == Pearl.KURO) {
						sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2) + margin)
								+ "\" cx=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2))
								+ "\" r=\""
								+ (baseSize / 2 - 3)
								+ "\" fill=\"black\", stroke=\"black\">"
								+ "</circle>");
					}
				}
			}
			sb.append("</svg>");
			System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
			System.out.println(level);
			System.out.println(wkField.getHintCount());
			System.out.println(wkField);
			return new GeneratorResult(status, sb.toString(), link, url, level, "");
		}

	}

	/**
	 * 真珠
	 */
	public enum Pearl {
		SIRO("○", 1), KURO("●", 2);

		String str;
		int val;

		Pearl(String str, int val) {
			this.str = str;
			this.val = val;
		}

		@Override
		public String toString() {
			return str;
		}

		public static Pearl getByVal(int val) {
			for (Pearl one : Pearl.values()) {
				if (one.val == val) {
					return one;
				}
			}
			return null;
		}
	}

	public static class Field {
		static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";

		// マスの情報
		private Masu[][] masu;
		// 真珠の情報
		private Pearl[][] pearl;
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		private Wall[][] yokoWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
		private Wall[][] tateWall;

		public Masu[][] getMasu() {
			return masu;
		}

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			sb.append("http://pzv.jp/p.html?masyu/" + getXLength() + "/" + getYLength() + "/");
			for (int i = 0; i < getYLength() * getXLength(); i++) {
				int yIndex1 = i / getXLength();
				int xIndex1 = i % getXLength();
				i++;
				int yIndex2 = i / getXLength();
				int xIndex2 = i % getXLength();
				i++;
				int yIndex3 = i / getXLength();
				int xIndex3 = i % getXLength();
				int bitInfo = 0;
				if (yIndex1 < getYLength()) {
					if (pearl[yIndex1][xIndex1] == Pearl.SIRO) {
						bitInfo = bitInfo + 9;
					} else if (pearl[yIndex1][xIndex1] == Pearl.KURO) {
						bitInfo = bitInfo + 18;
					}
				}
				if (yIndex2 < getYLength()) {
					if (pearl[yIndex2][xIndex2] == Pearl.SIRO) {
						bitInfo = bitInfo + 3;
					} else if (pearl[yIndex2][xIndex2] == Pearl.KURO) {
						bitInfo = bitInfo + 6;
					}
				}
				if (yIndex3 < getYLength()) {
					if (pearl[yIndex3][xIndex3] == Pearl.SIRO) {
						bitInfo = bitInfo + 1;
					} else if (pearl[yIndex3][xIndex3] == Pearl.KURO) {
						bitInfo = bitInfo + 2;
					}
				}
				sb.append(Integer.toString(bitInfo, 36));
			}
			if (sb.charAt(sb.length() - 1) == '.') {
				sb.append("/");
			}
			return sb.toString();
		}

		public String getHintCount() {
			int siro = 0;
			int kuro = 0;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (pearl[yIndex][xIndex] == Pearl.SIRO) {
						siro++;
					}
					if (pearl[yIndex][xIndex] == Pearl.KURO) {
						kuro++;
					}
				}
			}
			return String.valueOf(siro + "/" + kuro);
		}

		public Pearl[][] getPearl() {
			return pearl;
		}

		public Wall[][] getYokoWall() {
			return yokoWall;
		}

		public Wall[][] getTateWall() {
			return tateWall;
		}

		public int getYLength() {
			return masu.length;
		}

		public int getXLength() {
			return masu[0].length;
		}

		public Field(int height, int width) {
			masu = new Masu[height][width];
			pearl = new Pearl[height][width];
			yokoWall = new Wall[height][width - 1];
			tateWall = new Wall[height - 1][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					yokoWall[yIndex][xIndex] = Wall.SPACE;
				}
			}
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					tateWall[yIndex][xIndex] = Wall.SPACE;
				}
			}
		}

		public Field(int height, int width, String param, boolean ura) {
			masu = new Masu[height][width];
			pearl = new Pearl[height][width];
			yokoWall = new Wall[height][width - 1];
			tateWall = new Wall[height - 1][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					yokoWall[yIndex][xIndex] = Wall.SPACE;
				}
			}
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					tateWall[yIndex][xIndex] = Wall.SPACE;
				}
			}
			int index = 0;
			for (int i = 0; i < param.length(); i++) {
				char ch = param.charAt(i);
				int bitInfo = Character.getNumericValue(ch);
				int pos1 = bitInfo / 9 % 3;
				int pos2 = bitInfo / 3 % 3;
				int pos3 = bitInfo % 3;
				if (index / getXLength() < getYLength()) {
					if (pos1 > 0) {
						masu[index / getXLength()][index % getXLength()] = Masu.NOT_BLACK;
						pearl[index / getXLength()][index % getXLength()] = Pearl
								.getByVal(ura ? pos1 % 2 + 1 : pos1);
					}
				}
				index++;
				if (index / getXLength() < getYLength()) {
					if (pos2 > 0) {
						masu[index / getXLength()][index % getXLength()] = Masu.NOT_BLACK;
						pearl[index / getXLength()][index % getXLength()] = Pearl
								.getByVal(ura ? pos2 % 2 + 1 : pos2);
					}
				}
				index++;
				if (index / getXLength() < getYLength()) {
					if (pos3 > 0) {
						masu[index / getXLength()][index % getXLength()] = Masu.NOT_BLACK;
						pearl[index / getXLength()][index % getXLength()] = Pearl
								.getByVal(ura ? pos3 % 2 + 1 : pos3);
					}
				}
				index++;
			}
		}

		public Field(Field other) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			pearl = other.pearl;
			yokoWall = new Wall[other.getYLength()][other.getXLength() - 1];
			tateWall = new Wall[other.getYLength() - 1][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
				}
			}
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					yokoWall[yIndex][xIndex] = other.yokoWall[yIndex][xIndex];
				}
			}
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					tateWall[yIndex][xIndex] = other.tateWall[yIndex][xIndex];
				}
			}
		}

		public Field(Field other, boolean flag) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			pearl = new Pearl[other.getYLength()][other.getXLength()];
			yokoWall = new Wall[other.getYLength()][other.getXLength() - 1];
			tateWall = new Wall[other.getYLength() - 1][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
					pearl[yIndex][xIndex] = other.pearl[yIndex][xIndex];
				}
			}
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					yokoWall[yIndex][xIndex] = other.yokoWall[yIndex][xIndex];
				}
			}
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					tateWall[yIndex][xIndex] = other.tateWall[yIndex][xIndex];
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
					if (pearl[yIndex][xIndex] != null) {
						sb.append(pearl[yIndex][xIndex]);
					} else {
						sb.append(masu[yIndex][xIndex]);
					}
					if (xIndex != getXLength() - 1) {
						sb.append(yokoWall[yIndex][xIndex]);
					}
				}
				sb.append("□");
				sb.append(System.lineSeparator());
				if (yIndex != getYLength() - 1) {
					sb.append("□");
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						sb.append(tateWall[yIndex][xIndex]);
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

		public String getStateDump() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					sb.append(masu[yIndex][xIndex]);
				}
			}
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					sb.append(yokoWall[yIndex][xIndex]);
				}
			}
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					sb.append(tateWall[yIndex][xIndex]);
				}
			}
			return sb.toString();
		}

		/**
		 * 白真珠は直進のちカーブ、黒真珠はカーブのち直進。
		 * 条件を満たさない場合falseを返す。
		 */
		private boolean pearlSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (pearl[yIndex][xIndex] != null) {
						if (pearl[yIndex][xIndex] == Pearl.SIRO) {
							if (!toStraightCheck(yIndex, xIndex)) {
								return false;
							}
							toStraight(yIndex, xIndex);
							Wall wallUp = yIndex == 0 ? Wall.EXISTS : tateWall[yIndex - 1][xIndex];
							Wall wallRight = xIndex == getXLength() - 1 ? Wall.EXISTS : yokoWall[yIndex][xIndex];
							Wall wallDown = yIndex == getYLength() - 1 ? Wall.EXISTS : tateWall[yIndex][xIndex];
							Wall wallLeft = xIndex == 0 ? Wall.EXISTS : yokoWall[yIndex][xIndex - 1];
							if (wallUp == Wall.NOT_EXISTS || wallDown == Wall.NOT_EXISTS) {
								boolean canUpCurve = toCurveCheck(yIndex - 1, xIndex);
								boolean canDownCurve = toCurveCheck(yIndex + 1, xIndex);
								if (!canUpCurve && !canDownCurve) {
									return false;
								}
								if (!canUpCurve) {
									toCurve(yIndex + 1, xIndex);
								}
								if (!canDownCurve) {
									toCurve(yIndex - 1, xIndex);
								}
							}
							if (wallRight == Wall.NOT_EXISTS || wallLeft == Wall.NOT_EXISTS) {
								boolean canRightCurve = toCurveCheck(yIndex, xIndex + 1);
								boolean canLeftCurve = toCurveCheck(yIndex, xIndex - 1);
								if (!canRightCurve && !canLeftCurve) {
									return false;
								}
								if (!canRightCurve) {
									toCurve(yIndex, xIndex - 1);
								}
								if (!canLeftCurve) {
									toCurve(yIndex, xIndex + 1);
								}
							}
						} else if (pearl[yIndex][xIndex] == Pearl.KURO) {
							if (!toCurveCheck(yIndex, xIndex)) {
								return false;
							}
							toCurve(yIndex, xIndex);
							Wall wallUp = yIndex == 0 ? Wall.EXISTS : tateWall[yIndex - 1][xIndex];
							Wall wallRight = xIndex == getXLength() - 1 ? Wall.EXISTS : yokoWall[yIndex][xIndex];
							Wall wallDown = yIndex == getYLength() - 1 ? Wall.EXISTS : tateWall[yIndex][xIndex];
							Wall wallLeft = xIndex == 0 ? Wall.EXISTS : yokoWall[yIndex][xIndex - 1];
							if (wallUp == Wall.NOT_EXISTS) {
								if (!toStraightCheck(yIndex - 1, xIndex)) {
									return false;
								}
								toStraight(yIndex - 1, xIndex);
							}
							if (wallRight == Wall.NOT_EXISTS) {
								if (!toStraightCheck(yIndex, xIndex + 1)) {
									return false;
								}
								toStraight(yIndex, xIndex + 1);
							}
							if (wallDown == Wall.NOT_EXISTS) {
								if (!toStraightCheck(yIndex + 1, xIndex)) {
									return false;
								}
								toStraight(yIndex + 1, xIndex);
							}
							if (wallLeft == Wall.NOT_EXISTS) {
								if (!toStraightCheck(yIndex, xIndex - 1)) {
									return false;
								}
								toStraight(yIndex, xIndex - 1);
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 指定した位置のマスが直進可能かチェックする。できない場合falseを返す。
		 */
		private boolean toStraightCheck(int yIndex, int xIndex) {
			if (pearl[yIndex][xIndex] == Pearl.KURO) {
				return false;
			}
			Wall wallUp = yIndex == 0 ? Wall.EXISTS : tateWall[yIndex - 1][xIndex];
			Wall wallRight = xIndex == getXLength() - 1 ? Wall.EXISTS : yokoWall[yIndex][xIndex];
			Wall wallDown = yIndex == getYLength() - 1 ? Wall.EXISTS : tateWall[yIndex][xIndex];
			Wall wallLeft = xIndex == 0 ? Wall.EXISTS : yokoWall[yIndex][xIndex - 1];
			if ((wallUp == Wall.EXISTS && wallRight == Wall.EXISTS)
					|| (wallUp == Wall.EXISTS && wallLeft == Wall.EXISTS)
					|| (wallRight == Wall.EXISTS && wallDown == Wall.EXISTS)
					|| (wallDown == Wall.EXISTS && wallLeft == Wall.EXISTS)
					|| (wallUp == Wall.NOT_EXISTS && wallRight == Wall.NOT_EXISTS)
					|| (wallUp == Wall.NOT_EXISTS && wallLeft == Wall.NOT_EXISTS)
					|| (wallRight == Wall.NOT_EXISTS && wallDown == Wall.NOT_EXISTS)
					|| (wallDown == Wall.NOT_EXISTS && wallLeft == Wall.NOT_EXISTS)
					|| (wallUp == Wall.EXISTS && wallDown == Wall.NOT_EXISTS)
					|| (wallDown == Wall.EXISTS && wallUp == Wall.NOT_EXISTS)
					|| (wallRight == Wall.EXISTS && wallLeft == Wall.NOT_EXISTS)
					|| (wallLeft == Wall.EXISTS && wallRight == Wall.NOT_EXISTS)) {
				return false;
			}
			return true;
		}

		/**
		 * 指定した位置のマスを直進させる。必ずチェック処理後に呼ぶこと。
		 */
		private void toStraight(int yIndex, int xIndex) {
			Wall wallUp = yIndex == 0 ? Wall.EXISTS : tateWall[yIndex - 1][xIndex];
			Wall wallRight = xIndex == getXLength() - 1 ? Wall.EXISTS : yokoWall[yIndex][xIndex];
			Wall wallDown = yIndex == getYLength() - 1 ? Wall.EXISTS : tateWall[yIndex][xIndex];
			Wall wallLeft = xIndex == 0 ? Wall.EXISTS : yokoWall[yIndex][xIndex - 1];
			if (wallUp == Wall.EXISTS || wallDown == Wall.EXISTS ||
					wallRight == Wall.NOT_EXISTS || wallLeft == Wall.NOT_EXISTS) {
				if (wallUp == Wall.SPACE) {
					tateWall[yIndex - 1][xIndex] = Wall.EXISTS;
				}
				if (wallRight == Wall.SPACE) {
					yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
				}
				if (wallDown == Wall.SPACE) {
					tateWall[yIndex][xIndex] = Wall.EXISTS;
				}
				if (wallLeft == Wall.SPACE) {
					yokoWall[yIndex][xIndex - 1] = Wall.NOT_EXISTS;
				}
			}
			if (wallRight == Wall.EXISTS || wallLeft == Wall.EXISTS ||
					wallUp == Wall.NOT_EXISTS || wallDown == Wall.NOT_EXISTS) {
				if (wallUp == Wall.SPACE) {
					tateWall[yIndex - 1][xIndex] = Wall.NOT_EXISTS;
				}
				if (wallRight == Wall.SPACE) {
					yokoWall[yIndex][xIndex] = Wall.EXISTS;
				}
				if (wallDown == Wall.SPACE) {
					tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
				}
				if (wallLeft == Wall.SPACE) {
					yokoWall[yIndex][xIndex - 1] = Wall.EXISTS;
				}
			}
		}

		/**
		 * 指定した位置のマスがカーブ可能かチェックする。できない場合falseを返す。
		 */
		private boolean toCurveCheck(int yIndex, int xIndex) {
			if (pearl[yIndex][xIndex] == Pearl.SIRO) {
				return false;
			}
			Wall wallUp = yIndex == 0 ? Wall.EXISTS : tateWall[yIndex - 1][xIndex];
			Wall wallRight = xIndex == getXLength() - 1 ? Wall.EXISTS : yokoWall[yIndex][xIndex];
			Wall wallDown = yIndex == getYLength() - 1 ? Wall.EXISTS : tateWall[yIndex][xIndex];
			Wall wallLeft = xIndex == 0 ? Wall.EXISTS : yokoWall[yIndex][xIndex - 1];
			if ((wallUp == Wall.EXISTS && wallDown == Wall.EXISTS)
					|| (wallRight == Wall.EXISTS && wallLeft == Wall.EXISTS)
					|| (wallUp == Wall.NOT_EXISTS && wallDown == Wall.NOT_EXISTS)
					|| (wallRight == Wall.NOT_EXISTS && wallLeft == Wall.NOT_EXISTS)) {
				return false;
			}
			return true;
		}

		/**
		 * 指定した位置のマスをカーブさせる。必ずチェック処理後に呼ぶこと。
		 */
		private void toCurve(int yIndex, int xIndex) {
			Wall wallUp = yIndex == 0 ? Wall.EXISTS : tateWall[yIndex - 1][xIndex];
			Wall wallRight = xIndex == getXLength() - 1 ? Wall.EXISTS : yokoWall[yIndex][xIndex];
			Wall wallDown = yIndex == getYLength() - 1 ? Wall.EXISTS : tateWall[yIndex][xIndex];
			Wall wallLeft = xIndex == 0 ? Wall.EXISTS : yokoWall[yIndex][xIndex - 1];
			if (wallUp == Wall.EXISTS) {
				if (wallDown == Wall.SPACE) {
					tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
				}
			}
			if (wallRight == Wall.EXISTS) {
				if (wallLeft == Wall.SPACE) {
					yokoWall[yIndex][xIndex - 1] = Wall.NOT_EXISTS;
				}
			}
			if (wallDown == Wall.EXISTS) {
				if (wallUp == Wall.SPACE) {
					tateWall[yIndex - 1][xIndex] = Wall.NOT_EXISTS;
				}
			}
			if (wallLeft == Wall.EXISTS) {
				if (wallRight == Wall.SPACE) {
					yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
				}
			}
			if (wallUp == Wall.NOT_EXISTS) {
				if (wallDown == Wall.SPACE) {
					tateWall[yIndex][xIndex] = Wall.EXISTS;
				}
			}
			if (wallRight == Wall.NOT_EXISTS) {
				if (wallLeft == Wall.SPACE) {
					yokoWall[yIndex][xIndex - 1] = Wall.EXISTS;
				}
			}
			if (wallDown == Wall.NOT_EXISTS) {
				if (wallUp == Wall.SPACE) {
					tateWall[yIndex - 1][xIndex] = Wall.EXISTS;
				}
			}
			if (wallLeft == Wall.NOT_EXISTS) {
				if (wallRight == Wall.SPACE) {
					yokoWall[yIndex][xIndex] = Wall.EXISTS;
				}
			}
		}

		/**
		 * 黒マスの周囲の壁を埋め、隣接セルを白マスにする
		 * また、白マス隣接セルの周辺の壁の数が2にならない場合もfalseを返す。
		 */
		public boolean nextSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					int existsCount = 0;
					int notExistsCount = 0;
					Wall wallUp = yIndex == 0 ? Wall.EXISTS : tateWall[yIndex - 1][xIndex];
					if (wallUp == Wall.EXISTS) {
						existsCount++;
					} else if (wallUp == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
					Wall wallRight = xIndex == getXLength() - 1 ? Wall.EXISTS : yokoWall[yIndex][xIndex];
					if (wallRight == Wall.EXISTS) {
						existsCount++;
					} else if (wallRight == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
					Wall wallDown = yIndex == getYLength() - 1 ? Wall.EXISTS : tateWall[yIndex][xIndex];
					if (wallDown == Wall.EXISTS) {
						existsCount++;
					} else if (wallDown == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
					Wall wallLeft = xIndex == 0 ? Wall.EXISTS : yokoWall[yIndex][xIndex - 1];
					if (wallLeft == Wall.EXISTS) {
						existsCount++;
					} else if (wallLeft == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
					if (masu[yIndex][xIndex] == Masu.SPACE) {
						// 自分が不確定マスなら壁は2マスか4マス
						if ((existsCount == 3 && notExistsCount == 1)
								|| notExistsCount > 2) {
							return false;
						}
						if (existsCount > 2) {
							masu[yIndex][xIndex] = Masu.BLACK;
						} else if (notExistsCount != 0) {
							masu[yIndex][xIndex] = Masu.NOT_BLACK;
						}
					}
					if (masu[yIndex][xIndex] == Masu.BLACK) {
						if (notExistsCount > 0) {
							return false;
						}
						// 周囲の壁を閉鎖
						if (wallUp == Wall.SPACE) {
							tateWall[yIndex - 1][xIndex] = Wall.EXISTS;
						}
						if (wallRight == Wall.SPACE) {
							yokoWall[yIndex][xIndex] = Wall.EXISTS;
						}
						if (wallDown == Wall.SPACE) {
							tateWall[yIndex][xIndex] = Wall.EXISTS;
						}
						if (wallLeft == Wall.SPACE) {
							yokoWall[yIndex][xIndex - 1] = Wall.EXISTS;
						}
					} else if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
						// 自分が白マスなら壁は必ず2マス
						if (existsCount > 2 || notExistsCount > 2) {
							return false;
						}
						if (notExistsCount == 2) {
							if (wallUp == Wall.SPACE) {
								tateWall[yIndex - 1][xIndex] = Wall.EXISTS;
							}
							if (wallRight == Wall.SPACE) {
								yokoWall[yIndex][xIndex] = Wall.EXISTS;
							}
							if (wallDown == Wall.SPACE) {
								tateWall[yIndex][xIndex] = Wall.EXISTS;
							}
							if (wallLeft == Wall.SPACE) {
								yokoWall[yIndex][xIndex - 1] = Wall.EXISTS;
							}
						} else if (existsCount == 2) {
							if (wallUp == Wall.SPACE) {
								tateWall[yIndex - 1][xIndex] = Wall.NOT_EXISTS;
							}
							if (wallRight == Wall.SPACE) {
								yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
							}
							if (wallDown == Wall.SPACE) {
								tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
							}
							if (wallLeft == Wall.SPACE) {
								yokoWall[yIndex][xIndex - 1] = Wall.NOT_EXISTS;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 白マスが1つながりになっていない場合falseを返す。
		 */
		public boolean connectSolve() {
			Set<Position> whitePosSet = new HashSet<>();
			Set<Position> blackCandPosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					Position pos = new Position(yIndex, xIndex);
					if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
						if (whitePosSet.size() == 0) {
							whitePosSet.add(pos);
							setContinuePosSet(pos, whitePosSet, null);
						} else {
							if (!whitePosSet.contains(pos)) {
								return false;
							}
						}
					} else if (masu[yIndex][xIndex] == Masu.SPACE) {
						blackCandPosSet.add(pos);
					}
				}
			}
			blackCandPosSet.removeAll(whitePosSet);
			for (Position pos : blackCandPosSet) {
				masu[pos.getyIndex()][pos.getxIndex()] = Masu.BLACK;
			}
			return true;
		}

		/**
		 * posを起点に上下左右に壁で区切られていないマスを無制限につなげていく。
		 */
		private void setContinuePosSet(Position pos, Set<Position> continuePosSet, Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (tateWall[pos.getyIndex() - 1][pos.getxIndex()] != Wall.EXISTS
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.DOWN);
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (yokoWall[pos.getyIndex()][pos.getxIndex()] != Wall.EXISTS
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.LEFT);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (tateWall[pos.getyIndex()][pos.getxIndex()] != Wall.EXISTS
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.UP);
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (yokoWall[pos.getyIndex()][pos.getxIndex() - 1] != Wall.EXISTS
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.RIGHT);
				}
			}
		}

		/**
		 * 各種チェックを1セット実行
		 * @param recursive
		 * @param recursive
		 */
		public boolean solveAndCheck() {
			String str = getStateDump();
			if (!pearlSolve()) {
				return false;
			}
			if (!nextSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			} else {
				if (!oddSolve()) {
					return false;
				}
				if (!connectSolve()) {
					return false;
				}
				if (!finalSolve()) {
					return false;
				}
				//				if (!paritySolve()) {
				//					return false;
				//				}
			}
			return true;
		}

		/**
		 * ましゅのルール上、各列をふさぐ壁は必ず偶数になる。
		 * 偶数になっていない場合falseを返す。
		 */
		private boolean oddSolve() {
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				int notExistsCount = 0;
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (tateWall[yIndex][xIndex] == Wall.SPACE) {
						notExistsCount = 0;
						break;
					} else if (tateWall[yIndex][xIndex] == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
				}
				if (notExistsCount % 2 != 0) {
					return false;
				}
			}
			for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
				int notExistsCount = 0;
				for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
					if (yokoWall[yIndex][xIndex] == Wall.SPACE) {
						notExistsCount = 0;
						break;
					} else if (yokoWall[yIndex][xIndex] == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
				}
				if (notExistsCount % 2 != 0) {
					return false;
				}
			}
			return true;
		}

		/**
		 * 盤面を市松模様とみなした場合、奇属性と偶属性の白マスの数は同じになる。
		 */
		private boolean paritySolve() {
			int evenWhite = 0;
			int oddWhite = 0;
			int evenSpace = 0;
			int oddSpace = 0;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
						if ((yIndex + xIndex) % 2 == 0) {
							evenWhite++;
						} else {
							oddWhite++;
						}
					} else if (masu[yIndex][xIndex] == Masu.SPACE) {
						if ((yIndex + xIndex) % 2 == 0) {
							evenSpace++;
						} else {
							oddSpace++;
						}
					}
				}
			}
			if (evenWhite + evenSpace < oddWhite) {
				return false;
			}
			if (oddWhite + oddSpace < evenWhite) {
				return false;
			}
			if (evenWhite + evenSpace == oddWhite) {
				for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						if (masu[yIndex][xIndex] == Masu.SPACE) {
							if ((yIndex + xIndex) % 2 == 0) {
								masu[yIndex][xIndex] = Masu.NOT_BLACK;
							}
						}
					}
				}
			}
			if (oddSpace == 0 && evenWhite == oddWhite) {
				for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						if (masu[yIndex][xIndex] == Masu.SPACE) {
							if ((yIndex + xIndex) % 2 == 0) {
								masu[yIndex][xIndex] = Masu.BLACK;
							}
						}
					}
				}
			}
			if (oddWhite + oddSpace == evenWhite) {
				for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						if (masu[yIndex][xIndex] == Masu.SPACE) {
							if ((yIndex + xIndex) % 2 != 0) {
								masu[yIndex][xIndex] = Masu.NOT_BLACK;
							}
						}
					}
				}
			}
			if (evenSpace == 0 && evenWhite == oddWhite) {
				for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						if (masu[yIndex][xIndex] == Masu.SPACE) {
							if ((yIndex + xIndex) % 2 != 0) {
								masu[yIndex][xIndex] = Masu.BLACK;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * フィールドに1つは白マスが必要。
		 */
		private boolean finalSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] != Masu.BLACK) {
						return true;
					}
				}
			}
			return false;
		}

		public boolean isSolved() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.SPACE) {
						return false;
					}
				}
			}
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					if (yokoWall[yIndex][xIndex] == Wall.SPACE) {
						return false;
					}
				}
			}
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (tateWall[yIndex][xIndex] == Wall.SPACE) {
						return false;
					}
				}
			}
			return solveAndCheck();
		}

	}

	protected final Field field;
	protected int count = 0;

	public MasyuSolver(int height, int width, String param, boolean ura) {
		field = new Field(height, width, param, ura);
	}

	public MasyuSolver(Field field) {
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
		System.out.println(new MasyuSolver(height, width, param, false).solve());
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
		System.out.println("難易度:" + (count * 2));
		System.out.println(field);
		int level = (int) Math.sqrt(count * 2 / 3) + 1;
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count * 2).toString() + "(Lv:" + level + ")";
	}

	/**
	 * 仮置きして調べる
	 * @param posSet
	 */
	protected boolean candSolve(Field field, int recursive) {
		String str = field.getStateDump();
		for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength() - 1; xIndex++) {
				if (field.yokoWall[yIndex][xIndex] == Wall.SPACE) {
					Masu masuLeft = field.masu[yIndex][xIndex];
					Masu masuRight = field.masu[yIndex][xIndex + 1];
					if (masuLeft == Masu.SPACE && masuRight == Masu.SPACE) {
						continue;
					}
					count++;
					if (!oneCandYokoWallSolve(field, yIndex, xIndex, recursive)) {
						return false;
					}
				}
			}
		}
		for (int yIndex = 0; yIndex < field.getYLength() - 1; yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				if (field.tateWall[yIndex][xIndex] == Wall.SPACE) {
					Masu masuUp = field.masu[yIndex][xIndex];
					Masu masuDown = field.masu[yIndex + 1][xIndex];
					if (masuUp == Masu.SPACE && masuDown == Masu.SPACE) {
						continue;
					}
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
		virtual.yokoWall[yIndex][xIndex] = Wall.EXISTS;
		boolean allowBlack = virtual.solveAndCheck();
		if (allowBlack && recursive > 0) {
			if (!candSolve(virtual, recursive - 1)) {
				allowBlack = false;
			}
		}
		Field virtual2 = new Field(field);
		virtual2.yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
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
			field.tateWall = virtual2.tateWall;
			field.yokoWall = virtual2.yokoWall;
		} else if (!allowNotBlack) {
			field.masu = virtual.masu;
			field.tateWall = virtual.tateWall;
			field.yokoWall = virtual.yokoWall;
		}
		return true;
	}

	private boolean oneCandTateWallSolve(Field field, int yIndex, int xIndex, int recursive) {
		Field virtual = new Field(field);
		virtual.tateWall[yIndex][xIndex] = Wall.EXISTS;
		boolean allowBlack = virtual.solveAndCheck();
		if (allowBlack && recursive > 0) {
			if (!candSolve(virtual, recursive - 1)) {
				allowBlack = false;
			}
		}
		Field virtual2 = new Field(field);
		virtual2.tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
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
			field.tateWall = virtual2.tateWall;
			field.yokoWall = virtual2.yokoWall;
		} else if (!allowNotBlack) {
			field.masu = virtual.masu;
			field.tateWall = virtual.tateWall;
			field.yokoWall = virtual.yokoWall;
		}
		return true;
	}
}