import java.io.IOException;
import java.io.InputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Evaluator {
    public static final int DISK_VALUE = 100;
    
    private static final int POW_3_0 = 1;
    private static final int POW_3_1 = 3;
    private static final int POW_3_2 = 9;
    private static final int POW_3_3 = 27;
    private static final int POW_3_4 = 81;
    private static final int POW_3_5 = 243;
    private static final int POW_3_6 = 729;
    private static final int POW_3_7 = 2187;
    private static final int POW_3_8 = 6561;
    private static final int POW_3_9 = 19683;
    private static final int POW_3_10 = 59049;
    private static final int PATTERN_ID_LINE_4 = 0;
    private static final int PATTERN_ID_LINE_3 = 1;
    private static final int PATTERN_ID_LINE_2 = 2;
    private static final int PATTERN_ID_DIAG_8 = 3;
    private static final int PATTERN_ID_DIAG_7 = 4;
    private static final int PATTERN_ID_DIAG_6 = 5;
    private static final int PATTERN_ID_DIAG_5 = 6;
    private static final int PATTERN_ID_DIAG_4 = 7;
    private static final int PATTERN_ID_EDGE_10 = 8;
    private static final int PATTERN_ID_CORNER_9 = 9;
    private static final int PATTERN_ID_PARITY = 10;
    private static final int[] STAGE_MIN_EMPTY_COUNT = new int[]{
        44, 24, 0
    };
    private static final int[] PATTERN_COUNT = new int[]{
        POW_3_8,
        POW_3_8,
        POW_3_8,
        POW_3_8,
        POW_3_7,
        POW_3_6,
        POW_3_5,
        POW_3_4,
        POW_3_10,
        POW_3_9,
        2,
    };

    private int[][] mCurrentValue;
    private int[][][] mValue;

    public Evaluator() {
        int i, j;
        mValue = new int[STAGE_MIN_EMPTY_COUNT.length][PATTERN_COUNT.length][];
        for (i = 0; i < STAGE_MIN_EMPTY_COUNT.length; i++) {
            for (j = 0; j < PATTERN_COUNT.length; j++) {
                mValue[i][j] = new int[PATTERN_COUNT[j]];
            }
        }
        mCurrentValue = mValue[0];
    }

    public boolean load(String fileName) {
        byte[] buffer = new byte[2048];
        ByteBuffer byteBuffer;
        int size = 0;
        int i, j, k;
    	InputStream is = getClass().getResourceAsStream(fileName);
        try {
        	size = is.read(buffer);
        	byteBuffer = ByteBuffer.wrap(buffer);
        	byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            for (i = 0; i < mValue.length; i++) {
                for (j = 0; j < mValue[i].length; j++) {
                    for (k = 0; k < mValue[i][j].length; k++) {
                        if (size == 0) {
                            size = is.read(buffer);
                            if (size <= 0) {
                                return false;
                            }
                            byteBuffer = ByteBuffer.wrap(buffer);
                            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                        } else if (size < 2) {
                        	for (int n = 0; n < size; n++) {
                        		buffer[n] = byteBuffer.get();
                        	}
                        	size += is.read(buffer, size, buffer.length - size);
                            byteBuffer = ByteBuffer.wrap(buffer);
                            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                        }
                        mValue[i][j][k] = (int)byteBuffer.getShort();
                        size -= 2;
                    }
                }
            }
        } catch (IOException e) {
            return false;
        } catch (BufferUnderflowException e) {
            return false;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ie) {
                }
            }
        }
        return true;
    }

    public int getValue(Board board) {
        int result = 0;

        result += mCurrentValue[PATTERN_ID_LINE_4][board.getPattern(Board.PATTERN_ID_LINE_4_1)];
        result += mCurrentValue[PATTERN_ID_LINE_4][board.getPattern(Board.PATTERN_ID_LINE_4_2)];
        result += mCurrentValue[PATTERN_ID_LINE_4][board.getPattern(Board.PATTERN_ID_LINE_4_3)];
        result += mCurrentValue[PATTERN_ID_LINE_4][board.getPattern(Board.PATTERN_ID_LINE_4_4)];
        result += mCurrentValue[PATTERN_ID_LINE_3][board.getPattern(Board.PATTERN_ID_LINE_3_1)];
        result += mCurrentValue[PATTERN_ID_LINE_3][board.getPattern(Board.PATTERN_ID_LINE_3_2)];
        result += mCurrentValue[PATTERN_ID_LINE_3][board.getPattern(Board.PATTERN_ID_LINE_3_3)];
        result += mCurrentValue[PATTERN_ID_LINE_3][board.getPattern(Board.PATTERN_ID_LINE_3_4)];
        result += mCurrentValue[PATTERN_ID_LINE_2][board.getPattern(Board.PATTERN_ID_LINE_2_1)];
        result += mCurrentValue[PATTERN_ID_LINE_2][board.getPattern(Board.PATTERN_ID_LINE_2_2)];
        result += mCurrentValue[PATTERN_ID_LINE_2][board.getPattern(Board.PATTERN_ID_LINE_2_3)];
        result += mCurrentValue[PATTERN_ID_LINE_2][board.getPattern(Board.PATTERN_ID_LINE_2_4)];
        result += mCurrentValue[PATTERN_ID_DIAG_8][board.getPattern(Board.PATTERN_ID_DIAG_8_1)];
        result += mCurrentValue[PATTERN_ID_DIAG_8][board.getPattern(Board.PATTERN_ID_DIAG_8_2)];
        result += mCurrentValue[PATTERN_ID_DIAG_7][board.getPattern(Board.PATTERN_ID_DIAG_7_1)];
        result += mCurrentValue[PATTERN_ID_DIAG_7][board.getPattern(Board.PATTERN_ID_DIAG_7_2)];
        result += mCurrentValue[PATTERN_ID_DIAG_7][board.getPattern(Board.PATTERN_ID_DIAG_7_3)];
        result += mCurrentValue[PATTERN_ID_DIAG_7][board.getPattern(Board.PATTERN_ID_DIAG_7_4)];
        result += mCurrentValue[PATTERN_ID_DIAG_6][board.getPattern(Board.PATTERN_ID_DIAG_6_1)];
        result += mCurrentValue[PATTERN_ID_DIAG_6][board.getPattern(Board.PATTERN_ID_DIAG_6_2)];
        result += mCurrentValue[PATTERN_ID_DIAG_6][board.getPattern(Board.PATTERN_ID_DIAG_6_3)];
        result += mCurrentValue[PATTERN_ID_DIAG_6][board.getPattern(Board.PATTERN_ID_DIAG_6_4)];
        result += mCurrentValue[PATTERN_ID_DIAG_5][board.getPattern(Board.PATTERN_ID_DIAG_5_1)];
        result += mCurrentValue[PATTERN_ID_DIAG_5][board.getPattern(Board.PATTERN_ID_DIAG_5_2)];
        result += mCurrentValue[PATTERN_ID_DIAG_5][board.getPattern(Board.PATTERN_ID_DIAG_5_3)];
        result += mCurrentValue[PATTERN_ID_DIAG_5][board.getPattern(Board.PATTERN_ID_DIAG_5_4)];
        result += mCurrentValue[PATTERN_ID_DIAG_4][board.getPattern(Board.PATTERN_ID_DIAG_4_1)];
        result += mCurrentValue[PATTERN_ID_DIAG_4][board.getPattern(Board.PATTERN_ID_DIAG_4_2)];
        result += mCurrentValue[PATTERN_ID_DIAG_4][board.getPattern(Board.PATTERN_ID_DIAG_4_3)];
        result += mCurrentValue[PATTERN_ID_DIAG_4][board.getPattern(Board.PATTERN_ID_DIAG_4_4)];
        result += mCurrentValue[PATTERN_ID_EDGE_10][board.getPattern(Board.PATTERN_ID_EDGE_10_1)];
        result += mCurrentValue[PATTERN_ID_EDGE_10][board.getPattern(Board.PATTERN_ID_EDGE_10_2)];
        result += mCurrentValue[PATTERN_ID_EDGE_10][board.getPattern(Board.PATTERN_ID_EDGE_10_3)];
        result += mCurrentValue[PATTERN_ID_EDGE_10][board.getPattern(Board.PATTERN_ID_EDGE_10_4)];
        result += mCurrentValue[PATTERN_ID_CORNER_9][board.getPattern(Board.PATTERN_ID_CORNER_9_1)];
        result += mCurrentValue[PATTERN_ID_CORNER_9][board.getPattern(Board.PATTERN_ID_CORNER_9_2)];
        result += mCurrentValue[PATTERN_ID_CORNER_9][board.getPattern(Board.PATTERN_ID_CORNER_9_3)];
        result += mCurrentValue[PATTERN_ID_CORNER_9][board.getPattern(Board.PATTERN_ID_CORNER_9_4)];
        /* parity */
        result += mCurrentValue[PATTERN_ID_PARITY][board.countDisks(Board.EMPTY) & 1];

        return result;        
    }

    public void setStage(int emptyCount) {
        mCurrentValue = mValue[getStageIndex(emptyCount)];
    }

    public static int getStageCount() {
        return STAGE_MIN_EMPTY_COUNT.length;
    }

    public static int getStageIndex(int emptyCount) {
        int i;
        for (i = 0; i < STAGE_MIN_EMPTY_COUNT.length - 1; i++) {
            if (emptyCount >= STAGE_MIN_EMPTY_COUNT[i]) {
                break;
            }
        }
        return i;
    }
}
