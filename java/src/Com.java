
public class Com {
    private static final int MAX_VALUE = Evaluator.DISK_VALUE * 200;
    private static final int HASH_SIZE = 18;
    private static final int TEMP_MOVE_ARRAY_NUM = Board.SIZE * Board.SIZE / 2;
    private static final int MPC_DEPTH_MIN = 3;

    private static class MPCInfo {
        int mDepth;
        int mOffset;
        int mDeviation;
    }

    private static class MoveList {
        public MoveList mPrev;
        public MoveList mNext;
        public int mPos;

        public void remove() {
            if (mPrev != null) {
                mPrev.mNext = mNext;
            }
            if (mNext != null) {
                mNext.mPrev = mPrev;
            }
        }
        public void recover() {
            if (mPrev != null) {
                mPrev.mNext = this;
            }
            if (mNext != null) {
                mNext.mPrev = this;
            }
        }
    }

    private static class SortList {
        public MoveList mMove;
        public int mValue;

        public void copy(SortList list) {
            mMove = list.mMove;
            mValue = list.mValue;
        }
    }

    private Board mBoard;
    private Evaluator mEvaluator;
    private Opening mOpening;
    private Hash mHash;
    private boolean mUseOpening;
    private int mMiddleDepth;
    private int mWLDDepth;
    private int mExactDepth;
    private int mRandom;
    private int mRandomRatio;
    private MoveList[] mMoves;
    private MPCInfo[][] mMPCInfo;
    private MPCInfo[] mCurrentMPCInfo;
    private boolean mThinking;
    
    public Com(Evaluator evaluator, Opening opening) {
        int i;
        mBoard = new Board();
        mEvaluator = evaluator;
        mOpening = opening;
        mHash = new Hash(HASH_SIZE);
        mHash.clear();
        mUseOpening = false;
        mMiddleDepth = 2;
        mWLDDepth = 2;
        mExactDepth = 2;
        mRandom = 0;
        mRandomRatio = 0;
        mThinking = false;
        mMoves = new MoveList[Board.SIZE * Board.SIZE];
        for (i = 0; i < mMoves.length; i++) {
            mMoves[i] = new MoveList();
        }
        mMPCInfo = new MPCInfo[Evaluator.getStageCount()][];
        for (i = 0; i < mMPCInfo.length; i++) {
            mMPCInfo[i] = new MPCInfo[0];
        }
        mCurrentMPCInfo = mMPCInfo[0];
    }

    public void setLevel(int middle, int exact, int wld) {
        mMiddleDepth = middle;
        mWLDDepth = wld;
        mExactDepth = exact;
    }

    public void setRandom(int value, int ratio) {
        mRandom = value * Evaluator.DISK_VALUE;
        mRandomRatio = ratio;
    }

    public void setOpeining(boolean useOpening) {
        mUseOpening = useOpening;
    }

    public int getNextMove(Board board, int color) {
        int left;
        int move;
        int opponent = Board.getOpponent(color);

        if (mThinking) {
            return Board.NOMOVE;
        }
        mThinking = true;
        mBoard.copy(board);
        left = mBoard.countDisks(Board.EMPTY);
        makeMoveList();
        mBoard.initializePattern();
        move = searchOpening(color, opponent);
        if (move != Board.NOMOVE) {
        } else if (left <= mExactDepth) {
            mEvaluator.setStage(0);
            move = searchEndRoot(left, color, opponent, false);
        } else if (left <= mWLDDepth) {
            mEvaluator.setStage(0);
            move = searchEndRoot(left, color, opponent, true);
        } else {
            mEvaluator.setStage(left - mMiddleDepth);
            int id = Evaluator.getStageIndex(left - mMiddleDepth);
            mCurrentMPCInfo = mMPCInfo[id];
            if ((color == Board.WHITE && mMiddleDepth %2 == 0) ||
                (color == Board.BLACK && mMiddleDepth %2 == 1)) {
                mBoard.reverse();
                move = searchMiddleRoot(mMiddleDepth, opponent, color);
                mBoard.reverse();
            } else {
                move = searchMiddleRoot(mMiddleDepth, color, opponent);                
            }
        }
        mThinking = false;
        return move;
    }

    public boolean isThinking() {
        return mThinking;
    }

    public boolean loadMPCInfo(String fileName) {
        return false;
    }

    private static boolean random(int max, int ratio) {
        return ((int)(Math.random() * max) < ratio);
    }

