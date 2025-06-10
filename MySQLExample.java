import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

public class MySQLExample {
    public static void main(String[] args) throws ClassNotFoundException {
        String host, port, databaseName, userName, password;
        host = "mysql-3cca993-fixgmc-e8b8.c.aivencloud.com";
        port = "21268";
        databaseName = "tictactoedb";
        userName = "avnadmin";
        password = "AVNS_1q_ZM4X2qbl8OKtYMD7";
        for (int i = 0; i < args.length - 1; i++) {
            switch (args[i].toLowerCase(Locale.ROOT)) {
                case "-host": host = args[++i]; break;
                case "-username": userName = args[++i]; break;
                case "-password": password = args[++i]; break;
                case "-database": databaseName = args[++i]; break;
                case "-port": port = args[++i]; break;
            }
        }
        // JDBC allows to have nullable username and password
        if (host == null || port == null || databaseName == null) {
            System.out.println("Host, port, database information is required");
            return;
        }
        Class.forName("com.mysql.cj.jdbc.Driver");
        try (final Connection connection =
                     DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + databaseName + "?sslmode=require", userName, password);
             final Statement statement = connection.createStatement();
             final ResultSet resultSet = statement.executeQuery("SHOW tables")) {

            while (resultSet.next()) {
                System.out.println("Table: " + resultSet.getString(1));
            }
        } catch (SQLException e) {
            System.out.println("Connection failure.");
            e.printStackTrace();
        }
    }
}