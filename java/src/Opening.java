import java.io.IOException;
import java.io.InputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class Opening {

    private static class Key {
        public long mBlack;
        public long mWhite;
        public Key() {
            mBlack = 0;
            mWhite = 0;
        }

        public int compare(Key key) {
            if (mWhite > key.mWhite) {
                return 1;
            } else if (mWhite < key.mWhite) {
                return -1;
            } else if (mBlack > key.mBlack) {
                return 1;
            } else if (mBlack < key.mBlack) {
                return -1;
            }
            return 0;
        }

        public void copy(Key key) {
            mBlack = key.mBlack;
            mWhite = key.mWhite;
        }
    }
    private static class Info {
        public long mBlack;
        public long mWhite;
        public int mValue;

        public boolean equalsKey(Key key) {
            if (mBlack == key.mBlack && mWhite == key.mWhite) {
                return true;
            }
            return false;
        }
    }

    private Info[] mInfo;

    public Opening() {
        mInfo = new Info[0];
    }

    public boolean load(String fileName) {
        InputStream is = null;
        byte[] buffer = new byte[20 * 100];
        ByteBuffer byteBuffer;
        int size;
        try {
            is = getClass().getResourceAsStream(fileName);
            size = is.read(buffer, 0, 4);
            if (size < 4) {
                return false;
            }
            byteBuffer = ByteBuffer.wrap(buffer);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            int count = byteBuffer.getInt();
            Info[] info = new Info[count];
            size = 0;
            for (int i = 0; i < count; i++) {
                if (size == 0) {
                    size = is.read(buffer);
                    byteBuffer = ByteBuffer.wrap(buffer);
                    byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                } else if (size < 20) {
                	for (int n = 0; n < size; n++) {
                		buffer[n] = byteBuffer.get();
                	}
                	size += is.read(buffer, size, buffer.length - size);
                    byteBuffer = ByteBuffer.wrap(buffer);
                    byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                }
                info[i] = new Info();
                info[i].mBlack = byteBuffer.getLong();
                info[i].mWhite = byteBuffer.getLong();
                info[i].mValue = byteBuffer.getInt();
                size -= 20;
            }
            mInfo = info;
            return true;
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
    }
    public Integer get(Board board, int color) {
        Key key = calcKey(board, color);
        for (int i = 0; i < mInfo.length; i++) {
            if (mInfo[i].equalsKey(key)) {
                return new Integer(mInfo[i].mValue);
            }
        }
        return null;
    }

    private static int rotatePos(int x, int y, int rotate) {
        switch (rotate) {
        case 0:
            return Board.getPos(x, y);
        case 1:
            return Board.getPos(Board.SIZE - x - 1, y);
        case 2:
            return Board.getPos(x, Board.SIZE - y - 1);
        case 3:
            return Board.getPos(Board.SIZE - x - 1, Board.SIZE - y - 1);
        case 4:
            return Board.getPos(y, x);
        case 5:
            return Board.getPos(Board.SIZE - y - 1, x);
        case 6:
            return Board.getPos(y, Board.SIZE - x - 1);
        case 7:
            return Board.getPos(Board.SIZE - y - 1, Board.SIZE - x - 1);
        default:
            break;
        }
        return Board.getPos(x, y);
    }

    private static Key calcKey(Board board, int color) {
        Key resultKey = new Key();
        Key key = new Key();
        
        int op = Board.getOpponent(color);
        for (int rotate = 0; rotate < 8; rotate++) {
        	key.mBlack = key.mWhite = 0;
            long flag = 1;
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    int disk = board.getDisk(rotatePos(i, j, rotate));
                    if (disk == color) {
                        key.mBlack |= flag;
                    } else if (disk == op) {
                        key.mWhite |= flag;
                    }
                    flag <<= 1;
                }
            }
            if (rotate == 0 || key.compare(resultKey) < 0) {
                resultKey.copy(key);
            }
        }
        return resultKey;
    }
}
