import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;

public class GameMain extends JPanel {
    private static final long serialVersionUID = 1L;

    public static final String TITLE = "Tic Tac Toe";
    public static final Color COLOR_BG = new Color(245, 236, 224);
    public static final Color COLOR_GRID = new Color(198, 177, 152);
    public static final Color COLOR_CROSS = new Color(120, 66, 18);
    public static final Color COLOR_NOUGHT = new Color(89, 114, 90);
    public static final Font FONT_STATUS = new Font("Segoe UI", Font.BOLD, 14);

    private Board board;
    private State currentState;
    private Seed currentPlayer;
    private JLabel statusBar;
    private JButton leaderboardButton, vsPlayerButton, vsAIButton;
    private boolean vsComputer = false;
    private String username;

    private static final String DB_URL = "jdbc:mysql://mysql-3cca993-fixgmc-e8b8.c.aivencloud.com:21268/tictactoedb?sslmode=require";
    private static final String DB_USER = "avnadmin";
    private static final String DB_PASSWORD = "AVNS_1q_ZM4X2qbl8OKtYMD7";

    public GameMain(String username) {
        this.username = username;

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(Board.CANVAS_WIDTH, Board.CANVAS_HEIGHT + 50));

        statusBar = new JLabel("Welcome, " + username);
        statusBar.setFont(FONT_STATUS);
        statusBar.setOpaque(true);
        statusBar.setBackground(COLOR_GRID);
        statusBar.setPreferredSize(new Dimension(300, 30));
        statusBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 12));

        JPanel topPanel = new JPanel();
        leaderboardButton = new JButton("Leaderboard");
        vsPlayerButton = new JButton("Vs Player");
        vsAIButton = new JButton("Vs AI");

        topPanel.add(vsPlayerButton);
        topPanel.add(vsAIButton);
        topPanel.add(leaderboardButton);

        add(topPanel, BorderLayout.NORTH);
        add(statusBar, BorderLayout.SOUTH);

        initGame();

        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = e.getY() / Cell.SIZE;
                int col = e.getX() / Cell.SIZE;

                if (currentState == State.PLAYING) {
                    if (row < Board.ROWS && col < Board.COLS && board.cells[row][col].content == Seed.NO_SEED) {
                        board.cells[row][col].content = currentPlayer;
                        currentState = board.stepGame(currentPlayer, row, col);
                        repaint();

                        if (vsComputer && currentState == State.PLAYING) {
                            currentPlayer = (currentPlayer == Seed.CROSS) ? Seed.NOUGHT : Seed.CROSS;
                            Timer timer = new Timer(500, ev -> {
                                makeComputerMove();
                                currentPlayer = (currentPlayer == Seed.CROSS) ? Seed.NOUGHT : Seed.CROSS;
                                repaint();
                            });
                            timer.setRepeats(false);
                            timer.start();
                        } else {
                            currentPlayer = (currentPlayer == Seed.CROSS) ? Seed.NOUGHT : Seed.CROSS;
                        }
                    }
                } else {
                    newGame();
                }
                repaint();
            }
        });

        leaderboardButton.addActionListener(e -> showLeaderboard());
        vsPlayerButton.addActionListener(e -> {
            vsComputer = false;
            newGame();
        });
        vsAIButton.addActionListener(e -> {
            vsComputer = true;
            newGame();
        });
    }

    public void initGame() {
        board = new Board();
        newGame();
    }

    public void newGame() {
        for (int row = 0; row < Board.ROWS; ++row) {
            for (int col = 0; col < Board.COLS; ++col) {
                board.cells[row][col].content = Seed.NO_SEED;
            }
        }
        currentPlayer = Seed.CROSS;
        currentState = State.PLAYING;
        repaint();
    }

    private void makeComputerMove() {
        for (int row = 0; row < Board.ROWS; ++row) {
            for (int col = 0; col < Board.COLS; ++col) {
                if (board.cells[row][col].content == Seed.NO_SEED) {
                    board.cells[row][col].content = currentPlayer;
                    currentState = board.stepGame(currentPlayer, row, col);
                    return;
                }
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(COLOR_BG);
        board.paint(g);

        switch (currentState) {
            case PLAYING -> {
                String playerName = (currentPlayer == Seed.CROSS) ? username : (vsComputer ? "Computer" : "Player 2");
                statusBar.setText(playerName + "'s Turn");
            }
            case DRAW -> {
                statusBar.setText("Draw! Click to play again.");
                updateResult(false);
            }
            case CROSS_WON, NOUGHT_WON -> {
                String winnerName;
                boolean isWinner = false;

                if (vsComputer) {
                    if (currentState == State.CROSS_WON) {
                        winnerName = (currentPlayer == Seed.NOUGHT) ? username : "Computer";
                        isWinner = (currentPlayer == Seed.NOUGHT);
                    } else {
                        winnerName = (currentPlayer == Seed.CROSS) ? username : "Computer";
                        isWinner = (currentPlayer == Seed.CROSS);
                    }
                } else {
                    winnerName = (currentState == State.CROSS_WON) ? username : "Player 2";
                    isWinner = (currentState == State.CROSS_WON);
                }

                statusBar.setText(winnerName + " won! Click to play again.");
                updateResult(isWinner);
            }
        }
    }

    private void updateResult(boolean win) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = win ? "UPDATE user SET win = win + 1 WHERE username = ?" : "UPDATE user SET lose = lose + 1 WHERE username = ?";
            try (PreparedStatement pst = conn.prepareStatement(sql)) {
                pst.setString(1, username);
                int updated = pst.executeUpdate();
                System.out.println("Updating results for user: " + username + " win: " + win);
                System.out.println("Rows updated: " + updated);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showLeaderboard() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT username, win FROM user ORDER BY win DESC LIMIT 10";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                ArrayList<String> leaders = new ArrayList<>();
                while (rs.next()) {
                    leaders.add(rs.getString("username") + " - Wins: " + rs.getInt("win"));
                }
                JOptionPane.showMessageDialog(this, String.join("\n", leaders), "Leaderboard", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error fetching leaderboard.", "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginDialog loginDialog = new LoginDialog(null);
            loginDialog.setVisible(true);
            if (loginDialog.isSucceeded()) {
                JFrame frame = new JFrame(TITLE);
                frame.setContentPane(new GameMain(loginDialog.getUsername()));
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            } else {
                System.exit(0);
            }
        });
    }
}