    private int searchOpening(int color, int opponent) {
        MoveList p;
        Integer positionValue;
        int value;
        int bestMove = Board.NOMOVE;
        int bestValue = -MAX_VALUE;
        int currentValue;
        int count = 0;
        final int minRandom = Evaluator.DISK_VALUE * 4;
        final int bound = Evaluator.DISK_VALUE * 2;
        if (!mUseOpening || mOpening == null) {
            return bestMove;
        }
        positionValue = mOpening.get(mBoard, color);
        if (positionValue != null) {
        	currentValue = -positionValue.intValue();
        } else {
        	currentValue = MAX_VALUE;
        }
        for (p = mMoves[0].mNext; p != null; p = p.mNext) {
            if (mBoard.flip(color, p.mPos) <= 0) {
            	continue;
            }
            positionValue = mOpening.get(mBoard, opponent);
            if (positionValue != null) {
                value = -positionValue.intValue();
                if (mRandom >= minRandom && value < currentValue && 
                	value >= currentValue - bound && value >= -bound) {
                	value += bound;
                }
                if (value > bestValue) {
                    count = 1;
                    bestMove = p.mPos;
                    bestValue = value;
                } else if (value == bestValue) {
                    count++;
                    if (random(count, 1)) {
                        bestMove = p.mPos;
                    }
                }
            }
            mBoard.unflip();
        }
        return bestMove;
    }

    private int searchMiddleRoot(int depth, int color, int opponent) {
        int value;
        int bestValue = -MAX_VALUE;
        int bestMove;
        int secondValue = -MAX_VALUE;
        int secondMove;
        SortList[] info = new SortList[TEMP_MOVE_ARRAY_NUM];
        int infoCount;

        infoCount = sortMoveList(color, info);
        if (infoCount == 0) {
            return Board.PASS;
        } else if (infoCount == 1) {
            return info[0].mMove.mPos;
        }
        bestMove = info[0].mMove.mPos;
        secondMove = bestMove;
        MoveList current;
        for (int i = 0; i < infoCount; i++) {
            current = info[i].mMove;
            mBoard.flipPattern(color, current.mPos);
            current.remove();
            value = -searchMiddle(depth - 1, -MAX_VALUE, -bestValue + mRandom + 1, opponent, color, false);
            mBoard.unflipPattern();
            current.recover();
            info[i].mValue = value;
            if (value > bestValue) {
                secondMove = bestMove;
                secondValue = bestValue;
                bestMove = current.mPos;
                bestValue = value;
            } else if (value > secondValue) {
                secondMove = current.mPos;
                secondValue = value;
            }
        }
       	if (bestValue - secondValue <= mRandom && random(100, mRandomRatio)) {
       		int count = 0;
       		for (int i = 0; i < infoCount; i++) {
       			if (bestValue - info[i].mValue < mRandom) {
       				count++;
       				if (count == 1 || random(count, 1)) {
       					bestMove = info[i].mMove.mPos;
       				}
       			}
       		}
       	}
        return bestMove;
    }

