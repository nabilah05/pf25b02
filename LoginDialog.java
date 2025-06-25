import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.security.MessageDigest;

public class LoginDialog extends JDialog {
    private JTextField tfUsername;
    private JPasswordField pfPassword;
    private JButton btnLogin, btnSignUp, btnCancel;
    private boolean succeeded;
    private JLabel welcomeLabel;
    private Timer animationTimer;
    private float alpha = 0f;

    private static final String DB_HOST = "mysql-3cca993-fixgmc-e8b8.c.aivencloud.com";
    private static final String DB_PORT = "21268";
    private static final String DB_NAME = "tictactoedb";
    private static final String DB_USER = "avnadmin";
    private static final String DB_PASSWORD = "AVNS_1q_ZM4X2qbl8OKtYMD7";

    public LoginDialog(Frame parent) {
        super(parent, "Login", true);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(245, 236, 224));

        // === Welcome label with animation ===
        welcomeLabel = new JLabel("Welcome to Tic Tac Toe", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Serif", Font.BOLD, 24));
        welcomeLabel.setForeground(new Color(120, 66, 18, 0));
        add(welcomeLabel, BorderLayout.NORTH);

        // === Form Panel ===
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(245, 236, 224));
        GridBagConstraints cs = new GridBagConstraints();
        cs.fill = GridBagConstraints.HORIZONTAL;

        JLabel lbUsername = new JLabel("Username: ");
        lbUsername.setForeground(new Color(89, 60, 40));
        cs.gridx = 0; cs.gridy = 0; cs.gridwidth = 1;
        panel.add(lbUsername, cs);

        tfUsername = new JTextField(20);
        cs.gridx = 1; cs.gridy = 0; cs.gridwidth = 2;
        panel.add(tfUsername, cs);

        JLabel lbPassword = new JLabel("Password: ");
        lbPassword.setForeground(new Color(89, 60, 40));
        cs.gridx = 0; cs.gridy = 1; cs.gridwidth = 1;
        panel.add(lbPassword, cs);

        pfPassword = new JPasswordField(20);
        cs.gridx = 1; cs.gridy = 1; cs.gridwidth = 2;
        panel.add(pfPassword, cs);

        add(panel, BorderLayout.CENTER);

        // === Button Panel ===
        JPanel bp = new JPanel();
        bp.setBackground(new Color(245, 236, 224));
        btnLogin = new JButton("Login");
        btnSignUp = new JButton("Sign Up");
        btnCancel = new JButton("Cancel");

        styleButton(btnLogin);
        styleButton(btnSignUp);
        styleButton(btnCancel);

        bp.add(btnLogin);
        bp.add(btnSignUp);
        bp.add(btnCancel);
        add(bp, BorderLayout.SOUTH);

        // === Frame Settings ===
        pack();
        setResizable(false);
        setLocationRelativeTo(parent);

        // === Action Listeners ===
        btnLogin.addActionListener(e -> handleLogin());
        btnSignUp.addActionListener(e -> handleSignUp());
        btnCancel.addActionListener(e -> {
            succeeded = false;
            dispose();
        });

        // Start welcome animation
        startWelcomeAnimation();
    }

    private void styleButton(JButton button) {
        button.setBackground(new Color(198, 177, 152));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
    }

    private void startWelcomeAnimation() {
        animationTimer = new Timer(30, e -> {
            alpha += 0.03f;
            if (alpha >= 1f) {
                alpha = 1f;
                animationTimer.stop();
            }
            welcomeLabel.setForeground(new Color(120, 66, 18, Math.min(255, (int)(alpha * 255))));
            repaint();
        });
        animationTimer.start();
    }

    private void handleLogin() {
        String username = getUsername();
        String password = getPassword();
        String hashedPassword = hashPassword(password);

        if (authenticate(username, hashedPassword)) {
            JOptionPane.showMessageDialog(this,
                    "Hi " + username + "! You have successfully logged in.",
                    "Login", JOptionPane.INFORMATION_MESSAGE);
            succeeded = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Invalid username or password",
                    "Login", JOptionPane.ERROR_MESSAGE);
            pfPassword.setText("");
            succeeded = false;
        }
    }

    private void handleSignUp() {
        String username = getUsername();
        String password = getPassword();
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Username and password cannot be empty",
                    "Sign Up", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String hashedPassword = hashPassword(password);
        if (register(username, hashedPassword)) {
            JOptionPane.showMessageDialog(this,
                    "User registered successfully! You can now login.",
                    "Sign Up", JOptionPane.INFORMATION_MESSAGE);
            tfUsername.setText("");
            pfPassword.setText("");
        } else {
            JOptionPane.showMessageDialog(this,
                    "Registration failed! Username may already exist.",
                    "Sign Up", JOptionPane.ERROR_MESSAGE);
        }
    }

    public String getUsername() {
        return tfUsername.getText().trim();
    }

    public String getPassword() {
        return new String(pfPassword.getPassword());
    }

    public boolean isSucceeded() {
        return succeeded;
    }

    private boolean authenticate(String username, String hashedPassword) {
        boolean valid = false;
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME + "?sslmode=require",
                DB_USER, DB_PASSWORD)) {

            String sql = "SELECT * FROM user WHERE username = ? AND password = ?";
            try (PreparedStatement pst = conn.prepareStatement(sql)) {
                pst.setString(1, username);
                pst.setString(2, hashedPassword);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) valid = true;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
        return valid;
    }

    private boolean register(String username, String hashedPassword) {
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME + "?sslmode=require",
                DB_USER, DB_PASSWORD)) {

            // Check if user exists
            String checkSql = "SELECT * FROM user WHERE username = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, username);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) return false; // user exists
                }
            }

            // Insert user
            String insertSql = "INSERT INTO user (username, password) VALUES (?, ?)";
            try (PreparedStatement pst = conn.prepareStatement(insertSql)) {
                pst.setString(1, username);
                pst.setString(2, hashedPassword);
                return pst.executeUpdate() > 0;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
