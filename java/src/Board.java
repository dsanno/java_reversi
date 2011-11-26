import java.util.Random;


public class Board {
    // 盤面の大きさ
    public static final int SIZE = 8;

    // マスの状態
    public static final int WALL = -1;
    public static final int EMPTY = 0;
    public static final int BLACK = 1;
    public static final int WHITE = 2;

    // マスの位置または手の種類
    public static final int PASS = -1;
    public static final int NOMOVE = -2;

    public static final int A1 = 10;
    public static final int B1 = 11;
    public static final int C1 = 12;
    public static final int D1 = 13;
    public static final int E1 = 14;
    public static final int F1 = 15;
    public static final int G1 = 16;
    public static final int H1 = 17;

    public static final int A2 = 19;
    public static final int B2 = 20;
    public static final int C2 = 21;
    public static final int D2 = 22;
    public static final int E2 = 23;
    public static final int F2 = 24;
    public static final int G2 = 25;
    public static final int H2 = 26;

    public static final int A3 = 28;
    public static final int B3 = 29;
    public static final int C3 = 30;
    public static final int D3 = 31;
    public static final int E3 = 32;
    public static final int F3 = 33;
    public static final int G3 = 34;
    public static final int H3 = 35;

    public static final int A4 = 37;
    public static final int B4 = 38;
    public static final int C4 = 39;
    public static final int D4 = 40;
    public static final int E4 = 41;
    public static final int F4 = 42;
    public static final int G4 = 43;
    public static final int H4 = 44;

    public static final int A5 = 46;
    public static final int B5 = 47;
    public static final int C5 = 48;
    public static final int D5 = 49;
    public static final int E5 = 50;
    public static final int F5 = 51;
    public static final int G5 = 52;
    public static final int H5 = 53;

    public static final int A6 = 55;
    public static final int B6 = 56;
    public static final int C6 = 57;
    public static final int D6 = 58;
    public static final int E6 = 59;
    public static final int F6 = 60;
    public static final int G6 = 61;
    public static final int H6 = 62;

    public static final int A7 = 64;
    public static final int B7 = 65;
    public static final int C7 = 66;
    public static final int D7 = 67;
    public static final int E7 = 68;
    public static final int F7 = 69;
    public static final int G7 = 70;
    public static final int H7 = 71;

    public static final int A8 = 73;
    public static final int B8 = 74;
    public static final int C8 = 75;
    public static final int D8 = 76;
    public static final int E8 = 77;
    public static final int F8 = 78;
    public static final int G8 = 79;
    public static final int H8 = 80;

    // パターンID
    public static final int PATTERN_ID_LINE_4_1 = 0;
    public static final int PATTERN_ID_LINE_4_2 = 1;
    public static final int PATTERN_ID_LINE_4_3 = 2;
    public static final int PATTERN_ID_LINE_4_4 = 3;
    public static final int PATTERN_ID_LINE_3_1 = 4;
    public static final int PATTERN_ID_LINE_3_2 = 5;
    public static final int PATTERN_ID_LINE_3_3 = 6;
    public static final int PATTERN_ID_LINE_3_4 = 7;
    public static final int PATTERN_ID_LINE_2_1 = 8;
    public static final int PATTERN_ID_LINE_2_2 = 9;
    public static final int PATTERN_ID_LINE_2_3 = 10;
    public static final int PATTERN_ID_LINE_2_4 = 11;
    public static final int PATTERN_ID_DIAG_8_1 = 12;
    public static final int PATTERN_ID_DIAG_8_2 = 13;
    public static final int PATTERN_ID_DIAG_7_1 = 14;
    public static final int PATTERN_ID_DIAG_7_2 = 15;
    public static final int PATTERN_ID_DIAG_7_3 = 16;
    public static final int PATTERN_ID_DIAG_7_4 = 17;
    public static final int PATTERN_ID_DIAG_6_1 = 18;
    public static final int PATTERN_ID_DIAG_6_2 = 19;
    public static final int PATTERN_ID_DIAG_6_3 = 20;
    public static final int PATTERN_ID_DIAG_6_4 = 21;
    public static final int PATTERN_ID_DIAG_5_1 = 22;
    public static final int PATTERN_ID_DIAG_5_2 = 23;
    public static final int PATTERN_ID_DIAG_5_3 = 24;
    public static final int PATTERN_ID_DIAG_5_4 = 25;
    public static final int PATTERN_ID_DIAG_4_1 = 26;
    public static final int PATTERN_ID_DIAG_4_2 = 27;
    public static final int PATTERN_ID_DIAG_4_3 = 28;
    public static final int PATTERN_ID_DIAG_4_4 = 29;
    public static final int PATTERN_ID_EDGE_10_1 = 30;
    public static final int PATTERN_ID_EDGE_10_2 = 31;
    public static final int PATTERN_ID_EDGE_10_3 = 32;
    public static final int PATTERN_ID_EDGE_10_4 = 33;
    public static final int PATTERN_ID_CORNER_9_1 = 34;
    public static final int PATTERN_ID_CORNER_9_2 = 35;
    public static final int PATTERN_ID_CORNER_9_3 = 36;
    public static final int PATTERN_ID_CORNER_9_4 = 37;
    public static final int NUM_PATTERN_ID         = 38;

