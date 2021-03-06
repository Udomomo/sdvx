package myamya.other.solver.heyawake;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Solver;

public class HeyawakeSolver implements Solver {

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
		// 同一グループに属するマスの情報
		private final Map<Integer, List<String>> roomsCand;
		// ayeheya
		private final boolean ayeheya;

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

		public int getXLength() {
			return masu[0].length;
		}

		public Field(int height, int width, String param, boolean ayeheya) {
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
						blackCnt = Integer.parseInt("" + param.charAt(readPos + 1) + param.charAt(readPos + 2), 16);
						readPos++;
						readPos++;
					} else if (ch == '+') {
						blackCnt = Integer.parseInt(
								"" + param.charAt(readPos + 1) + param.charAt(readPos + 2) + param.charAt(readPos + 3),
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
						setContinuePosSet(pos, continuePosSet);
						rooms.add(new Room(blackCntList.get(blackCntListIndex), new ArrayList<>(continuePosSet)));
						blackCntListIndex++;
					}
				}
			}
			roomsCand = new HashMap<>();
			this.ayeheya = ayeheya;
		}

		/**
		 * 部屋の候補を決定する。最初に一度だけ呼ぶこと
		 */
		public void roomsCandSetUp() {
			for (int i = 0; i < rooms.size(); i++) {
				Room room = rooms.get(i);
				if (room.getBlackCnt() != -1) {
					if (room.member.size() <= 50 && room.isRect()) {
						Position leftUp = room.getMember().get(0);
						Position rightDown = room.getMember().get(room.getMember().size() - 1);
						int roomHeight = rightDown.getyIndex() - leftUp.getyIndex() + 1;
						int roomWidth = rightDown.getxIndex() - leftUp.getxIndex() + 1;
						HeyaSolver heyaSolver = new HeyaSolver(roomHeight, roomWidth, room.getBlackCnt(),
								leftUp.getyIndex() == 0, rightDown.getxIndex() == getXLength() - 1,
								rightDown.getyIndex() == getYLength() - 1, leftUp.getxIndex() == 0, 10000);
						List<String> result = heyaSolver.solveForSolver();
						cnt = cnt + (heyaSolver.cnt / 20);
						if (!result.isEmpty()) {
							roomsCand.put(i, result);
						}
					}
				}
			}
			System.out.println("部屋ソルバー負荷:" + cnt);
		}

		int cnt = 0;

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
			roomsCand = new HashMap<>();
			for (Entry<Integer, List<String>> entry : other.roomsCand.entrySet()) {
				roomsCand.put(entry.getKey(), new ArrayList<>(entry.getValue()));
			}
			this.ayeheya = other.ayeheya;
		}

		// posを起点に上下左右に壁または白確定でないマスを無制限につなげていく。
		private void setContinuePosSet(Position pos, Set<Position> continuePosSet) {
			if (pos.getyIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && !tateWall[pos.getyIndex() - 1][pos.getxIndex()]
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getxIndex() != getXLength() - 1) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos) && !yokoWall[pos.getyIndex()][pos.getxIndex()]
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getyIndex() != getYLength() - 1) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && !tateWall[pos.getyIndex()][pos.getxIndex()]
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getxIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos) && !yokoWall[pos.getyIndex()][pos.getxIndex() - 1]
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet);
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
						sb.append(yokoWall[yIndex][xIndex] == true ? "□" : "＊");
					}
				}
				sb.append("□");
				sb.append(System.lineSeparator());
				if (yIndex != getYLength() - 1) {
					sb.append("□");
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						sb.append(tateWall[yIndex][xIndex] == true ? "□" : "＊");
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
			return sb.toString();
		}

		/**
		 * 部屋のマスを埋める。黒マス不足・過剰はfalseを返す。
		 */
		public boolean roomSolve() {
			for (Room room : rooms) {
				if (room.getBlackCnt() != -1) {
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
		 * 黒マス隣接セルを白マスにする。
		 * 黒マス隣接セルが黒マスの場合falseを返す。
		 */
		public boolean nextSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.BLACK) {
						Masu masuUp = yIndex == 0 ? Masu.NOT_BLACK : masu[yIndex - 1][xIndex];
						Masu masuRight = xIndex == getXLength() - 1 ? Masu.NOT_BLACK : masu[yIndex][xIndex + 1];
						Masu masuDown = yIndex == getYLength() - 1 ? Masu.NOT_BLACK : masu[yIndex + 1][xIndex];
						Masu masuLeft = xIndex == 0 ? Masu.NOT_BLACK : masu[yIndex][xIndex - 1];
						if (masuUp == Masu.BLACK || masuRight == Masu.BLACK || masuDown == Masu.BLACK
								|| masuLeft == Masu.BLACK) {
							return false;
						}
						if (masuUp == Masu.SPACE) {
							masu[yIndex - 1][xIndex] = Masu.NOT_BLACK;
						}
						if (masuRight == Masu.SPACE) {
							masu[yIndex][xIndex + 1] = Masu.NOT_BLACK;
						}
						if (masuDown == Masu.SPACE) {
							masu[yIndex + 1][xIndex] = Masu.NOT_BLACK;
						}
						if (masuLeft == Masu.SPACE) {
							masu[yIndex][xIndex - 1] = Masu.NOT_BLACK;
						}
					}
				}
			}
			return true;
		}

		/**
		 * 上下左右で白マスが3部屋連続する場所を白確定にする。
		 * 白にできない場合はfalseを返す。
		 */
		public boolean continueRoomSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					boolean sanren = false;
					int continueWhite = 0;
					for (int targetY = yIndex - 1; targetY >= 0; targetY--) {
						if (masu[targetY][xIndex] != Masu.NOT_BLACK) {
							break;
						}
						if (tateWall[targetY][xIndex]) {
							continueWhite++;
						}
						if (continueWhite == 2) {
							sanren = true;
							break;
						}
					}
					if (sanren == true) {
						if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
							return false;
						} else {
							masu[yIndex][xIndex] = Masu.BLACK;
							continue;
						}
					}
					continueWhite = 0;
					for (int targetX = xIndex; targetX < getXLength() - 1; targetX++) {
						if (masu[yIndex][targetX + 1] != Masu.NOT_BLACK) {
							break;
						}
						if (yokoWall[yIndex][targetX]) {
							continueWhite++;
						}
						if (continueWhite == 2) {
							sanren = true;
							break;
						}
					}
					if (sanren == true) {
						if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
							return false;
						} else {
							masu[yIndex][xIndex] = Masu.BLACK;
							continue;
						}
					}
					continueWhite = 0;
					for (int targetY = yIndex; targetY < getYLength() - 1; targetY++) {
						if (masu[targetY + 1][xIndex] != Masu.NOT_BLACK) {
							break;
						}
						if (tateWall[targetY][xIndex]) {
							continueWhite++;
						}
						if (continueWhite == 2) {
							sanren = true;
							break;
						}
					}
					if (sanren == true) {
						if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
							return false;
						} else {
							masu[yIndex][xIndex] = Masu.BLACK;
							continue;
						}
					}
					continueWhite = 0;
					for (int targetX = xIndex - 1; targetX >= 0; targetX--) {
						if (masu[yIndex][targetX] != Masu.NOT_BLACK) {
							break;
						}
						if (yokoWall[yIndex][targetX]) {
							continueWhite++;
						}
						if (continueWhite == 2) {
							sanren = true;
							break;
						}
					}
					if (sanren == true) {
						if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
							return false;
						} else {
							masu[yIndex][xIndex] = Masu.BLACK;
							continue;
						}
					}
				}
			}
			return true;
		}

		/**
		 * 部屋の候補情報を使ってマス目を埋める。
		 * 部屋の候補がなくなったらfalseを返す。
		 */
		public boolean roomCandSolve() {
			for (int i = 0; i < rooms.size(); i++) {
				List<String> candList = roomsCand.get(i);
				if (candList != null) {
					Room room = rooms.get(i);
					StringBuilder sb = new StringBuilder();
					Position leftUp = room.getMember().get(0);
					Position rightDown = room.getMember().get(room.getMember().size() - 1);
					for (int yIndex = leftUp.getyIndex(); yIndex <= rightDown.getyIndex(); yIndex++) {
						for (int xIndex = leftUp.getxIndex(); xIndex <= rightDown.getxIndex(); xIndex++) {
							sb.append(masu[yIndex][xIndex]);
						}
					}
					String state = sb.toString();
					for (Iterator<String> iterator = candList.iterator(); iterator.hasNext();) {
						String cand = iterator.next();
						if (state.length() != cand.length()) {
							iterator.remove();
						} else {
							for (int idx = 0; idx < state.length(); idx++) {
								char a = state.charAt(idx);
								char b = cand.charAt(idx);
								if ((a == '■' && b == '・') || (a == '・' && b == '■')) {
									iterator.remove();
									break;
								}
							}
						}
					}
					if (candList.size() == 0) {
						return false;
					}
					StringBuilder fixState = new StringBuilder(candList.get(0));
					for (String cand : candList) {
						for (int idx = 0; idx < fixState.length(); idx++) {
							char a = fixState.charAt(idx);
							char b = cand.charAt(idx);
							if ((a == '■' && b == '・') || (a == '・' && b == '■')) {
								fixState.setCharAt(idx, '　');
							}
						}
					}
					for (int idx = 0; idx < fixState.length(); idx++) {
						masu[room.getMember().get(idx).getyIndex()][room
								.getMember().get(idx).getxIndex()] = fixState.charAt(idx) == '■'
										? Masu.BLACK
										: fixState.charAt(idx) == '・'
												? Masu.NOT_BLACK
												: Masu.SPACE;
					}
				}
			}
			return true;
		}

		/**
		 * 白マスがひとつながりにならない場合Falseを返す。
		 */
		public boolean connectSolve() {
			Set<Position> whitePosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
						Position whitePos = new Position(yIndex, xIndex);
						if (whitePosSet.size() == 0) {
							whitePosSet.add(whitePos);
							setContinueWhitePosSet(whitePos, whitePosSet, null);
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
		 * posを起点に上下左右に黒確定でないマスをつなげていく。壁は無視する。
		 */
		private void setContinueWhitePosSet(Position pos, Set<Position> continuePosSet, Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueWhitePosSet(nextPos, continuePosSet, Direction.DOWN);
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueWhitePosSet(nextPos, continuePosSet, Direction.LEFT);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueWhitePosSet(nextPos, continuePosSet, Direction.UP);
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueWhitePosSet(nextPos, continuePosSet, Direction.RIGHT);
				}
			}
		}

		/**
		 * ayeheyaの場合は点対称が必須。
		 */
		public boolean ayeheyaSolve() {
			if (ayeheya) {
				for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						Position pos = new Position(yIndex, xIndex);
						for (Room room : rooms) {
							// TODO ここ毎回計算するの無駄なので事前に持ったほうがいい
							int minY = Integer.MAX_VALUE;
							int minX = Integer.MAX_VALUE;
							int maxY = 0;
							int maxX = 0;
							boolean roomContains = false;
							for (Position roomPos : room.getMember()) {
								if (minY > roomPos.getyIndex()) {
									minY = roomPos.getyIndex();
								}
								if (maxY < roomPos.getyIndex()) {
									maxY = roomPos.getyIndex();
								}
								if (minX > roomPos.getxIndex()) {
									minX = roomPos.getxIndex();
								}
								if (maxX < roomPos.getxIndex()) {
									maxX = roomPos.getxIndex();
								}
								if (roomPos.equals(pos)) {
									roomContains = true;
								}
							}
							if (roomContains) {
								Position anotherPos = new Position(minY + (maxY - pos.getyIndex()),
										minX + (maxX - pos.getxIndex()));
								if (!pos.equals(anotherPos)) {
									if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE
											|| masu[anotherPos.getyIndex()][anotherPos.getxIndex()] == Masu.SPACE) {
										masu[pos.getyIndex()][pos.getxIndex()] = masu[anotherPos.getyIndex()][anotherPos
												.getxIndex()];
										masu[anotherPos.getyIndex()][anotherPos.getxIndex()] = masu[pos.getyIndex()][pos
												.getxIndex()];
									} else if (masu[pos.getyIndex()][pos
											.getxIndex()] != masu[anotherPos.getyIndex()][anotherPos.getxIndex()]) {
										return false;
									}
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
			if (!nextSolve()) {
				return false;
			}
			if (!continueRoomSolve()) {
				return false;
			}
			if (!roomCandSolve()) {
				return false;
			}
			if (!ayeheyaSolve()) {
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

	public static class Room {
		@Override
		public String toString() {
			return "Room [blackCnt=" + blackCnt + ", member=" + member + "]";
		}

		// 黒マスが何マスあるか。数字がない場合は-1
		private final int blackCnt;
		// 部屋に属するマスの集合
		private final List<Position> member;

		public Room(int capacity, List<Position> member) {
			this.blackCnt = capacity;
			this.member = member;
			this.member.sort(new Comparator<Position>() {
				@Override
				public int compare(Position o1, Position o2) {
					return o1.getyIndex() * 100 + o1.getxIndex() - (o2.getyIndex() * 100 + o2.getxIndex());
				}
			});
		}

		public int getBlackCnt() {
			return blackCnt;
		}

		public List<Position> getMember() {
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

		/**
		 * 自身が長方形かどうか
		 */
		public boolean isRect() {
			int minY = Integer.MAX_VALUE;
			int maxY = 0;
			int minX = Integer.MAX_VALUE;
			int maxX = 0;
			for (Position pos : member) {
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
			for (int yIndex = minY; yIndex <= maxY; yIndex++) {
				for (int xIndex = minX; xIndex <= maxX; xIndex++) {
					if (!member.contains(new Position(yIndex, xIndex))) {
						return false;
					}
				}
			}
			return true;
		}
	}

	private final Field field;
	private int count = 0;

	public HeyawakeSolver(int height, int width, String param, boolean ayeheya) {
		field = new Field(height, width, param, ayeheya);
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
		System.out.println(new HeyawakeSolver(height, width, param, false).solve());
	}

	@Override
	public String solve() {
		long start = System.nanoTime();
		field.roomsCandSetUp();
		while (!field.isSolved()) {
			System.out.println(field);
			String befStr = field.getStateDump();
			if (!field.solveAndCheck()) {
				return "問題に矛盾がある可能性があります。途中経過を返します。";
			}
			int recursiveCnt = 0;
			while (field.getStateDump().equals(befStr) && recursiveCnt < 4) {
				if (!candSolve(field, recursiveCnt)) {
					return "問題に矛盾がある可能性があります。途中経過を返します。";
				}
				recursiveCnt++;
			}
			if (recursiveCnt == 4 && field.getStateDump().equals(befStr)) {
				return "解けませんでした。途中経過を返します。";
			}
		}
		System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
		System.out.println("難易度:" + (count + field.cnt));
		System.out.println(field);
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count + field.cnt).toString();
	}

	/**
	 * 仮置きして調べる
	 */
	private boolean candSolve(Field field, int recursive) {
		String str = field.getStateDump();
		for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				if (!oneCandSolve(field, yIndex, xIndex, recursive)) {
					return false;
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
		if (field.masu[yIndex][xIndex] == Masu.SPACE) {
			count++;
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
		}
		return true;
	}

}
