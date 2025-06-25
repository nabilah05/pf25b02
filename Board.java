import java.awt.*;

public class Board {
    // Atribut instance untuk ukuran papan dan ukuran kanvas
    public final int ROWS;
    public final int COLS;
    public final int CANVAS_WIDTH;
    public final int CANVAS_HEIGHT;

    public static final int GRID_WIDTH = 8;  
    public static final int GRID_WIDTH_HALF = GRID_WIDTH / 2; 
    public static final Color COLOR_GRID = Color.LIGHT_GRAY;
    public static final int Y_OFFSET = 1;

    Cell[][] cells;

    public Board(int rows, int cols) {
        this.ROWS = rows;
        this.COLS = cols;
        this.CANVAS_WIDTH = Cell.SIZE * COLS;
        this.CANVAS_HEIGHT = Cell.SIZE * ROWS;

        initGame();
    }

    public void initGame() {
        cells = new Cell[ROWS][COLS];
        for (int row = 0; row < ROWS; ++row) {
            for (int col = 0; col < COLS; ++col) {
                cells[row][col] = new Cell(row, col);
            }
        }
    }

    public void newGame() {
        for (int row = 0; row < ROWS; ++row) {
            for (int col = 0; col < COLS; ++col) {
                cells[row][col].newGame();
            }
        }
    }

    public State stepGame(Seed player, int selectedRow, int selectedCol) {
        cells[selectedRow][selectedCol].content = player;

        // **Modifikasi logika menang sesuai ukuran papan**
        // Misal cek seluruh baris, kolom, diagonal dengan loop:
        boolean win;

        // Check row
        win = true;
        for (int col = 0; col < COLS; col++) {
            if (cells[selectedRow][col].content != player) {
                win = false;
                break;
            }
        }
        if (win) return (player == Seed.CROSS) ? State.CROSS_WON : State.NOUGHT_WON;

        // Check column
        win = true;
        for (int row = 0; row < ROWS; row++) {
            if (cells[row][selectedCol].content != player) {
                win = false;
                break;
            }
        }
        if (win) return (player == Seed.CROSS) ? State.CROSS_WON : State.NOUGHT_WON;

        // Check main diagonal (jika applicable)
        if (selectedRow == selectedCol) {
            win = true;
            for (int i = 0; i < ROWS; i++) {
                if (cells[i][i].content != player) {
                    win = false;
                    break;
                }
            }
            if (win) return (player == Seed.CROSS) ? State.CROSS_WON : State.NOUGHT_WON;
        }

        // Check anti diagonal (jika applicable)
        if (selectedRow + selectedCol == COLS - 1) {
            win = true;
            for (int i = 0; i < ROWS; i++) {
                if (cells[i][COLS - 1 - i].content != player) {
                    win = false;
                    break;
                }
            }
            if (win) return (player == Seed.CROSS) ? State.CROSS_WON : State.NOUGHT_WON;
        }

        // Check draw
        for (int row = 0; row < ROWS; ++row) {
            for (int col = 0; col < COLS; ++col) {
                if (cells[row][col].content == Seed.NO_SEED) {
                    return State.PLAYING;
                }
            }
        }

        return State.DRAW;
    }

    public void paint(Graphics g) {
        g.setColor(COLOR_GRID);
        for (int row = 1; row < ROWS; ++row) {
            g.fillRoundRect(0, Cell.SIZE * row - GRID_WIDTH_HALF,
                    CANVAS_WIDTH - 1, GRID_WIDTH,
                    GRID_WIDTH, GRID_WIDTH);
        }
        for (int col = 1; col < COLS; ++col) {
            g.fillRoundRect(Cell.SIZE * col - GRID_WIDTH_HALF, 0 + Y_OFFSET,
                    GRID_WIDTH, CANVAS_HEIGHT - 1,
                    GRID_WIDTH, GRID_WIDTH);
        }

        for (int row = 0; row < ROWS; ++row) {
            for (int col = 0; col < COLS; ++col) {
                cells[row][col].paint(g);
            }
        }
    }
}
