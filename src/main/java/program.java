import database.*;

import java.sql.SQLException;


public class program {
    public static void main(String[] args) throws SQLException {
        db database = new db("./lib/users.db");
        TelegramBot bot = new TelegramBot(database);
        bot.Start();

        

    }
}
