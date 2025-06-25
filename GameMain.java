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
    private JPanel topPanel;  // Field supaya bisa disembunyikan/dimunculkan
    private boolean vsComputer = false;
    private String username;

    private static final String DB_URL = "jdbc:mysql://mysql-3cca993-fixgmc-e8b8.c.aivencloud.com:21268/tictactoedb?sslmode=require";
    private static final String DB_USER = "avnadmin";
    private static final String DB_PASSWORD = "AVNS_1q_ZM4X2qbl8OKtYMD7";

    public GameMain(String username, int boardSize, boolean vsComputer) {
        this.username = username;
        this.vsComputer = vsComputer;

        board = new Board(boardSize, boardSize);

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(board.CANVAS_WIDTH, board.CANVAS_HEIGHT + 50));

        statusBar = new JLabel("Welcome, " + username);
        statusBar.setFont(FONT_STATUS);
        statusBar.setOpaque(true);
        statusBar.setBackground(COLOR_GRID);
        statusBar.setPreferredSize(new Dimension(300, 30));
        statusBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 12));

        // Inisialisasi topPanel dan tombol
        topPanel = new JPanel();
        leaderboardButton = new JButton("Leaderboard");
        vsPlayerButton = new JButton("Vs Player");
        vsAIButton = new JButton("Vs Komputer");

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
                    if (row < board.ROWS && col < board.COLS && board.cells[row][col].content == Seed.NO_SEED) {
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
            this.vsComputer = false;
            newGame();
        });
        vsAIButton.addActionListener(e -> {
            this.vsComputer = true;
            newGame();
        });
    }

    public void initGame() {
        newGame();
    }

    public void newGame() {
        for (int row = 0; row < board.ROWS; ++row) {
            for (int col = 0; col < board.COLS; ++col) {
                board.cells[row][col].content = Seed.NO_SEED;
            }
        }
        currentPlayer = Seed.CROSS;
        currentState = State.PLAYING;
        repaint();

        // Sembunyikan tombol saat game berjalan
        if (topPanel != null) {
            topPanel.setVisible(false);
        }
    }

    private void makeComputerMove() {
        java.util.List<int[]> emptyCells = new java.util.ArrayList<>();
        for (int row = 0; row < board.ROWS; ++row) {
            for (int col = 0; col < board.COLS; ++col) {
                if (board.cells[row][col].content == Seed.NO_SEED) {
                    emptyCells.add(new int[]{row, col});
                }
            }
        }

        if (!emptyCells.isEmpty()) {
            int[] move = emptyCells.get((int) (Math.random() * emptyCells.size()));
            int row = move[0];
            int col = move[1];
            board.cells[row][col].content = currentPlayer;
            currentState = board.stepGame(currentPlayer, row, col);
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

                // Ganti kode JOptionPane lama dengan yang ini:
                JPanel panel = new JPanel();
                panel.setBackground(new Color(245, 236, 224));
                panel.setLayout(new BorderLayout(10, 10));

                JLabel iconLabel = new JLabel(UIManager.getIcon("OptionPane.informationIcon"));
                panel.add(iconLabel, BorderLayout.WEST);

                JLabel messageLabel = new JLabel(
                    "<html><center>"
                    + "<h1 style='color: #4CAF50;'>🎉 Congratulations, " + winnerName + "! 🎉</h1>"
                    + "<br><p style='font-size:14px;'>Permainan telah selesai.</p>"
                    + "</center></html>");
                messageLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
                panel.add(messageLabel, BorderLayout.CENTER);

                JOptionPane.showMessageDialog(this, panel, "Game Selesai", JOptionPane.PLAIN_MESSAGE);

                showLeaderboard();
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

    private void showLeaderboardAndRestart() {
        showLeaderboard();

        // Cari JFrame induk (yang punya panel ini)
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof JFrame frame) {
            frame.dispose();  // tutup frame game

            // Buka lagi menu login & setup (bisa langsung ke setup jika ingin)
            SwingUtilities.invokeLater(() -> {
                LoginDialog loginDialog = new LoginDialog(null);
                loginDialog.setVisible(true);
                if (!loginDialog.isSucceeded()) System.exit(0);

                GameSetupDialog setupDialog = new GameSetupDialog(null);
                setupDialog.setVisible(true);
                if (!setupDialog.isConfirmed()) System.exit(0);

                int boardSize = setupDialog.getSelectedSize();
                boolean vsComputer = setupDialog.isVsComputer();

                JFrame newFrame = new JFrame(TITLE);
                newFrame.setContentPane(new GameMain(loginDialog.getUsername(), boardSize, vsComputer));
                newFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                newFrame.pack();
                newFrame.setLocationRelativeTo(null);
                newFrame.setVisible(true);
            });
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginDialog loginDialog = new LoginDialog(null);
            loginDialog.setVisible(true);
            if (!loginDialog.isSucceeded()) System.exit(0);

            GameSetupDialog setupDialog = new GameSetupDialog(null);
            setupDialog.setVisible(true);
            if (!setupDialog.isConfirmed()) System.exit(0);

            int boardSize = setupDialog.getSelectedSize();
            boolean vsComputer = setupDialog.isVsComputer();

            JFrame frame = new JFrame(TITLE);
            frame.setContentPane(new GameMain(loginDialog.getUsername(), boardSize, vsComputer));
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
