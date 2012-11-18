/*
 * Copyright (c) 2011 Daiki Sanno
 */

import java.applet.Applet;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class JDolphin extends Applet {
    private static final long serialVersionUID = 1L;
    private static final int[][] sLevelParam = new int[][] {
        {2, 4, 4, 4, 45},
        {2, 4, 4, 4, 40},
        {2, 4, 4, 3, 40},
        {2, 4, 4, 3, 35},
        {2, 4, 4, 3, 30},
        {2, 4, 4, 2, 40},
        {2, 4, 4, 2, 38},
        {2, 4, 4, 2, 33},
        {2, 4, 4, 2, 28},
        {2, 4, 4, 2, 23},
        {2, 4, 4, 2, 18},
        {2, 4, 4, 2, 13},
        {2, 4, 4, 2, 8},
        {2, 8, 8, 2, 8},
        {4, 4, 4, 2, 34},
        {4, 4, 4, 2, 29},
        {4, 4, 4, 2, 24},
        {4, 4, 4, 2, 20},
        {4, 4, 4, 2, 16},
        {4, 8, 8, 2, 20},
        {4, 8, 8, 2, 16},
        {4, 8, 8, 2, 12},
        {4, 8, 8, 2, 8},
        {4, 10, 12, 2, 8},
        {6, 12, 14, 2, 24},
        {6, 12, 14, 2, 20},
        {6, 12, 14, 2, 16},
        {6, 12, 14, 2, 12},
        {6, 12, 14, 2, 8},
        {8, 14, 16, 2, 16},
        {8, 14, 16, 2, 12},
        {10, 16, 18, 2, 16},
        {8, 14, 16, 2, 8},
        {10, 16, 18, 2, 12},
        {10, 16, 18, 2, 8},
        {10, 16, 18, 1, 8}
    };
    private static final int WIDTH = 480;
    private static final int HEIGHT = 480;
    private static final int PLAYER_NONE = Board.EMPTY;
    private static final int PLAYER_BLACK = Board.BLACK;
    private static final int PLAYER_WHITE = Board.WHITE;
    private static final int STATE_TITLE = 0;
    private static final int STATE_GAME = 1;    
    private Image mOffScreen = null;
    private int mPlayer;
    private int mLevel;
    private int mActivePlayer;
    private int mActiveLevel;
    private boolean mStartIsActive;
    private Board mBoard;
    private MouseListener mMouseListener;
    private MouseMotionListener mMouseMotionListener;
    private Com mCom;
    private int mState;

    public void init() {
        mPlayer = PLAYER_BLACK;
        mLevel = 0;
        mActivePlayer = PLAYER_NONE;
        mActiveLevel = -1;
        mOffScreen = createImage(WIDTH, HEIGHT);
        mMouseListener = null;
        mMouseMotionListener = null;
        mBoard = new Board();
        Evaluator evaluator = new Evaluator();
        if (!evaluator.load("eval.dat")) {
            showError("初期化に失敗しました", 11);
            return;
        }
        Opening opening = new Opening();
        if (!opening.load("open.dat")) {
            showError("初期化に失敗しました", 21);
            return;
        }
        mCom = new Com(evaluator, opening);

        startTitle();
    }

    public void destroy() {
        
    }

    public void start() {
        
    }

    public void stop() {
        
    }

    public void paint(Graphics g) {
           g.drawImage(mOffScreen, 0, 0, this);
    }

    public void update(Graphics g) {
        paint(g);
    }

    private void showError(String str, int code) {
        Graphics g = mOffScreen.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setColor(Color.BLACK);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, FONT_SIZE));
        g.drawString(str + "(" + code + ")", 20, 100);
    }

    private static final Color COLOR_BACKGROUND = Color.decode("#ffff99");
    private static final Color COLOR_TEXT = Color.BLACK;
    private static final Color COLOR_BOARD = Color.decode("#66cc66");
    private static final Color COLOR_BUTTON = Color.decode("#ffcc99");
    private static final int FONT_SIZE = 24;
    private static final int FONT_SIZE_LARGE = 36;
    private static final int TITLE_LABEL_COLOR_BASE = 50;
    private static final int TITLE_DISK_TOP = 85;
    private static final int TITLE_DISK_SIZE = 36;
    private static final int TITLE_DISK_SIZE_LARGE = 50;
    private static final int TITLE_DISK_SIZE_SELECTED = 46;
    private static final int TITLE_DISK_BLACK_LEFT = 160;
    private static final int TITLE_DISK_WHITE_LEFT = 320;
    private static final int TITLE_LABEL_LEVEL_BASE = 170;
    private static final int TITLE_LEVEL_LEFT = 80;
    private static final int TITLE_LEVEL_TOP = 200;
    private static final int TITLE_LEVEL_INTERVAL_X = 64;
    private static final int TITLE_LEVEL_INTERVAL_Y = 36;
    private static final int TITLE_LEVEL_SIZE_SELECTED = 40;
    private static final int TITLE_LEVEL_ROW_NUM = 6;
    private static final int TITLE_START_TOP = 440;
    private static final int TITLE_START_WIDTH = 220;
    private static final int BOARD_SQUARE_WIDTH = 40;
    private static final int BOARD_SQUARE_HEIGHT = 40;
    private static final int BOARD_DISK_SIZE = 32;
    private static final int BOARD_SCORE_DISK_SIZE = 24;
    private static final int BOARD_BOARD_LEFT = 80;
    private static final int BOARD_BOARD_TOP = 80;
    private static final int BOARD_TITLE_LEFT = 40;
    private static final int BOARD_TITLE_TOP = 4;
    private static final int BOARD_TITLE_WIDTH = 200;
    private static final int BOARD_TITLE_HEIGHT = 36;

    private void startTitle() {
        mState = STATE_TITLE;
        mActivePlayer = PLAYER_NONE;
        mActiveLevel = -1;
        mStartIsActive = false;
        removeMouseListener(mMouseListener);
        removeMouseMotionListener(mMouseMotionListener);
        mMouseListener = new MouseListener() {

            public void mouseClicked(MouseEvent arg0) {
            }

            public void mouseEntered(MouseEvent arg0) {
            }

            public void mouseExited(MouseEvent arg0) {
            }

            public void mousePressed(MouseEvent arg0) {
                int x = arg0.getX();
                int y = arg0.getY();
                int oldPlayer = mPlayer;
                int oldLevel = mLevel;
                if (x >= TITLE_DISK_BLACK_LEFT - TITLE_DISK_SIZE / 2 && x < TITLE_DISK_BLACK_LEFT + TITLE_DISK_SIZE / 2 &&
                        y >= TITLE_DISK_TOP - TITLE_DISK_SIZE / 2 && y < TITLE_DISK_TOP + TITLE_DISK_SIZE / 2) {
                    mPlayer = PLAYER_BLACK;
                } else if (x >= TITLE_DISK_WHITE_LEFT - TITLE_DISK_SIZE / 2 && x < TITLE_DISK_WHITE_LEFT + TITLE_DISK_SIZE / 2 &&
                        y >= TITLE_DISK_TOP - TITLE_DISK_SIZE / 2 && y < TITLE_DISK_TOP + TITLE_DISK_SIZE / 2) {
                    mPlayer = PLAYER_WHITE;
                }
                for (int i = 0; i < sLevelParam.length; i++) {
                    int levelX = TITLE_LEVEL_LEFT + (i % TITLE_LEVEL_ROW_NUM) * TITLE_LEVEL_INTERVAL_X;
                    int levelY = TITLE_LEVEL_TOP + (i / TITLE_LEVEL_ROW_NUM) * TITLE_LEVEL_INTERVAL_Y;
                    if (x >= levelX - FONT_SIZE / 2 && x < levelX + FONT_SIZE / 2 &&
                        y >= levelY - FONT_SIZE / 2 && y < levelY + FONT_SIZE / 2) {
                        mLevel = i;
                        break;
                    }
                }
                if (x >= (WIDTH - TITLE_START_WIDTH) / 2 && x < (WIDTH + TITLE_START_WIDTH) / 2 &&
                       y >= TITLE_START_TOP - FONT_SIZE / 2 && y < TITLE_START_TOP + FONT_SIZE / 2) {
                    startGame();
                } else if (oldPlayer != mPlayer || oldLevel != mLevel) {
                    paintTitle();
                }
            }

            public void mouseReleased(MouseEvent arg0) {
            }
            
        };
        mMouseMotionListener = new MouseMotionListener() {
            public void mouseDragged(MouseEvent arg0) {                
            }

            public void mouseMoved(MouseEvent arg0) {
                int x = arg0.getX();
                int y = arg0.getY();
                int oldActivePlayer = mActivePlayer;
                int oldActiveLevel = mActiveLevel;
                boolean oldStartIsActive = mStartIsActive;
                mActivePlayer = PLAYER_NONE;
                mActiveLevel = -1;
                mStartIsActive = false;
                if (x >= TITLE_DISK_BLACK_LEFT - TITLE_DISK_SIZE / 2 && x < TITLE_DISK_BLACK_LEFT + TITLE_DISK_SIZE / 2 &&
                    y >= TITLE_DISK_TOP - TITLE_DISK_SIZE / 2 && y < TITLE_DISK_TOP + TITLE_DISK_SIZE / 2) {
                    mActivePlayer = PLAYER_BLACK;
                } else if (x >= TITLE_DISK_WHITE_LEFT - TITLE_DISK_SIZE / 2 && x < TITLE_DISK_WHITE_LEFT + TITLE_DISK_SIZE / 2 &&
                    y >= TITLE_DISK_TOP - TITLE_DISK_SIZE / 2 && y < TITLE_DISK_TOP + TITLE_DISK_SIZE / 2) {
                    mActivePlayer = PLAYER_WHITE;
                }
                for (int i = 0; i < sLevelParam.length; i++) {
                    int levelX = TITLE_LEVEL_LEFT + (i % TITLE_LEVEL_ROW_NUM) * TITLE_LEVEL_INTERVAL_X;
                    int levelY = TITLE_LEVEL_TOP + (i / TITLE_LEVEL_ROW_NUM) * TITLE_LEVEL_INTERVAL_Y;
                    if (x >= levelX - FONT_SIZE / 2 && x < levelX + FONT_SIZE / 2 &&
                        y >= levelY - FONT_SIZE / 2 && y < levelY + FONT_SIZE / 2) {
                        mActiveLevel = i;
                        break;
                    }
                }
                if (x >= (WIDTH - TITLE_START_WIDTH) / 2 && x < (WIDTH + TITLE_START_WIDTH) / 2 &&
                    y >= TITLE_START_TOP - FONT_SIZE / 2 && y < TITLE_START_TOP + FONT_SIZE / 2) {
                    mStartIsActive = true;
                }
                if (oldActivePlayer != mActivePlayer || oldActiveLevel != mActiveLevel ||
                    oldStartIsActive != mStartIsActive) {
                    paintTitle();
                }
            }
        };
        addMouseListener(mMouseListener);
        addMouseMotionListener(mMouseMotionListener);
        paintTitle();
    }

    private void paintTitle() {
        Graphics g;
        int stringWidth;
        String str;
        Font font = new Font(Font.SANS_SERIF, Font.PLAIN, FONT_SIZE);
        Font largeFont = new Font(Font.SANS_SERIF, Font.PLAIN, FONT_SIZE_LARGE);
        // background
        g = mOffScreen.getGraphics();
        g.setColor(COLOR_BACKGROUND);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setFont(font);
        // select color
        g.setColor(COLOR_TEXT);
        str = "色の選択";
        stringWidth = g.getFontMetrics().stringWidth(str);
        g.drawString(str, (WIDTH - stringWidth) / 2, TITLE_LABEL_COLOR_BASE);
        int diskSize;
        if (mPlayer == PLAYER_BLACK) {
            drawRect(g, TITLE_DISK_BLACK_LEFT, TITLE_DISK_TOP, TITLE_DISK_SIZE_SELECTED, TITLE_DISK_SIZE_SELECTED, Color.RED);
        }
        if (mActivePlayer == PLAYER_BLACK) {
            diskSize = TITLE_DISK_SIZE_LARGE;
        } else {
            diskSize = TITLE_DISK_SIZE;
        }
        fillOval(g, TITLE_DISK_BLACK_LEFT, TITLE_DISK_TOP, diskSize, Color.BLACK, Color.BLACK);
        if (mPlayer == PLAYER_WHITE) {
            drawRect(g, TITLE_DISK_WHITE_LEFT, TITLE_DISK_TOP, TITLE_DISK_SIZE_SELECTED, TITLE_DISK_SIZE_SELECTED, Color.RED);
        }
        if (mActivePlayer == PLAYER_WHITE) {
            diskSize = TITLE_DISK_SIZE_LARGE;
        } else {
            diskSize = TITLE_DISK_SIZE;
        }
        fillOval(g, TITLE_DISK_WHITE_LEFT, TITLE_DISK_TOP, diskSize, Color.WHITE, Color.BLACK);
        // select level
        g.setFont(font);
        str = "レベルの選択";
        drawString(g, str, WIDTH / 2, TITLE_LABEL_LEVEL_BASE, COLOR_TEXT, true, false);
        for (int i = 0; i < sLevelParam.length; i++) {
            int x = TITLE_LEVEL_LEFT + (i % TITLE_LEVEL_ROW_NUM) * TITLE_LEVEL_INTERVAL_X;
            int y = TITLE_LEVEL_TOP + (i / TITLE_LEVEL_ROW_NUM) * TITLE_LEVEL_INTERVAL_Y;
            if (i == mLevel) {
                drawRect(g, x, y, TITLE_LEVEL_SIZE_SELECTED, TITLE_LEVEL_SIZE_SELECTED, Color.RED);
            }
            if (i == mActiveLevel) {
                g.setFont(largeFont);
            } else {
                g.setFont(font);
            }
            drawString(g, Integer.toString(i + 1) , x, y, COLOR_TEXT, true, true);
        }
        // message to start
        if (mStartIsActive) {
            g.setFont(largeFont);
        } else {
            g.setFont(font);
        }
        str = "ゲームをはじめる";
        drawString(g, str, WIDTH / 2, TITLE_START_TOP, COLOR_TEXT, true, true);
        repaint();
    }

    private void startGame() {
        mState = STATE_GAME;
        mBoard.clear();
        mActivePlayer = PLAYER_BLACK;
        mCom.setLevel(sLevelParam[mLevel][0], sLevelParam[mLevel][1], sLevelParam[mLevel][2]);
        mCom.setRandom(sLevelParam[mLevel][3], sLevelParam[mLevel][4]);
        mCom.setOpeining((mLevel / 2) * 2);
        removeMouseListener(mMouseListener);
        removeMouseMotionListener(mMouseMotionListener);
        mMouseListener = new MouseListener() {

            public void mouseClicked(MouseEvent arg0) {
            }

            public void mouseEntered(MouseEvent arg0) {
            }

            public void mouseExited(MouseEvent arg0) {
            }

            public void mousePressed(MouseEvent arg0) {
                int x = arg0.getX();
                int y = arg0.getY();
                int posX = (x - BOARD_BOARD_LEFT) / BOARD_SQUARE_WIDTH;
                int posY = (y - BOARD_BOARD_TOP) / BOARD_SQUARE_HEIGHT;
                if (mPlayer == mActivePlayer && posX >= 0 && posX < Board.SIZE && posY >= 0 && posY < Board.SIZE) {
                    playGame(Board.getPos(posX, posY));
                } else if (x >= BOARD_TITLE_LEFT && x < BOARD_TITLE_LEFT + BOARD_TITLE_WIDTH &&
                        y >= BOARD_TITLE_TOP && y < BOARD_TITLE_TOP + BOARD_TITLE_HEIGHT) {
                    startTitle();
                }
            }

            public void mouseReleased(MouseEvent arg0) {
            }
            
        };
        mMouseMotionListener = new MouseMotionListener() {
            public void mouseDragged(MouseEvent arg0) {                
            }

            public void mouseMoved(MouseEvent arg0) {
            }
        };
        addMouseListener(mMouseListener);
        addMouseMotionListener(mMouseMotionListener);
        updateGame();
    }

    private void updateGame() {
        if (Board.getOpponent(mPlayer) == mActivePlayer && !mCom.isThinking()) {
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    long startTime = System.currentTimeMillis();
                    int move = mCom.getNextMove(mBoard, mActivePlayer);
                    long endTime = System.currentTimeMillis();
                    if (endTime >= startTime) {
                        long t = endTime - startTime;
                        if (t < 500) {
                            try {
                                Thread.sleep(500 - t);
                            } catch (InterruptedException e) {
                            }
                        }
                    }
                    if (mState == STATE_GAME) {
                        playGame(move);
                    }
                }
            });
            thread.start();
        }
        paintBoard();
    }

    private void playGame(int pos) {
        if (mBoard.flip(mActivePlayer, pos) <= 0) {
            return;
        }
        mActivePlayer = Board.getOpponent(mActivePlayer);
        if (!mBoard.canPlay(mActivePlayer)) {
            mActivePlayer = Board.getOpponent(mActivePlayer);
            if (!mBoard.canPlay(mActivePlayer)) {
                mActivePlayer = PLAYER_NONE;
            }
        }
        updateGame();
    }

    private void paintBoard() {
        Graphics g = mOffScreen.getGraphics();
        String str;
        int i, j;

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, FONT_SIZE));
        g.setColor(COLOR_BACKGROUND);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        for (i = 0; i < Board.SIZE; i++) {
            for (j = 0; j < Board.SIZE; j++) {
                int color = mBoard.getDisk(Board.getPos(i, j));
                fillRect(g, BOARD_BOARD_LEFT + i * BOARD_SQUARE_WIDTH + BOARD_SQUARE_WIDTH / 2,
                        BOARD_BOARD_TOP + j * BOARD_SQUARE_HEIGHT + BOARD_SQUARE_HEIGHT / 2,
                        BOARD_SQUARE_WIDTH, BOARD_SQUARE_HEIGHT,
                        COLOR_BOARD, Color.BLACK);
                if (color == Board.BLACK) {
                    fillOval(g, BOARD_BOARD_LEFT + i * BOARD_SQUARE_WIDTH + BOARD_SQUARE_WIDTH / 2,
                            BOARD_BOARD_TOP + j * BOARD_SQUARE_HEIGHT + BOARD_SQUARE_HEIGHT / 2,
                            BOARD_DISK_SIZE, Color.BLACK, Color.BLACK);
                    
                } else if (color == Board.WHITE) {
                    fillOval(g, BOARD_BOARD_LEFT + i * BOARD_SQUARE_WIDTH + BOARD_SQUARE_WIDTH / 2,
                            BOARD_BOARD_TOP + j * BOARD_SQUARE_HEIGHT + BOARD_SQUARE_HEIGHT / 2,
                            BOARD_DISK_SIZE, Color.WHITE, Color.BLACK);
                }
            }
            drawString(g, "ABCDEFGH".substring(i, i + 1),
                    BOARD_BOARD_LEFT + i * BOARD_SQUARE_WIDTH + BOARD_SQUARE_WIDTH / 2,
                    BOARD_BOARD_TOP - BOARD_SQUARE_HEIGHT / 2, COLOR_TEXT, true, true);
            drawString(g, "12345678".substring(i, i + 1),
                    BOARD_BOARD_LEFT - BOARD_SQUARE_WIDTH / 2,
                    BOARD_BOARD_TOP + BOARD_SQUARE_HEIGHT * i + BOARD_SQUARE_HEIGHT / 2,
                    COLOR_TEXT, true, true);
        }
        // score
        int base = BOARD_BOARD_TOP + (Board.SIZE + 1) * BOARD_SQUARE_HEIGHT;
        fillOval(g, BOARD_BOARD_LEFT - BOARD_SQUARE_WIDTH / 2, base - BOARD_SCORE_DISK_SIZE / 2,
                BOARD_SCORE_DISK_SIZE, Color.BLACK, Color.BLACK);
        drawString(g, Integer.toString(mBoard.countDisks(Board.BLACK)), BOARD_BOARD_LEFT,
                base, COLOR_TEXT);
        fillOval(g, BOARD_BOARD_LEFT + Board.SIZE * BOARD_SQUARE_WIDTH - BOARD_SQUARE_WIDTH / 2,
                base - BOARD_SCORE_DISK_SIZE / 2, BOARD_SCORE_DISK_SIZE, Color.WHITE, Color.BLACK);
        drawString(g, Integer.toString(mBoard.countDisks(Board.WHITE)), 
                BOARD_BOARD_LEFT + Board.SIZE * BOARD_SQUARE_WIDTH, base, COLOR_TEXT);
        // message
        str = "";
        if (mPlayer == mActivePlayer) {
            if (mActivePlayer == PLAYER_BLACK) {
                str = "黒の番です";
            } else if (mActivePlayer == PLAYER_WHITE) {
                str = "白の番です";
            }
        } else if (Board.getOpponent(mPlayer) == mActivePlayer) {
            str = "考え中...";
        } else {
            int result = mBoard.countDisks(Board.BLACK) - mBoard.countDisks(Board.WHITE);
            if (result > 0) {
                   str = "黒の" + Integer.toString(result) + "石勝ちです";                    
            } else if (result < 0) {
                   str = "白の" + Integer.toString(-result) + "石勝ちです";                    
            } else {
                str = "引き分けです";
            }
        }
        drawString(g, str, WIDTH / 2, base, COLOR_TEXT, true, false);

        // button to return to title
        g.setColor(COLOR_BUTTON);
        g.fillRect(BOARD_TITLE_LEFT, BOARD_TITLE_TOP, BOARD_TITLE_WIDTH, BOARD_TITLE_HEIGHT);
        g.setColor(Color.BLACK);
        g.drawRect(BOARD_TITLE_LEFT, BOARD_TITLE_TOP, BOARD_TITLE_WIDTH, BOARD_TITLE_HEIGHT);
        drawString(g, "タイトルへ戻る", BOARD_TITLE_LEFT + BOARD_TITLE_WIDTH / 2,
                BOARD_TITLE_TOP + BOARD_TITLE_HEIGHT / 2, COLOR_TEXT, true, true);
        repaint();
    }

    private void fillOval(Graphics g, int centerX, int centerY, int diameter, Color fillColor, Color frameColor) {
        g.setColor(fillColor);
        g.fillOval(centerX - diameter / 2, centerY - diameter / 2, diameter, diameter);
        g.setColor(frameColor);
        g.drawOval(centerX - diameter / 2, centerY - diameter / 2, diameter, diameter);
    }
    
    private void drawRect(Graphics g, int centerX, int centerY, int width, int height, Color color) {
        g.setColor(color);
        g.drawRect(centerX - width / 2, centerY - height / 2, width, height);
    }

    private void fillRect(Graphics g, int centerX, int centerY, int width, int height, Color fillColor, Color frameColor) {
        g.setColor(fillColor);
        g.fillRect(centerX - width / 2, centerY - height / 2, width, height);
        g.setColor(frameColor);
        g.drawRect(centerX - width / 2, centerY - height / 2, width, height);
    }

    private void drawString(Graphics g, String string, int x, int y, Color color) {
        drawString(g, string, x, y, color, false, false);
    }

    private void drawString(Graphics g, String string, int x, int y, Color color, boolean centerHorizontal, boolean centerVertical) {
        if (centerHorizontal) {
            int width = g.getFontMetrics().stringWidth(string);
            x -= width / 2;
        }
        if (centerVertical) {
            int height = g.getFont().getSize();
            y += height / 2;
        }
        g.setColor(color);
        g.drawString(string, x, y);
    }
}