    private static final int NUM_DISK = (SIZE + 1) * (SIZE + 2) + 1;
    private static final int NUM_STACK = ((SIZE-2)*3+3)*SIZE*SIZE;
    private static final int NUM_PATTERN_DIFF = 6;

    private static final int DIR_UP_LEFT = -SIZE - 2;
    private static final int DIR_UP = -SIZE - 1;
    private static final int DIR_UP_RIGHT = -SIZE;
    private static final int DIR_LEFT = -1;
    private static final int DIR_RIGHT = 1;
    private static final int DIR_DOWN_LEFT = SIZE;
    private static final int DIR_DOWN = SIZE + 1;
    private static final int DIR_DOWN_RIGHT = SIZE + 2;

    private int[] mDisk = new int[NUM_DISK];
    private int[] mStack = new int[NUM_STACK];
    private int mStackPos = 0;
    private int[] mDiskNum = new int[3];
    private int[] mPattern = new int[NUM_PATTERN_ID];

    private static boolean sStaticInfomationInitialized = false;
    private static int[][] sPatternID = new int[NUM_DISK][NUM_PATTERN_DIFF];
    private static int[][] sPatternDiff = new int[NUM_DISK][NUM_PATTERN_DIFF];
    private static long[] sHashDiffBlack = new long[NUM_DISK];
    private static long[] sHashDiffWhite = new long[NUM_DISK];
    private static long sHashDiffTurn;

    public Board() {
        if (!sStaticInfomationInitialized) {
            initializePatternDiff();
            initializeHashDiff();
            sStaticInfomationInitialized = true;
        }
        clear();
    }

    public void clear() {
        int i, j;
        for (i = 0; i < NUM_DISK; i++) {
            mDisk[i] = WALL;
        }
        for (i = 0; i < SIZE; i++) {
            for (j = 0; j < SIZE; j++) {
                mDisk[getPos(i, j)] = EMPTY;
            }
        }
        mDisk[E4] = BLACK;
        mDisk[D5] = BLACK;
        mDisk[D4] = WHITE;
        mDisk[E5] = WHITE;

        mStackPos = 0;
        mDiskNum[BLACK] = 2;
        mDiskNum[WHITE] = 2;
        mDiskNum[EMPTY] = SIZE * SIZE - 4;

        initializePattern();
    }

    public int getDisk(int pos) {
        return mDisk[pos];
    }

    public int countDisks(int color) {
        return mDiskNum[color];
    }

