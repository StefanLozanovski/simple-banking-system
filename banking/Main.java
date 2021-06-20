package banking;

public class Main {

    public static void main(String[] args) {
        String fileName = "";
        for (int i = 0; i < args.length; i++) {
            if (args[0].equals("-fileName")) {
                fileName = args[1];
            } else {
                fileName = "card.s3db";
            }
        }
        Bank banking = new Bank(fileName);
        banking.start();
    }
}