package myamya.other.solver.stostone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Solver;

public class StostoneSolver implements Solver {

	public static class Field {
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		// マスの情報
		private Masu[][] masu;
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		private final boolean[][] yokoWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
		private final boolean[][] tateWall;
		// 同一グループに属するマスの情報
		private final List<Room> rooms;

		public Masu[][] getMasu() {
			return masu;
		}

		public boolean[][] getYokoWall() {
			return yokoWall;
		}

		public boolean[][] getTateWall() {
			return tateWall;
		}

		public List<Room> getRooms() {
			return rooms;
		}

		public int getYLength() {
			return masu.length;
		}

		public int getHalfYLength() {
			return getYLength() / 2;
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
			yokoWall = new boolean[height][width - 1];
			tateWall = new boolean[height - 1][width];
			int readPos = 0;
			int bit = 0;
			for (int cnt = 0; cnt < getYLength() * (getXLength()
					- 1); cnt++) {
				int mod = cnt % 5;
				if (mod == 0) {
					bit = Character.getNumericValue(param.charAt(readPos));
					readPos++;
				}
				if (mod == 4 || cnt == (getYLength() *
						(getXLength() - 1)) - 1) {
					if (mod >= 0) {
						yokoWall[(cnt - mod + 0) /
								(getXLength() - 1)][(cnt - mod + 0) % (getXLength() - 1)] = bit / 16
										% 2 == 1;
					}
					if (mod >= 1) {
						yokoWall[(cnt - mod + 1) /
								(getXLength() - 1)][(cnt - mod + 1) % (getXLength() - 1)] = bit / 8
										% 2 == 1;
					}
					if (mod >= 2) {
						yokoWall[(cnt - mod + 2) /
								(getXLength() - 1)][(cnt - mod + 2) % (getXLength() - 1)] = bit / 4
										% 2 == 1;
					}
					if (mod >= 3) {
						yokoWall[(cnt - mod + 3) /
								(getXLength() - 1)][(cnt - mod + 3) % (getXLength() - 1)] = bit / 2
										% 2 == 1;
					}
					if (mod >= 4) {
						yokoWall[(cnt - mod + 4) /
								(getXLength() - 1)][(cnt - mod + 4) % (getXLength() - 1)] = bit / 1
										% 2 == 1;
					}
				}
			}
			for (int cnt = 0; cnt < (getYLength() - 1) *
					getXLength(); cnt++) {
				int mod = cnt % 5;
				if (mod == 0) {
					bit = Character.getNumericValue(param.charAt(readPos));
					readPos++;
				}
				if (mod == 4 || cnt == ((getYLength() - 1) *
						getXLength()) - 1) {
					if (mod >= 0) {
						tateWall[(cnt - mod + 0) /
								getXLength()][(cnt - mod + 0) % getXLength()] = bit / 16 % 2 == 1;
					}
					if (mod >= 1) {
						tateWall[(cnt - mod + 1) /
								getXLength()][(cnt - mod + 1) % getXLength()] = bit / 8 % 2 == 1;
					}
					if (mod >= 2) {
						tateWall[(cnt - mod + 2) /
								getXLength()][(cnt - mod + 2) % getXLength()] = bit / 4 % 2 == 1;
					}
					if (mod >= 3) {
						tateWall[(cnt - mod + 3) /
								getXLength()][(cnt - mod + 3) % getXLength()] = bit / 2 % 2 == 1;
					}
					if (mod >= 4) {
						tateWall[(cnt - mod + 4) /
								getXLength()][(cnt - mod + 4) % getXLength()] = bit / 1 % 2 == 1;
					}
				}
			}
			// 縦と横の壁の関係からにょろっと部屋を決めていく
			List<Integer> blackCntList = new ArrayList<>();
			for (; readPos < param.length(); readPos++) {
				char ch = param.charAt(readPos);
				int interval = ALPHABET_FROM_G.indexOf(ch);
				if (interval != -1) {
					for (int i = 0; i < interval + 1; i++) {
						// 数字がない部屋の場合は、部屋の数字は-1として扱う。
						blackCntList.add(-1);
					}
				} else {
					//16 - 255は '-'
					//256 - 999は '+'
					int blackCnt;
					if (ch == '-') {
						blackCnt = Integer.parseInt("" + param.charAt(readPos + 1) + param.charAt(readPos + 2),
								16);
						readPos++;
						readPos++;
					} else if (ch == '+') {
						blackCnt = Integer.parseInt(
								"" +
										param.charAt(readPos + 1) + param.charAt(readPos + 2) + param.charAt(readPos
												+ 3),
								16);
						readPos++;
						readPos++;
						readPos++;
					} else {
						blackCnt = Integer.parseInt(String.valueOf(ch), 16);
					}
					blackCntList.add(blackCnt);
				}
			}
			rooms = new ArrayList<>();
			int blackCntListIndex = 0;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					Position pos = new Position(yIndex, xIndex);
					boolean alreadyRoomed = false;
					for (Room room : rooms) {
						if (room.getMember().contains(pos)) {
							alreadyRoomed = true;
							break;
						}
					}
					if (!alreadyRoomed) {
						Set<Position> continuePosSet = new HashSet<>();
						continuePosSet.add(pos);
						setContinuePosSet(pos, continuePosSet, null);
						rooms.add(new Room(blackCntList.get(blackCntListIndex), continuePosSet));
						blackCntListIndex++;
					}
				}
			}
			System.out.println();
		}

		public Field(Field other) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
				}
			}
			// 壁・部屋は参照渡しで使い回し(一度Fieldができたら変化しないはずなので。)
			yokoWall = other.yokoWall;
			tateWall = other.tateWall;
			rooms = other.rooms;
		}

		// posを起点に上下左右に壁または白確定でないマスを無制限につなげていく。
		private void setContinuePosSet(Position pos, Set<Position> continuePosSet, Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && !tateWall[pos.getyIndex() - 1][pos.getxIndex()]
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.DOWN);
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos) && !yokoWall[pos.getyIndex()][pos.getxIndex()]
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.LEFT);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && !tateWall[pos.getyIndex()][pos.getxIndex()]
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.UP);
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos) && !yokoWall[pos.getyIndex()][pos.getxIndex() - 1]
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.RIGHT);
				}
			}
		}

		/**
		 * 石を落とした後のマスを返す。
		 */
		public Masu[][] drop() {
			Masu[][] result = new Masu[getYLength()][getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					result[yIndex][xIndex] = Masu.SPACE;
				}
			}
			Map<Position, Integer> positionMap = new HashMap<>();
			makePositionMap(new HashSet<>(), positionMap, 0);
			for (Entry<Position, Integer> e : positionMap.entrySet()) {
				if (e.getValue() != -1) {
					Position pos = e.getKey();
					result[pos.getyIndex() +
							e.getValue()][pos.getxIndex()] = masu[pos.getyIndex()][pos.getxIndex()];
				}
			}
			return result;
		}

		/**
		 * マスごとの落下距離マップを作成
		 */
		private boolean makePositionMap(Set<Position> droppedBlackPos, Map<Position, Integer> positionMap,
				int dropDistance) {
			if (positionMap.size() == getYLength() * getXLength()) {
				return true;
			}
			boolean distanceUp = true;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					Position pos = new Position(yIndex, xIndex);
					if (positionMap.containsKey(pos)) {
						// 既に確定済みのマスは除外
						continue;
					}
					if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK) {
						boolean isDropped = false;
						if (pos.getyIndex() +
								dropDistance == getYLength() - 1) {
							//その黒は接地している
							isDropped = true;
						} else if (droppedBlackPos
								.contains(new Position(pos.getyIndex() + 1 + dropDistance, pos.getxIndex()))) {
							// 別の部屋の黒マスと密着している
							isDropped = true;
						}
						if (isDropped) {
							// 黒マス落下距離確定
							Set<Position> blackSet = new HashSet<>();
							blackSet.add(pos);
							setBlackGroupPosSet(pos, blackSet);
							for (Position blackPos : blackSet) {
								positionMap.put(blackPos, dropDistance);
								droppedBlackPos
										.add(new Position(blackPos.getyIndex() + dropDistance, blackPos.getxIndex()));
							}
							distanceUp = false;
						}
					} else {
						positionMap.put(pos, -1);
					}
				}
			}
			return makePositionMap(droppedBlackPos, positionMap,
					distanceUp ? dropDistance + 1 : dropDistance);
		}

		/**
		 * 同じ部屋で隣接する黒マスの位置セットを返す
		 */
		private void setBlackGroupPosSet(Position pos, Set<Position> blackSet) {
			if (pos.getyIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!blackSet.contains(nextPos) &&
						!tateWall[pos.getyIndex() - 1][pos.getxIndex()]
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK) {
					blackSet.add(nextPos);
					setBlackGroupPosSet(nextPos, blackSet);
				}
			}
			if (pos.getxIndex() != getXLength() - 1) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!blackSet.contains(nextPos) &&
						!yokoWall[pos.getyIndex()][pos.getxIndex()]
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK) {
					blackSet.add(nextPos);
					setBlackGroupPosSet(nextPos, blackSet);
				}
			}
			if (pos.getyIndex() != getYLength() - 1) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!blackSet.contains(nextPos) &&
						!tateWall[pos.getyIndex()][pos.getxIndex()]
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK) {
					blackSet.add(nextPos);
					setBlackGroupPosSet(nextPos, blackSet);
				}
			}
			if (pos.getxIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!blackSet.contains(nextPos) &&
						!yokoWall[pos.getyIndex()][pos.getxIndex() - 1]
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK) {
					blackSet.add(nextPos);
					setBlackGroupPosSet(nextPos, blackSet);
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
						sb.append(yokoWall[yIndex][xIndex] == true ? "□" : "　");
					}
				}
				sb.append("□");
				sb.append(System.lineSeparator());
				if (yIndex != getYLength() - 1) {
					sb.append("□");
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {

						sb.append(tateWall[yIndex][xIndex] == true ? "□" : "　");
						if (xIndex != getXLength() -
								1) {
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
			return sb.toString();
		}

		/**
		 * 部屋のマスを埋める。
		 * 部屋に対して黒マスのルールに違反している場合はfalseを返す。
		 */
		public boolean roomSolve() {
			for (Room room : rooms) {
				// 部屋に対する調査
				int blackCnt = 0;
				int spaceCnt = 0;
				for (Position pos : room.getMember()) {
					if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK) {
						blackCnt++;
					} else if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
						spaceCnt++;
					}
				}
				if (room.getBlackCnt() == -1) {
					if (blackCnt + spaceCnt < 1) {
						// 黒マス不足
						return false;
					}
					if (blackCnt == 0 && spaceCnt == 1) {
						// 数字がない部屋でまだ黒マスが置かれておらず、未確定マスが1マスであればそれは黒マス
						for (Position pos : room.getMember()) {
							if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
								masu[pos.getyIndex()][pos.getxIndex()] = Masu.BLACK;
							}
						}
					}
				} else {
					if (blackCnt + spaceCnt < room.getBlackCnt()) {
						// 黒マス不足
						return false;
					}
					// 置かねばならない黒マスの数
					int retainBlackCnt = room.getBlackCnt() - blackCnt;
					if (retainBlackCnt < 0) {
						// 黒マス超過
						return false;
					} else if (retainBlackCnt == 0) {
						// 黒マス数が既に部屋の黒マス数に等しければ、部屋の他のマスは白マス
						for (Position pos : room.getMember()) {
							if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
								masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
							}
						}
					} else if (spaceCnt == retainBlackCnt) {
						// 未確定マスが置かねばならない黒マスの数に等しければ、未確定マスは黒マス
						for (Position pos : room.getMember()) {
							if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
								masu[pos.getyIndex()][pos.getxIndex()] = Masu.BLACK;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 縦方向のマスを埋める。
		 * 縦方向に対して黒マス・白マスの個数が超過している場合はfalseを返す。
		 */
		public boolean varticalSolve() {
			for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
				// 縦方向に対する調査
				int blackCnt = 0;
				int notBlackCnt = 0;
				Set<Room> roomSet = new HashSet<>();
				boolean checkRoomSet = false;
				for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
					if (yIndex != 0 && tateWall[yIndex - 1][xIndex]) {
						checkRoomSet = true;
					}
					if (masu[yIndex][xIndex] == Masu.BLACK) {
						blackCnt++;
						for (Room room : rooms) {
							if (room.getMember().contains(new Position(yIndex, xIndex))) {
								if (checkRoomSet) {
									if (roomSet.contains(room)) {
										return false;
									}
								}
								roomSet.add(room);
								checkRoomSet = false;
								break;
							}
						}
					} else if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
						notBlackCnt++;
						checkRoomSet = true;
					}
				}
				if (blackCnt > getHalfYLength() ||
						notBlackCnt > getHalfYLength()) {
					return false;
				} else if (blackCnt == getHalfYLength()) {
					// 黒マス数が盤面の半分に達していれば、同じ縦列の残りは白マス
					for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
						if (masu[yIndex][xIndex] == Masu.SPACE) {
							masu[yIndex][xIndex] = Masu.NOT_BLACK;
						}
					}
				} else if (notBlackCnt == getHalfYLength()) {
					// 白マス数が盤面の半分に達していれば、同じ縦列の残りは黒マス
					for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
						if (masu[yIndex][xIndex] == Masu.SPACE) {
							masu[yIndex][xIndex] = Masu.BLACK;
						}
					}
				}
			}
			return true;
		}

		/**
		 * 別部屋の黒マス隣接セルを白マスにする。
		 * 別部屋の黒マス隣接セルが黒マスの場合falseを返す。
		 */
		public boolean nextSolve() {
			for (Room room : rooms) {
				for (Position pos : room.getMember()) {
					if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK) {
						if (pos.getyIndex() != 0) {
							Position blackPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
							if (!room.getMember().contains(blackPos)) {
								if (masu[blackPos.getyIndex()][blackPos.getxIndex()] == Masu.BLACK) {
									return false;
								} else {
									masu[blackPos.getyIndex()][blackPos.getxIndex()] = Masu.NOT_BLACK;
								}
							}
						}
						if (pos.getxIndex() != getXLength() - 1) {
							Position blackPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
							if (!room.getMember().contains(blackPos)) {
								if (masu[blackPos.getyIndex()][blackPos.getxIndex()] == Masu.BLACK) {
									return false;
								} else {
									masu[blackPos.getyIndex()][blackPos.getxIndex()] = Masu.NOT_BLACK;
								}
							}
						}
						if (pos.getyIndex() != getYLength() - 1) {
							Position blackPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
							if (!room.getMember().contains(blackPos)) {
								if (masu[blackPos.getyIndex()][blackPos.getxIndex()] == Masu.BLACK) {
									return false;
								} else {
									masu[blackPos.getyIndex()][blackPos.getxIndex()] = Masu.NOT_BLACK;
								}
							}
						}
						if (pos.getxIndex() != 0) {
							Position blackPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
							if (!room.getMember().contains(blackPos)) {
								if (masu[blackPos.getyIndex()][blackPos.getxIndex()] == Masu.BLACK) {
									return false;
								} else {
									masu[blackPos.getyIndex()][blackPos.getxIndex()] = Masu.NOT_BLACK;
								}
							}
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
			if (!varticalSolve()) {
				return false;
			}
			if (!nextSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			} else {
				if (!dropAndCheck()) {
					return false;
				}
				if (!capacitySolve()) {
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

		/**
		 * dropの結果、上半分に黒マスができたらfalseを返す
		 */
		private boolean dropAndCheck() {
			Masu[][] droppedMasu = drop();
			for (int yIndex = 0; yIndex < getHalfYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (droppedMasu[yIndex][xIndex] == Masu.BLACK) {
						return false;
					}
				}
			}
			return true;
		}

		/**
		 * 既にある黒から届かない領域を白マスにする。
		 * 既にある黒から届かない場所に黒を見つけた場合falseを返す。
		 */
		public boolean capacitySolve() {
			for (Room room : rooms) {
				Set<Position> alreadySurvey = new HashSet<>();
				for (Position pos : room.getMember()) {
					if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK && !alreadySurvey.contains(pos)) {
						if (!alreadySurvey.isEmpty()) {
							return false;
						}
						Set<Position> continuePosSet = new HashSet<>();
						continuePosSet.add(pos);
						if (room.getBlackCnt() == -1) {
							setContinuePosSet(pos, continuePosSet, null);
						} else {
							setContinuePosSetUseDistance(new HashSet<>(continuePosSet), continuePosSet,
									room.getBlackCnt() - 1);
						}
						alreadySurvey.addAll(continuePosSet);
					}
				}
				if (!alreadySurvey.isEmpty()) {
					Set<Position> toWhitePosSet = new HashSet<>(room.getMember());
					toWhitePosSet.removeAll(alreadySurvey);
					for (Position pos : toWhitePosSet) {
						if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
							masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
						}
					}
				}
			}
			return true;
		}

		/**
		 * pivotPosSetを起点に上下左右に壁または白確定でないマスをpivotPosSetからのdistanceだけつなげていく。
		 */
		private void setContinuePosSetUseDistance(Set<Position> pivotPosSet, Set<Position> continuePosSet,
				int distance) {
			if (distance == 0 || pivotPosSet.isEmpty()) {
				return;
			}
			Set<Position> nextPivotPosSet = new HashSet<>();
			for (Position pos : pivotPosSet) {
				if (pos.getyIndex() != 0) {
					Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
					if (!tateWall[pos.getyIndex() - 1][pos.getxIndex()] && !continuePosSet.contains(nextPos)
							&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
						nextPivotPosSet.add(nextPos);
						continuePosSet.add(nextPos);
					}
				}
				if (pos.getxIndex() != getXLength() - 1) {
					Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
					if (!yokoWall[pos.getyIndex()][pos.getxIndex()]
							&& !continuePosSet.contains(nextPos)
							&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
						nextPivotPosSet.add(nextPos);
						continuePosSet.add(nextPos);
					}
				}
				if (pos.getyIndex() != getYLength() - 1) {
					Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
					if (!tateWall[pos.getyIndex()][pos.getxIndex()]
							&& !continuePosSet.contains(nextPos)
							&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
						nextPivotPosSet.add(nextPos);
						continuePosSet.add(nextPos);
					}
				}
				if (pos.getxIndex() != 0) {
					Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
					if (!yokoWall[pos.getyIndex()][pos.getxIndex() - 1]
							&& !continuePosSet.contains(nextPos)
							&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
						nextPivotPosSet.add(nextPos);
						continuePosSet.add(nextPos);
					}
				}
			}
			setContinuePosSetUseDistance(nextPivotPosSet, continuePosSet, distance - 1);
		}

		/**
		 * posを起点に上下左右に同部屋の白確定でないマスを無制限につなげていく。
		 */
		private void setContinuePosSetSameRoom(Position pos, Set<Position> continuePosSet, Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP && !tateWall[pos.getyIndex() - 1][pos.getxIndex()]) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.DOWN);
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT
					&& !yokoWall[pos.getyIndex()][pos.getxIndex()]) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.LEFT);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN
					&& !tateWall[pos.getyIndex()][pos.getxIndex()]) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.UP);
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT && !yokoWall[pos.getyIndex()][pos.getxIndex() - 1]) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.RIGHT);
				}
			}
		}
	}

	public static class Room {
		@Override
		public String toString() {
			return "Room [blackCnt=" + blackCnt + ", member=" +
					member + "]";
		}

		// 黒マスが何マスあるか。数字がない場合は-1
		private final int blackCnt;
		// 部屋に属するマスの集合
		private final Set<Position> member;

		public Room(int capacity, Set<Position> member) {
			this.blackCnt = capacity;
			this.member = member;
		}

		public int getBlackCnt() {
			return blackCnt;
		}

		public Set<Position> getMember() {
			return member;
		}

		// 一番左→一番上の位置を返す。画面の数字描画用。
		public Position getNumberMasuPos() {
			int yIndex = Integer.MAX_VALUE;
			int xIndex = Integer.MAX_VALUE;
			for (Position pos : member) {
				if (pos.getxIndex() < xIndex) {
					xIndex = pos.getxIndex();
				}
			}
			for (Position pos : member) {
				if (pos.getxIndex() == xIndex && pos.getyIndex() < yIndex) {
					yIndex = pos.getyIndex();
				}
			}
			return new Position(yIndex, xIndex);
		}
	}

	private final Field field;
	protected int count = 0;

	public StostoneSolver(int height, int width, String param) {
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
		System.out.println(new StostoneSolver(height, width, param).solve());
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
				if (!candSolve(field, recursiveCnt)) {
					return "問題に矛盾がある可能性があります。途中経過を返します。";
				}
				recursiveCnt++;
			}
			if (recursiveCnt == 3 && field.getStateDump().equals(befStr)) {
				return "解けませんでした。途中経過を返します。";
			}
		}
		System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
		System.out.println("難易度:" + (count));
		System.out.println(field);
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count).toString();
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