    public int flip(int color, int pos) {
        int result = 0;

        if (mDisk[pos] != EMPTY) {
            return 0;
        }
        switch (pos) {
        case C1:
        case C2:
        case D1:
        case D2:
        case E1:
        case E2:
        case F1:
        case F2:
            result += flipLine(color, pos, DIR_LEFT);
            result += flipLine(color, pos, DIR_RIGHT);
            result += flipLine(color, pos, DIR_DOWN_LEFT);
            result += flipLine(color, pos, DIR_DOWN);
            result += flipLine(color, pos, DIR_DOWN_RIGHT);
            break;
        case C8:
        case C7:
        case D8:
        case D7:
        case E8:
        case E7:
        case F8:
        case F7:
            result += flipLine(color, pos, DIR_UP_LEFT);
            result += flipLine(color, pos, DIR_UP);
            result += flipLine(color, pos, DIR_UP_RIGHT);
            result += flipLine(color, pos, DIR_LEFT);
            result += flipLine(color, pos, DIR_RIGHT);
            break;
        case A3:
        case A4:
        case A5:
        case A6:
        case B3:
        case B4:
        case B5:
        case B6:
            result += flipLine(color, pos, DIR_UP);
            result += flipLine(color, pos, DIR_UP_RIGHT);
            result += flipLine(color, pos, DIR_RIGHT);
            result += flipLine(color, pos, DIR_DOWN);
            result += flipLine(color, pos, DIR_DOWN_RIGHT);
            break;
        case H3:
        case H4:
        case H5:
        case H6:
        case G3:
        case G4:
        case G5:
        case G6:
            result += flipLine(color, pos, DIR_UP_LEFT);
            result += flipLine(color, pos, DIR_UP);
            result += flipLine(color, pos, DIR_LEFT);
            result += flipLine(color, pos, DIR_DOWN_LEFT);
            result += flipLine(color, pos, DIR_DOWN);
            break;
        case A1:
        case A2:
        case B1:
        case B2:
            result += flipLine(color, pos, DIR_RIGHT);
            result += flipLine(color, pos, DIR_DOWN);
            result += flipLine(color, pos, DIR_DOWN_RIGHT);
            break;
        case A8:
        case A7:
        case B8:
        case B7:
            result += flipLine(color, pos, DIR_UP);
            result += flipLine(color, pos, DIR_UP_RIGHT);
            result += flipLine(color, pos, DIR_RIGHT);
            break;
        case H1:
        case H2:
        case G1:
        case G2:
            result += flipLine(color, pos, DIR_LEFT);
            result += flipLine(color, pos, DIR_DOWN_LEFT);
            result += flipLine(color, pos, DIR_DOWN);
            break;
        case H8:
        case H7:
        case G8:
        case G7:
            result += flipLine(color, pos, DIR_UP_LEFT);
            result += flipLine(color, pos, DIR_UP);
            result += flipLine(color, pos, DIR_LEFT);
            break;
        default:
            result += flipLine(color, pos, DIR_UP_LEFT);
            result += flipLine(color, pos, DIR_UP);
            result += flipLine(color, pos, DIR_UP_RIGHT);
            result += flipLine(color, pos, DIR_LEFT);
            result += flipLine(color, pos, DIR_RIGHT);
            result += flipLine(color, pos, DIR_DOWN_LEFT);
            result += flipLine(color, pos, DIR_DOWN);
            result += flipLine(color, pos, DIR_DOWN_RIGHT);
            break;
        }
        if (result > 0) {
            mDisk[pos] = color;
            pushStack(pos);
            pushStack(getOpponent(color));
            pushStack(result);
            mDiskNum[color] += result + 1;
            mDiskNum[getOpponent(color)] -= result;
            mDiskNum[EMPTY]--;
        }
        return result;
    }

    public int unflip() {
        int result;
        int i, color;
        if (mStackPos <= 0) {
            return 0;
        }
        result = popStack();
        color = popStack();
        mDisk[popStack()] = EMPTY;
        for (i = 0; i < result; i++) {
            mDisk[popStack()] = color;
        }
        mDiskNum[color] += result;
        mDiskNum[getOpponent(color)] -= result + 1;
        mDiskNum[EMPTY]++;
        return result;
    }

    public boolean canFlip(int color, int pos) {
        if (mDisk[pos] != EMPTY) {
            return false;
        }
        if (countFlipsLine(color, pos, DIR_UP_LEFT) > 0) {
            return true;
        }
        if (countFlipsLine(color, pos, DIR_UP) > 0) {
            return true;
        }
        if (countFlipsLine(color, pos, DIR_UP_RIGHT) > 0) {
            return true;
        }
        if (countFlipsLine(color, pos, DIR_LEFT) > 0) {
            return true;
        }
        if (countFlipsLine(color, pos, DIR_RIGHT) > 0) {
            return true;
        }
        if (countFlipsLine(color, pos, DIR_DOWN_LEFT) > 0) {
            return true;
        }
        if (countFlipsLine(color, pos, DIR_DOWN) > 0) {
            return true;
        }
        if (countFlipsLine(color, pos, DIR_DOWN_RIGHT) > 0) {
            return true;
        }
        return false;
    }

