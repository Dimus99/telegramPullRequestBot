import java.sql.SQLException;


public class program {
    public static void main(String[] args) throws SQLException {
        DataBase database = new DataBase("src/main/java/lib/users.db");
        TelegramBot bot = new TelegramBot(database);
        bot.Start();
    }
}
