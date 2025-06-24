import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LoginDialog extends JDialog {
    private JTextField tfUsername;
    private JPasswordField pfPassword;
    private JButton btnLogin, btnSignUp, btnCancel;
    private boolean succeeded;

    // Database connection info
    private static final String DB_HOST = "mysql-3cca993-fixgmc-e8b8.c.aivencloud.com";
    private static final String DB_PORT = "21268";
    private static final String DB_NAME = "tictactoedb";
    private static final String DB_USER = "avnadmin";
    private static final String DB_PASSWORD = "AVNS_1q_ZM4X2qbl8OKtYMD7";

    public LoginDialog(Frame parent) {
        super(parent, "Login", true);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints cs = new GridBagConstraints();
        cs.fill = GridBagConstraints.HORIZONTAL;

        JLabel lbUsername = new JLabel("Username: ");
        cs.gridx = 0; cs.gridy = 0; cs.gridwidth = 1;
        panel.add(lbUsername, cs);

        tfUsername = new JTextField(20);
        cs.gridx = 1; cs.gridy = 0; cs.gridwidth = 2;
        panel.add(tfUsername, cs);

        JLabel lbPassword = new JLabel("Password: ");
        cs.gridx = 0; cs.gridy = 1; cs.gridwidth = 1;
        panel.add(lbPassword, cs);

        pfPassword = new JPasswordField(20);
        cs.gridx = 1; cs.gridy = 1; cs.gridwidth = 2;
        panel.add(pfPassword, cs);

        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        btnLogin = new JButton("Login");
        btnSignUp = new JButton("Sign Up");
        btnCancel = new JButton("Cancel");

        JPanel bp = new JPanel();
        bp.add(btnLogin);
        bp.add(btnSignUp);
        bp.add(btnCancel);

        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(bp, BorderLayout.PAGE_END);

        pack();
        setResizable(false);
        setLocationRelativeTo(parent);

        btnLogin.addActionListener(e -> {
            String username = getUsername();
            String password = getPassword();

            if (authenticate(username, password)) {
                JOptionPane.showMessageDialog(LoginDialog.this,
                        "Hi " + username + "! You have successfully logged in.",
                        "Login", JOptionPane.INFORMATION_MESSAGE);
                succeeded = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(LoginDialog.this,
                        "Invalid username or password",
                        "Login", JOptionPane.ERROR_MESSAGE);
                pfPassword.setText("");
                succeeded = false;
            }
        });

        btnSignUp.addActionListener(e -> {
            String username = getUsername();
            String password = getPassword();
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(LoginDialog.this,
                        "Username and password cannot be empty",
                        "Sign Up", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (register(username, password)) {
                JOptionPane.showMessageDialog(LoginDialog.this,
                        "User registered successfully! You can now login.",
                        "Sign Up", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(LoginDialog.this,
                        "Registration failed! Username may already exist.",
                        "Sign Up", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancel.addActionListener(e -> {
            succeeded = false;
            dispose();
        });
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

    private boolean authenticate(String username, String password) {
        boolean valid = false;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME + "?sslmode=require",
                    DB_USER, DB_PASSWORD)) {

                String sql = "SELECT * FROM user WHERE username = ? AND password = ?";
                try (PreparedStatement pst = conn.prepareStatement(sql)) {
                    pst.setString(1, username);
                    pst.setString(2, password);
                    try (ResultSet rs = pst.executeQuery()) {
                        if (rs.next()) {
                            valid = true;
                        }
                    }
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

    private boolean register(String username, String password) {
        boolean success = false;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME + "?sslmode=require",
                    DB_USER, DB_PASSWORD)) {

                String checkSql = "SELECT * FROM user WHERE username = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                    checkStmt.setString(1, username);
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next()) {
                            return false;
                        }
                    }
                }

                String insertSql = "INSERT INTO user (username, password) VALUES (?, ?)";
                try (PreparedStatement pst = conn.prepareStatement(insertSql)) {
                    pst.setString(1, username);
                    pst.setString(2, password);
                    int rowCount = pst.executeUpdate();
                    if (rowCount > 0) {
                        success = true;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
        return success;
    }
}