    public int countFlips(int color, int pos) {
           int result = 0;

        if (mDisk[pos] != EMPTY) {
            return 0;
        }
        switch (pos) {
        case C1:
        case C2:
        case D1:
        case D2:
        case E1:
        case E2:
        case F1:
        case F2:
            result += countFlipsLine(color, pos, DIR_LEFT);
            result += countFlipsLine(color, pos, DIR_RIGHT);
            result += countFlipsLine(color, pos, DIR_DOWN_LEFT);
            result += countFlipsLine(color, pos, DIR_DOWN);
            result += countFlipsLine(color, pos, DIR_DOWN_RIGHT);
            break;
        case C8:
        case C7:
        case D8:
        case D7:
        case E8:
        case E7:
        case F8:
        case F7:
            result += countFlipsLine(color, pos, DIR_UP_LEFT);
            result += countFlipsLine(color, pos, DIR_UP);
            result += countFlipsLine(color, pos, DIR_UP_RIGHT);
            result += countFlipsLine(color, pos, DIR_LEFT);
            result += countFlipsLine(color, pos, DIR_RIGHT);
            break;
        case A3:
        case A4:
        case A5:
        case A6:
        case B3:
        case B4:
        case B5:
        case B6:
            result += countFlipsLine(color, pos, DIR_UP);
            result += countFlipsLine(color, pos, DIR_UP_RIGHT);
            result += countFlipsLine(color, pos, DIR_RIGHT);
            result += countFlipsLine(color, pos, DIR_DOWN);
            result += countFlipsLine(color, pos, DIR_DOWN_RIGHT);
            break;
        case H3:
        case H4:
        case H5:
        case H6:
        case G3:
        case G4:
        case G5:
        case G6:
            result += countFlipsLine(color, pos, DIR_UP_LEFT);
            result += countFlipsLine(color, pos, DIR_UP);
            result += countFlipsLine(color, pos, DIR_LEFT);
            result += countFlipsLine(color, pos, DIR_DOWN_LEFT);
            result += countFlipsLine(color, pos, DIR_DOWN);
            break;
        case A1:
        case A2:
        case B1:
        case B2:
            result += countFlipsLine(color, pos, DIR_RIGHT);
            result += countFlipsLine(color, pos, DIR_DOWN);
            result += countFlipsLine(color, pos, DIR_DOWN_RIGHT);
            break;
        case A8:
        case A7:
        case B8:
        case B7:
            result += countFlipsLine(color, pos, DIR_UP);
            result += countFlipsLine(color, pos, DIR_UP_RIGHT);
            result += countFlipsLine(color, pos, DIR_RIGHT);
            break;
        case H1:
        case H2:
        case G1:
        case G2:
            result += countFlipsLine(color, pos, DIR_LEFT);
            result += countFlipsLine(color, pos, DIR_DOWN_LEFT);
            result += countFlipsLine(color, pos, DIR_DOWN);
            break;
        case H8:
        case H7:
        case G8:
        case G7:
            result += countFlipsLine(color, pos, DIR_UP_LEFT);
            result += countFlipsLine(color, pos, DIR_UP);
            result += countFlipsLine(color, pos, DIR_LEFT);
            break;
        default:
            result += countFlipsLine(color, pos, DIR_UP_LEFT);
            result += countFlipsLine(color, pos, DIR_UP);
            result += countFlipsLine(color, pos, DIR_UP_RIGHT);
            result += countFlipsLine(color, pos, DIR_LEFT);
            result += countFlipsLine(color, pos, DIR_RIGHT);
            result += countFlipsLine(color, pos, DIR_DOWN_LEFT);
            result += countFlipsLine(color, pos, DIR_DOWN);
            result += countFlipsLine(color, pos, DIR_DOWN_RIGHT);
            break;
        }
        return result;
    }

    public void initializePattern() {
        int i;
        for (i = 0; i < NUM_PATTERN_ID; i++) {
            mPattern[i] = 0;
        }
        for (i = 0; i < NUM_DISK; i++) {
            if (mDisk[i] == BLACK) {
                putSquareBlack(i);
            } else if (mDisk[i] == WHITE) {
                putSquareWhite(i);
            }
        }
    }

    public int getPattern(int id) {
        return mPattern[id];
    }