    private int searchMiddle(int depth, int alpha, int beta, int color, int opponent, boolean pass) {
        int bestValue = alpha;
        boolean canMove = false;
        int value;
        if (depth == 0) {
            int count;
            count = mBoard.countDisks(color);
            if (count == 0) {
                return -Evaluator.DISK_VALUE * Board.SIZE * Board.SIZE;
            }
            count = mBoard.countDisks(opponent);
            if (count == 0) {
                return Evaluator.DISK_VALUE * Board.SIZE * Board.SIZE;                
            }
            return mEvaluator.getValue(mBoard);
        }
        if (depth > 2) {
            long key = mBoard.getHashValue(color);
            Hash.Info hashInfo = mHash.get(key);
            if (hashInfo != null && hashInfo.mDepth >= depth) {
                if (hashInfo.mUpper <= alpha) {
                    return alpha;
                } else if (hashInfo.mLower >= beta) {
                    return beta;
                } else if (hashInfo.mLower >= hashInfo.mUpper) {
                    return hashInfo.mLower;
                }
                if (hashInfo.mUpper < beta) {
                    beta = hashInfo.mUpper;
                }
                if (hashInfo.mLower > alpha) {
                    bestValue = hashInfo.mLower;
                }
            } else {
                hashInfo = new Hash.Info(key, -MAX_VALUE, MAX_VALUE, depth);
            }
            if (depth >= MPC_DEPTH_MIN && depth < MPC_DEPTH_MIN + mCurrentMPCInfo.length) {
                MPCInfo mpcInfo = mCurrentMPCInfo[depth - MPC_DEPTH_MIN];
                value = bestValue - mpcInfo.mDeviation + mpcInfo.mOffset;
                if (searchMiddle(mpcInfo.mDepth, value - 1, value, color, opponent, pass) < value) {
                    return bestValue;
                }
                value = beta + mpcInfo.mDepth + mpcInfo.mOffset;
                if (searchMiddle(mpcInfo.mDepth, value, value + 1, color, opponent, pass) > value) {
                    return beta;
                }
            }
            SortList[] list = new SortList[TEMP_MOVE_ARRAY_NUM];
            int listCount = sortMoveList(color, list);
            if (listCount > 0) {
                canMove = true;
            }
            for (int i = 0; i < listCount; i++) {
                mBoard.flipPattern(color, list[i].mMove.mPos);
                list[i].mMove.remove();
                value = -searchMiddle(depth - 1, -beta, -bestValue, opponent, color, false);
                mBoard.unflipPattern();
                list[i].mMove.recover();
                if (value > bestValue) {
                    if (value >= beta) {
                        hashInfo.mLower = value;
                        mHash.set(hashInfo);
                        return beta;
                    }
                    bestValue = value;
                }
            }
            if (canMove) {
                if (bestValue > alpha) {
                    hashInfo.mUpper = hashInfo.mLower = bestValue;
                } else {
                    hashInfo.mUpper = bestValue;
                }
                mHash.set(hashInfo);
            }
        } else {
            MoveList p;
            for (p = mMoves[0].mNext; p != null; p = p.mNext) {
                if (mBoard.flipPattern(color, p.mPos) > 0) {
                    p.remove();
                    canMove = true;
                    value = -searchMiddle(depth - 1, -beta, -bestValue, opponent, color, false);
                    mBoard.unflipPattern();
                    p.recover();
                    if (value > bestValue) {
                        if (value >= beta) {
                            return beta;
                        }
                        bestValue = value;
                    }
                }
            }
        }
        if (!canMove) {
            if (pass) {
                int count;
                int countOpponent;
                count = mBoard.countDisks(color);
                countOpponent = mBoard.countDisks(opponent);
                if (count == 0) {
                    bestValue = -Evaluator.DISK_VALUE * Board.SIZE * Board.SIZE;
                } else if (countOpponent == 0) {
                    bestValue = Evaluator.DISK_VALUE * Board.SIZE * Board.SIZE; 
                } else {
                    bestValue = Evaluator.DISK_VALUE * (count - countOpponent);
                }
            } else {
                bestValue = -searchMiddle(depth - 1, -beta, -bestValue, opponent, color, true);
            }
        }
        return bestValue;
    }

    private int searchEndRoot(int depth, int color, int opponent, boolean wld) {
        int value;
        int bestValue = -Board.SIZE * Board.SIZE;
        int beta;
        int bestMove;
        SortList[] info = new SortList[TEMP_MOVE_ARRAY_NUM];
        int infoCount;

        if (wld) {
            beta = 1;
        } else {
            beta = Board.SIZE * Board.SIZE;
        }
        infoCount = sortMoveList(color, info);
        if (infoCount == 0) {
            return Board.PASS;
        } else if (infoCount == 1) {
            return info[0].mMove.mPos;
        }
        bestMove = info[0].mMove.mPos;
        MoveList current;
        for (int i = 0; i < infoCount; i++) {
            current = info[i].mMove;
            mBoard.flipPattern(color, current.mPos);
            current.remove();
            value = -searchEnd(depth - 1, -beta, -bestValue + mRandom + 1, opponent, color, false);
            mBoard.unflipPattern();
            current.recover();
            if (value > bestValue) {
                bestMove = current.mPos;
                bestValue = value;
            }
        }
        return bestMove;
    }

