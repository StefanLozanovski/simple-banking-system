package banking;

import java.security.SecureRandom;

public class Account {

    private String cardNumber;
    private String cardPin;
    private int cardBalance;

    public Account() {
        final String bankIdentificationNumber = "400000";
        SecureRandom random = new SecureRandom();
        String customerAccountNumber = String.format("%09d", random.nextInt(1000000000));
        cardNumber = bankIdentificationNumber + customerAccountNumber;
        cardNumber += findCheckSum(cardNumber);
        cardPin =  String.format("%04d", random.nextInt(10000));
        cardBalance = 0;
    }

    public static String findCheckSum(String cardNumber) {
        /* Luhn algorithm */
        int[] number = new int[cardNumber.length()];
        int sum = 0;
        
        for (int i = 0; i < cardNumber.length() ; i++) {
            number[i] = Integer.parseInt(String.valueOf(cardNumber.charAt(i)));
            if (i % 2 == 0) {
                if (number[i] * 2 > 9) {
                    number[i] = number[i] * 2 - 9;
                } else {
                    number[i] = number[i] * 2;
                }
            }
            sum = sum + number[i];
        }
        return String.valueOf((10 - (sum % 10) == 10) ? 0 : (10 - (sum % 10)));
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getCardPin() {
        return cardPin;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public void setCardPin(String cardPin) {
        this.cardPin = cardPin;
    }

    public void setCardBalance(int cardBalance) {
        this.cardBalance = cardBalance;
    }
}