    public int flipPattern(int color, int pos) {
        int result = 0;
        FlippableSquare flippable;

        if (mDisk[pos] != EMPTY) {
            return 0;
        }
        if (color == BLACK) {
            flippable = new FlippableSquare() {
                public void flip(int pos) {
                    flipSquareBlack(pos);
                }
            };
        } else {
            flippable = new FlippableSquare() {
                public void flip(int pos) {
                    flipSquareWhite(pos);
                }
            };
        }
        switch (pos) {
        case C1:
        case C2:
        case D1:
        case D2:
        case E1:
        case E2:
        case F1:
        case F2:
            result += flipLinePattern(color, pos, DIR_LEFT, flippable);
            result += flipLinePattern(color, pos, DIR_RIGHT, flippable);
            result += flipLinePattern(color, pos, DIR_DOWN_LEFT, flippable);
            result += flipLinePattern(color, pos, DIR_DOWN, flippable);
            result += flipLinePattern(color, pos, DIR_DOWN_RIGHT, flippable);
            break;
        case C8:
        case C7:
        case D8:
        case D7:
        case E8:
        case E7:
        case F8:
        case F7:
            result += flipLinePattern(color, pos, DIR_UP_LEFT, flippable);
            result += flipLinePattern(color, pos, DIR_UP, flippable);
            result += flipLinePattern(color, pos, DIR_UP_RIGHT, flippable);
            result += flipLinePattern(color, pos, DIR_LEFT, flippable);
            result += flipLinePattern(color, pos, DIR_RIGHT, flippable);
            break;
        case A3:
        case A4:
        case A5:
        case A6:
        case B3:
        case B4:
        case B5:
        case B6:
            result += flipLinePattern(color, pos, DIR_UP, flippable);
            result += flipLinePattern(color, pos, DIR_UP_RIGHT, flippable);
            result += flipLinePattern(color, pos, DIR_RIGHT, flippable);
            result += flipLinePattern(color, pos, DIR_DOWN, flippable);
            result += flipLinePattern(color, pos, DIR_DOWN_RIGHT, flippable);
            break;
        case H3:
        case H4:
        case H5:
        case H6:
        case G3:
        case G4:
        case G5:
        case G6:
            result += flipLinePattern(color, pos, DIR_UP_LEFT, flippable);
            result += flipLinePattern(color, pos, DIR_UP, flippable);
            result += flipLinePattern(color, pos, DIR_LEFT, flippable);
            result += flipLinePattern(color, pos, DIR_DOWN_LEFT, flippable);
            result += flipLinePattern(color, pos, DIR_DOWN, flippable);
            break;
        case A1:
        case A2:
        case B1:
        case B2:
            result += flipLinePattern(color, pos, DIR_RIGHT, flippable);
            result += flipLinePattern(color, pos, DIR_DOWN, flippable);
            result += flipLinePattern(color, pos, DIR_DOWN_RIGHT, flippable);
            break;
        case A8:
        case A7:
        case B8:
        case B7:
            result += flipLinePattern(color, pos, DIR_UP, flippable);
            result += flipLinePattern(color, pos, DIR_UP_RIGHT, flippable);
            result += flipLinePattern(color, pos, DIR_RIGHT, flippable);
            break;
        case H1:
        case H2:
        case G1:
        case G2:
            result += flipLinePattern(color, pos, DIR_LEFT, flippable);
            result += flipLinePattern(color, pos, DIR_DOWN_LEFT, flippable);
            result += flipLinePattern(color, pos, DIR_DOWN, flippable);
            break;
        case H8:
        case H7:
        case G8:
        case G7:
            result += flipLinePattern(color, pos, DIR_UP_LEFT, flippable);
            result += flipLinePattern(color, pos, DIR_UP, flippable);
            result += flipLinePattern(color, pos, DIR_LEFT, flippable);
            break;
        default:
            result += flipLinePattern(color, pos, DIR_UP_LEFT, flippable);
            result += flipLinePattern(color, pos, DIR_UP, flippable);
            result += flipLinePattern(color, pos, DIR_UP_RIGHT, flippable);
            result += flipLinePattern(color, pos, DIR_LEFT, flippable);
            result += flipLinePattern(color, pos, DIR_RIGHT, flippable);
            result += flipLinePattern(color, pos, DIR_DOWN_LEFT, flippable);
            result += flipLinePattern(color, pos, DIR_DOWN, flippable);
            result += flipLinePattern(color, pos, DIR_DOWN_RIGHT, flippable);
            break;
        }
        if (result > 0) {
            if (color == BLACK) {
                putSquareBlack(pos);
            } else {
                putSquareWhite(pos);
            }
            pushStack(pos);
            pushStack(getOpponent(color));
            pushStack(result);
            mDiskNum[color] += result + 1;
            mDiskNum[getOpponent(color)] -= result;
            mDiskNum[EMPTY]--;
        }
        return result;
    }

    public int unflipPattern() {
        int result;
        int i, color;
        if (mStackPos <= 0) {
            return 0;
        }
        result = popStack();
        color = popStack();
        if (color == BLACK) {
            removeSquareWhite(popStack());
            for (i = 0; i < result; i++) {
                flipSquareBlack(popStack());
            }
        } else {
            removeSquareBlack(popStack());
            for (i = 0; i < result; i++) {
                flipSquareWhite(popStack());
            }
        }
        mDiskNum[color] += result;
        mDiskNum[getOpponent(color)] -= result + 1;
        mDiskNum[EMPTY]++;
        return result;
    }

