package banking;

import java.sql.*;

public class Database {

    private static String url;
    private Connection connection;
    private Statement statement;

    public Database(String fileName) {
        url = "jdbc:sqlite:" + fileName;
        createNewDatabase(fileName);
    }

    Connection connect() {
        connection = null;
        try {
            connection = DriverManager.getConnection(url);
            statement = connection.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    void disconnect() {
        try {
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void executeSQL(String sql){
        try (Connection connection = this.connect()) {
            connection.createStatement().execute(sql);
        }
        catch (SQLException e){
            e.printStackTrace();
        }
    }

    private static void createNewDatabase(String fileName) {
        String url = "jdbc:sqlite:" + fileName;
        try (Connection connection = DriverManager.getConnection(url)) {
            if (connection != null) {
                DatabaseMetaData meta = connection.getMetaData();
                System.out.println("The driver name is: " + meta.getDriverName());
                System.out.println("A new database has been created.\n");
            } else {
                System.out.println("A new database has not been created.\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void createNewTable() {
        String sql = "CREATE TABLE IF NOT EXISTS card (\n"
                + "     id INTEGER PRIMARY KEY ASC,\n"
                + "     number TEXT,\n"
                + "     pin TEXT,\n"
                + "     balance INTEGER DEFAULT 0\n"
                + ");";
        executeSQL(sql);
    }

    void insertCard(Account card) {
        String sql = String.format("INSERT INTO card (number, pin) VALUES ('%s', '%s')",
                card.getCardNumber(),
                card.getCardPin());
        executeSQL(sql);
    }

    Account selectCard(String cardNumber, String cardPin) {
        /* Get an object with a card number and PIN */
        Account result = new Account();
        String resultNumber = "";
        String resultPin = "";
        String sql = "SELECT number, pin, balance FROM card WHERE number=" + cardNumber + " AND pin=" + cardPin;

        try (Connection connection = this.connect();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                resultNumber = rs.getString("number");
                resultPin = rs.getString("pin");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        result.setCardNumber(resultNumber);
        result.setCardPin(resultPin);
        return result;
    }

    Account selectRecipientCard(String cardNumber) {
        /* Get an object with a card number and balance */
        Account result = new Account();
        String resultNumber = "";
        int resultBalance = 0;
        String sql = "SELECT number, balance FROM card WHERE number=" + cardNumber;

        try (Connection connection = this.connect();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                resultNumber = rs.getString("number");
                resultBalance = rs.getInt("balance");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        result.setCardNumber(resultNumber);
        result.setCardBalance(resultBalance);
        return result;
    }

    String selectCardNumber(String cardNumber) {
        String resultNumber = "";
        String sql = "SELECT number, pin, balance FROM card WHERE number=" + cardNumber;

        try (Connection connection = this.connect();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                resultNumber = rs.getString("number");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultNumber;
    }

    int selectBalance(Account currentCard) {
        int resultBalance = 0;
        String sql = "SELECT balance FROM card WHERE number=" + currentCard.getCardNumber();

        try (Connection connection = this.connect();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                resultBalance = rs.getInt("balance");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultBalance;
    }

    void addBalance(int balance, Account bankCard) {
        int thisId = 0;
        String sql = "SELECT id FROM card WHERE number=" + bankCard.getCardNumber();

        try (Connection connection = this.connect();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                thisId = rs.getInt("id");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        
        sql = "UPDATE card SET balance = ? WHERE id = ?";
        try (Connection connection = this.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, balance);
            preparedStatement.setInt(2, thisId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}