package banking;

import java.sql.*;
import java.util.Scanner;

import static banking.Account.findCheckSum;

public class Bank {

    private final Scanner sc = new Scanner(System.in);
    private Database database;

    public Bank(String fileName) {
        this.database = new Database(fileName);
        database.createNewTable();
    }

    public void start() {
        while (true) {
            System.out.println("1. Create an account\n"
                    + "2. Log into account\n"
                    + "0. Exit");
            int initialChoice = sc.nextInt();
            switch (initialChoice) {
                case 1:
                    createAccount();
                    break;
                case 2:
                    logIn();
                    break;
                case 0:
                    System.out.println("Bye!\n");
                    database.disconnect();
                    System.exit(0);
                    break;
                default:
                    System.out.println("Unexpected value, try again.\n");
                    break;
            }
        }
    }

    private void createAccount() {
        Account card = new Account();
        System.out.println("Your card has been created");
        System.out.println("Your card number:\n" + card.getCardNumber());
        System.out.println("Your card pin:\n" + card.getCardPin() + "\n");
        database.insertCard(card);
    }

    private void logIn() {
        sc.nextLine();
        System.out.println("Enter your card number:");
        String inputCardNumber = sc.nextLine();
        System.out.println("Enter your PIN:");
        String inputCardPin = sc.nextLine();
        Account currentCard = database.selectCard(inputCardNumber, inputCardPin);

        if (inputCardNumber.equals(currentCard.getCardNumber()) && inputCardPin.equals(currentCard.getCardPin())){
            System.out.println("You have successfully logged in!\n");
            loggedInActivity(currentCard);
        } else {
            System.out.println("Wrong card number or PIN!\n");
        }
    }

    private void loggedInActivity(Account currentCard) {
        boolean logout = false;
        while (!logout) {
            System.out.println("1. Balance\n"
                    + "2. Add income\n"
                    + "3. Do transfer\n"
                    + "4. Close account\n"
                    + "5. Log out\n"
                    + "0. Exit");
            int choice = sc.nextInt();
            switch (choice) {
                case 1:
                    System.out.println("Balance: " + displayBalance(currentCard) + "\n");
                    break;
                case 2:
                    System.out.println("Enter income:");
                    addIncome(sc.nextInt() + displayBalance(currentCard), currentCard);
                    System.out.println("Income was added!\n");
                    break;
                case 3:
                    System.out.println("Transfer");
                    doTransfer(currentCard);
                    break;
                case 4:
                    closeAccount(currentCard);
                    System.out.println("The account " + currentCard.getCardNumber() + " has been closed!\n");
                    break;
                case 5:
                    logout = true;
                    System.out.println("You have successfully logged out!\n");
                    start();
                    break;
                case 0:
                    System.out.println("Bye!\n");
                    database.disconnect();
                    System.exit(0);
                    break;
                default:
                    System.out.println("Unexpected value, try again.\n");
                    break;
            }
        }
    }

    private int displayBalance(Account currentCard) {
        int resultBalance = 0;
        String sql = "SELECT balance FROM card WHERE number=" + currentCard.getCardNumber();

        try (Connection connection = database.connect();
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

    private void addIncome(int balance, Account currentCard) {
        int thisId = 0;
        String sql = "SELECT id FROM card WHERE number=" + currentCard.getCardNumber();

        try (Connection connection = database.connect();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                thisId = rs.getInt("id");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        sql = "UPDATE card SET balance = ? WHERE id = ?";
        try (Connection connection = database.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, balance);
            preparedStatement.setInt(2, thisId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void doTransfer(Account currentCard) {
        String recipientCardNum;
        String recipientCheckSum;
        int transferAmount;
        Account recipientCard;
        sc.nextLine();
        System.out.println("Enter card number:");
        recipientCardNum = sc.nextLine();

        /* If the receiver's card number doesn't pass the Luhn algorithm */
        recipientCheckSum = findCheckSum(recipientCardNum.substring(0, recipientCardNum.length() - 1));
        if (recipientCardNum.length() != 16 || !String.valueOf(recipientCardNum.charAt(15)).equals(recipientCheckSum)) {
            System.out.println("Probably you made mistake in card number. Please try again!\n");
            return;
        }
        /* If the receiver's card number doesn't exist */
        if (!recipientCardNum.equals(database.selectCardNumber(recipientCardNum))){
            System.out.println("Such a card does not exist.\n");
            return;
        }
        /* If the user tries to transfer money to the same account */
        if (recipientCardNum.equals(currentCard.getCardNumber())){
            System.out.println("You can't transfer money to the same account!\n");
            return;
        }
        /* If the user tries to transfer more money than currently available */
        System.out.println("Enter how much money you want to transfer:");
        transferAmount = sc.nextInt();
        if (database.selectBalance(currentCard) < transferAmount || transferAmount == 0) {
            System.out.println("Not enough money!\n");
            return;
        }
        /* Transfer */
        recipientCard = database.selectRecipientCard(recipientCardNum);
        database.addBalance(transferAmount + database.selectBalance(recipientCard), recipientCard);
        database.addBalance(database.selectBalance(currentCard) - transferAmount, currentCard);
        System.out.println("Success!");
    }

    private void closeAccount(Account currentCard) {
        int delId = 0;
        String sql = "SELECT id FROM card WHERE number=" + currentCard.getCardNumber();

        try (Connection connection = database.connect();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                delId = rs.getInt("id");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        
        sql = "DELETE FROM card WHERE id = ?";
        try (Connection connection = database.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement. setInt ( 1 , delId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}