    public long getHashValue(int color) {
        long result = 0;
        int i;
        for (i = 0; i < NUM_DISK; i++) {
            switch (mDisk[i]) {
            case BLACK:
                result ^= sHashDiffBlack[i];
                break;
            case WHITE:
                result ^= sHashDiffWhite[i];
                break;
            }
        }
        if (color == WHITE) {
            result ^= sHashDiffTurn;
        }
        return result;
    }

    public void copy(Board board) {
        int i;

        for (i = 0; i < mDisk.length; i++) {
            mDisk[i] = board.mDisk[i];
        }
        for (i = 0; i < mStack.length; i++) {
            mStack[i] = board.mStack[i];
        }
        board.mStackPos = mStackPos;
        for (i = 0; i < mDiskNum.length; i++) {
            mDiskNum[i] = board.mDiskNum[i];
        }
        for (i = 0; i < mPattern.length; i++) {
            mPattern[i] = board.mPattern[i];
        }
    }

    public void reverse() {
        int i;
        int n;

        for (i = 0; i < NUM_DISK; i++) {
            if (mDisk[i] == BLACK) {
                mDisk[i] = WHITE;
                mDiskNum[BLACK]--;
                mDiskNum[WHITE]++;
            } else if (mDisk[i] == WHITE) {
                mDisk[i] = BLACK;
                mDiskNum[WHITE]--;
                mDiskNum[BLACK]++;
            }
        }
        for (i = mStackPos; i > 0;) {
            i--;
            n = mStack[i];
            i--;
            mStack[i] = getOpponent(mStack[i]);
            i -= n + 1;
        }
        initializePattern();
    }

    public boolean canPlay(int color) {
        int i, j;
        for (i = 0; i < SIZE; i++) {
            for (j = 0; j < SIZE; j++) {
                if (canFlip(color, getPos(i, j))) {
                    return true;
                }
            }
        }
        return false;
    }

    public static int getPos(int x, int y) {
        return (y + 1) * (SIZE + 1) + x + 1;
    }

    public static int getX(int pos) {
        return pos % (SIZE + 1) - 1;
    }

    public static int getY(int pos) {
        return pos / (SIZE + 1) - 1;
    }

    public static int getOpponent(int color) {
        return BLACK + WHITE - color;
    }

    private int popStack() {
        mStackPos--;
        return mStack[mStackPos];
    }

    private void pushStack(int n) {
        mStack[mStackPos] = n;
        mStackPos++;
    }

    private int flipLine(int color, int pos, int dir) {
        int result = 0;
        int op = getOpponent(color);
        int p;

        p = pos + dir;
        if (mDisk[p] != op) {
            return 0;
        }
        p += dir;
        if (mDisk[p] == op) {
            p += dir;
            if (mDisk[p] == op) {
                p += dir;
                if (mDisk[p] == op) {
                    p += dir;
                    if (mDisk[p] == op) {
                        p += dir;
                        if (mDisk[p] == op) {
                            p += dir;
                            if (mDisk[p] != color) {
                                return 0;
                            }
                            p -= dir;
                            result++;
                            mDisk[p] = color;
                            pushStack(p);
                        } else if (mDisk[p] != color) {
                            return 0;
                        }
                        p -= dir;
                        result ++;
                        mDisk[p] = color;
                        pushStack(p);
                    } else if (mDisk[p] != color) {
                        return 0;
                    }
                    p -= dir;
                    result ++;
                    mDisk[p] = color;
                    pushStack(p);
                } else if (mDisk[p] != color) {
                    return 0;
                }
                p -= dir;
                result ++;
                mDisk[p] = color;
                pushStack(p);
            } else if (mDisk[p] != color) {
                return 0;
            }
            p -= dir;
            result ++;
            mDisk[p] = color;
            pushStack(p);
        } else if (mDisk[p] != color) {
            return 0;
        }
        p -= dir;
        result ++;
        mDisk[p] = color;
        pushStack(p);

        return result;
    }

    private int countFlipsLine(int color, int pos, int dir) {
        int result = 0;
        int op = getOpponent(color);
        int p;
        for (p = pos + dir; mDisk[p] == op; p += dir) {
            result++;
        }
        if (mDisk[p] != color) {
            return 0;
        }
        return result;
    }

    private static interface FlippableSquare {
        public void flip(int pos);
    }

