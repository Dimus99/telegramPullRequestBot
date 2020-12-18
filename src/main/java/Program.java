public class Program {
    public static void main(String[] args) {
        DataBase database = new DataBase("src/main/java/databases/users.db");
        TelegramBot bot = new TelegramBot(database);
        bot.start();
    }
}