    private int searchEnd(int depth, int alpha, int beta, int color, int opponent, boolean pass) {
        int bestValue = alpha;
        boolean canMove = false;
        int value;
        MoveList p;

        if (depth == 1) {
            p = mMoves[0].mNext;
            value = mBoard.countFlips(color, p.mPos);
            bestValue = mBoard.countDisks(color) - mBoard.countDisks(opponent);
            if (value > 0) {
                return bestValue + value + value + 1;
            }
            value = mBoard.countFlips(opponent, p.mPos);
            if (value > 0) {
                return bestValue - value - value - 1;
            }
            return bestValue;
        }
        if (depth > 8) {
            long key = mBoard.getHashValue(color);
            Hash.Info hashInfo = mHash.get(key);
            if (hashInfo != null && hashInfo.mDepth >= depth) {
                if (hashInfo.mUpper <= alpha) {
                    return alpha;
                } else if (hashInfo.mLower >= beta) {
                    return beta;
                } else if (hashInfo.mLower >= hashInfo.mUpper) {
                    return hashInfo.mLower;
                }
                if (hashInfo.mUpper < beta) {
                    beta = hashInfo.mUpper;
                }
                if (hashInfo.mLower > alpha) {
                    bestValue = hashInfo.mLower;
                }
            } else {
                hashInfo = new Hash.Info(key, -MAX_VALUE, MAX_VALUE, depth);
            }
            SortList[] list = new SortList[TEMP_MOVE_ARRAY_NUM];
            int listCount = sortMoveList(color, list);
            if (listCount > 0) {
                canMove = true;
            }
            for (int i = 0; i < listCount; i++) {
                mBoard.flipPattern(color, list[i].mMove.mPos);
                list[i].mMove.remove();
                value = -searchEnd(depth - 1, -beta, -bestValue, opponent, color, false);
                mBoard.unflipPattern();
                list[i].mMove.recover();
                if (value > bestValue) {
                    if (value >= beta) {
                        hashInfo.mLower = value;
                        mHash.set(hashInfo);
                        return beta;
                    }
                    bestValue = value;
                }
            }
            if (canMove) {
                if (bestValue > alpha) {
                    hashInfo.mUpper = hashInfo.mLower = bestValue;
                } else {
                    hashInfo.mUpper = bestValue;
                }
                mHash.set(hashInfo);
            }
        } else {
            for (p = mMoves[0].mNext; p != null; p = p.mNext) {
                if (mBoard.flip(color, p.mPos) > 0) {
                    p.remove();
                    canMove = true;
                    value = -searchEnd(depth - 1, -beta, -bestValue, opponent, color, false);
                    mBoard.unflip();
                    p.recover();
                    if (value > bestValue) {
                        if (value >= beta) {
                            return beta;
                        }
                        bestValue = value;
                    }
                }
            }
        }
        if (!canMove) {
            if (pass) {
                int count;
                int countOpponent;
                count = mBoard.countDisks(color);
                countOpponent = mBoard.countDisks(opponent);
                if (count == 0) {
                    bestValue = -Board.SIZE * Board.SIZE;
                } else if (countOpponent == 0) {
                    bestValue = Board.SIZE * Board.SIZE; 
                } else {
                    bestValue = count - countOpponent;
                }
            } else {
                bestValue = -searchEnd(depth, -beta, -bestValue, opponent, color, true);
            }
        }
        return bestValue;
    }

    private int sortMoveList(int color, SortList[] list) {
        MoveList p;
        int count = 0;
        if (color == Board.BLACK) {
            for (p = mMoves[0].mNext; p != null; p = p.mNext) {
                if (mBoard.flipPattern(color, p.mPos) > 0) {
                    list[count] = new SortList();
                    list[count].mMove = p;
                    list[count].mValue = mEvaluator.getValue(mBoard);
                    count++;
                    mBoard.unflipPattern();
                }
            }
        } else {
            for (p = mMoves[0].mNext; p != null; p = p.mNext) {
                if (mBoard.flipPattern(color, p.mPos) > 0) {
                    list[count] = new SortList();
                    list[count].mMove = p;
                    list[count].mValue = -mEvaluator.getValue(mBoard);
                    count++;
                    mBoard.unflipPattern();
                }
            }            
        }
        SortList best;
        SortList temp = new SortList();
        for (int i = 0; i < count; i++) {
            best = list[i];
            for (int j = i + i; j < count; j++) {
                if (list[j].mValue > best.mValue) {
                    best = list[j];
                }
            }
            if (best != list[i]) {
                temp.copy(best);
                best.copy(list[i]);
                list[i].copy(temp);
            }
        }
        return count;
    }

    private void makeMoveList() {
        int[] list = new int[]{
            Board.A1, Board.A8, Board.H8, Board.H1,
            Board.D3, Board.D6, Board.E3, Board.E6,
            Board.C4, Board.C5, Board.F4, Board.F5,
            Board.C3, Board.C6, Board.F3, Board.F6,
            Board.D2, Board.D7, Board.E2, Board.E7,
            Board.B4, Board.B5, Board.G4, Board.G5,
            Board.C2, Board.C7, Board.F2, Board.F7,
            Board.B3, Board.B6, Board.G3, Board.G6,
            Board.D1, Board.D8, Board.E1, Board.E8,
            Board.A4, Board.A5, Board.H4, Board.H5,
            Board.C1, Board.C8, Board.F1, Board.F8,
            Board.A3, Board.A6, Board.H3, Board.H6,
            Board.B2, Board.B7, Board.G2, Board.G7,
            Board.B1, Board.B8, Board.G1, Board.G8,
            Board.A2, Board.A7, Board.H2, Board.H7,
            Board.D4, Board.D5, Board.E4, Board.E5
        };
        int id = 0;
        MoveList prev = mMoves[0];
        MoveList current;
        prev.mPos = Board.NOMOVE;
        prev.mPrev = null;
        prev.mNext = null;
        for (int i = 0; i < list.length; i++) {
            if (mBoard.getDisk(list[i]) == Board.EMPTY) {
                id++;
                current = mMoves[id];
                current.mPos = list[i];
                current.mPrev = prev;
                current.mNext = null;
                prev.mNext = current;
                prev = current;
            }
        }
    }
}