    private int flipLinePattern(int color, int pos, int dir, FlippableSquare flippable) {
        int result = 0;
        int op = getOpponent(color);
        int p;

        p = pos + dir;
        if (mDisk[p] != op) {
            return 0;
        }
        p += dir;
        if (mDisk[p] == op) {
            p += dir;
            if (mDisk[p] == op) {
                p += dir;
                if (mDisk[p] == op) {
                    p += dir;
                    if (mDisk[p] == op) {
                        p += dir;
                        if (mDisk[p] == op) {
                            p += dir;
                            if (mDisk[p] != color) {
                                return 0;
                            }
                            p -= dir;
                            result++;
                            flippable.flip(p);
                            pushStack(p);
                        } else if (mDisk[p] != color) {
                            return 0;
                        }
                        p -= dir;
                        result ++;
                        flippable.flip(p);
                        pushStack(p);
                    } else if (mDisk[p] != color) {
                        return 0;
                    }
                    p -= dir;
                    result ++;
                    flippable.flip(p);
                    pushStack(p);
                } else if (mDisk[p] != color) {
                    return 0;
                }
                p -= dir;
                result ++;
                flippable.flip(p);
                pushStack(p);
            } else if (mDisk[p] != color) {
                return 0;
            }
            p -= dir;
            result ++;
            flippable.flip(p);
            pushStack(p);
        } else if (mDisk[p] != color) {
            return 0;
        }
        p -= dir;
        result ++;
        flippable.flip(p);
        pushStack(p);

        return result;
    }

    private static void addPattern(int id, int[] posList) {
        int i, j, n;
        n = 1;
        for (i = 0; i < posList.length; i++) {
            for (j = 0; sPatternDiff[posList[i]][j] != 0; j++) {
            }
            sPatternID[posList[i]][j] = id;
            sPatternDiff[posList[i]][j] = n;
            n *= 3;            
        }
    }

    private static void initializePatternDiff() {
        int i, j;
        final int[][] patternList = new int[][]{
            { A4, B4, C4, D4, E4, F4, G4, H4 },
            { A5, B5, C5, D5, E5, F5, G5, H5 },
            { D1, D2, D3, D4, D5, D6, D7, D8 },
            { E1, E2, E3, E4, E5, E6, E7, E8 },
            { A3, B3, C3, D3, E3, F3, G3, H3 },
            { A6, B6, C6, D6, E6, F6, G6, H6 },
            { C1, C2, C3, C4, C5, C6, C7, C8 },
            { F1, F2, F3, F4, F5, F6, F7, F8 },
            { A2, B2, C2, D2, E2, F2, G2, H2 },
            { A7, B7, C7, D7, E7, F7, G7, H7 },
            { B1, B2, B3, B4, B5, B6, B7, B8 },
            { G1, G2, G3, G4, G5, G6, G7, G8 },
            { A1, B2, C3, D4, E5, F6, G7, H8 },
            { A8, B7, C6, D5, E4, F3, G2, H1 },
            { A2, B3, C4, D5, E6, F7, G8 },
            { B1, C2, D3, E4, F5, G6, H7 },
            { A7, B6, C5, D4, E3, F2, G1 },
            { B8, C7, D6, E5, F4, G3, H2 },
            { A3, B4, C5, D6, E7, F8 },
            { C1, D2, E3, F4, G5, H6 },
            { A6, B5, C4, D3, E2, F1 },
            { C8, D7, E6, F5, G4, H3 },
            { A4, B5, C6, D7, E8 },
            { D1, E2, F3, G4, H5 },
            { A5, B4, C3, D2, E1 },
            { D8, E7, F6, G5, H4 },
            { A5, B6, C7, D8 },
            { E1, F2, G3, H4 },
            { A4, B3, C2, D1 },
            { E8, F7, G6, H5 },
            { B2, A1, B1, C1, D1, E1, F1, G1, H1, G2 },
            { B7, A8, B8, C8, D8, E8, F8, G8, H8, G7 },
            { B2, A1, A2, A3, A4, A5, A6, A7, A8, B7 },
            { G2, H1, H2, H3, H4, H5, H6, H7, H8, G7 },
            { C3, B3, A3, C2, B2, A2, C1, B1, A1 },
            { F3, G3, H3, F2, G2, H2, F1, G1, H1 },
            { C6, B6, A6, C7, B7, A7, C8, B8, A8 },
            { F6, G6, H6, F7, G7, H7, F8, G8, H8 },
        };

        for (i = 0; i < NUM_DISK; i++) {
            for (j = 0; j < NUM_PATTERN_DIFF; j++) {
                sPatternID[i][j] = 0;
                sPatternDiff[i][j] = 0;
            }
        }
        for (i = 0; i < patternList.length; i++) {
            addPattern(i, patternList[i]);
        }
    }

    private static void initializeHashDiff() {
        Random random = new Random();
        sHashDiffTurn = random.nextLong();
        for (int i = 0; i < NUM_DISK; i++) {
            sHashDiffBlack[i] = random.nextLong();
            sHashDiffWhite[i] = random.nextLong();
        }
    }

    private void flipSquareBlack(int pos) {
        mDisk[pos] = BLACK;
        mPattern[sPatternID[pos][0]] -= sPatternDiff[pos][0];
        mPattern[sPatternID[pos][1]] -= sPatternDiff[pos][1];
        mPattern[sPatternID[pos][2]] -= sPatternDiff[pos][2];
        mPattern[sPatternID[pos][3]] -= sPatternDiff[pos][3];
        mPattern[sPatternID[pos][4]] -= sPatternDiff[pos][4];
        mPattern[sPatternID[pos][5]] -= sPatternDiff[pos][5];
    }

    private void flipSquareWhite(int pos) {
        mDisk[pos] = WHITE;
        mPattern[sPatternID[pos][0]] += sPatternDiff[pos][0];
        mPattern[sPatternID[pos][1]] += sPatternDiff[pos][1];
        mPattern[sPatternID[pos][2]] += sPatternDiff[pos][2];
        mPattern[sPatternID[pos][3]] += sPatternDiff[pos][3];
        mPattern[sPatternID[pos][4]] += sPatternDiff[pos][4];
        mPattern[sPatternID[pos][5]] += sPatternDiff[pos][5];
    }

    private void putSquareBlack(int pos) {
        mDisk[pos] = BLACK;
        mPattern[sPatternID[pos][0]] += sPatternDiff[pos][0];
        mPattern[sPatternID[pos][1]] += sPatternDiff[pos][1];
        mPattern[sPatternID[pos][2]] += sPatternDiff[pos][2];
        mPattern[sPatternID[pos][3]] += sPatternDiff[pos][3];
        mPattern[sPatternID[pos][4]] += sPatternDiff[pos][4];
        mPattern[sPatternID[pos][5]] += sPatternDiff[pos][5];
    }

    private void putSquareWhite(int pos) {
        mDisk[pos] = WHITE;
        mPattern[sPatternID[pos][0]] += sPatternDiff[pos][0] + sPatternDiff[pos][0];
        mPattern[sPatternID[pos][1]] += sPatternDiff[pos][1] + sPatternDiff[pos][1];
        mPattern[sPatternID[pos][2]] += sPatternDiff[pos][2] + sPatternDiff[pos][2];
        mPattern[sPatternID[pos][3]] += sPatternDiff[pos][3] + sPatternDiff[pos][3];
        mPattern[sPatternID[pos][4]] += sPatternDiff[pos][4] + sPatternDiff[pos][4];
        mPattern[sPatternID[pos][5]] += sPatternDiff[pos][5] + sPatternDiff[pos][5];
    }

    private void removeSquareBlack(int pos) {
        mDisk[pos] = EMPTY;
        mPattern[sPatternID[pos][0]] -= sPatternDiff[pos][0];
        mPattern[sPatternID[pos][1]] -= sPatternDiff[pos][1];
        mPattern[sPatternID[pos][2]] -= sPatternDiff[pos][2];
        mPattern[sPatternID[pos][3]] -= sPatternDiff[pos][3];
        mPattern[sPatternID[pos][4]] -= sPatternDiff[pos][4];
        mPattern[sPatternID[pos][5]] -= sPatternDiff[pos][5];
    }

    private void removeSquareWhite(int pos) {
        mDisk[pos] = EMPTY;
        mPattern[sPatternID[pos][0]] -= sPatternDiff[pos][0] + sPatternDiff[pos][0];
        mPattern[sPatternID[pos][1]] -= sPatternDiff[pos][1] + sPatternDiff[pos][1];
        mPattern[sPatternID[pos][2]] -= sPatternDiff[pos][2] + sPatternDiff[pos][2];
        mPattern[sPatternID[pos][3]] -= sPatternDiff[pos][3] + sPatternDiff[pos][3];
        mPattern[sPatternID[pos][4]] -= sPatternDiff[pos][4] + sPatternDiff[pos][4];
        mPattern[sPatternID[pos][5]] -= sPatternDiff[pos][5] + sPatternDiff[pos][5];
    }
}
