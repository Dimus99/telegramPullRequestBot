import java.sql.*;
import java.util.Objects;

public class DataBase {
    static Connection conn;
    String databaseName;


    //Конструктор с  подключением к бд
    //В переменной path должно находится расположние файла с бд (имя файла и самой таблицы должны сопадать)
    public DataBase(String path) {
        databaseName = path.split("\\W+")[path.split("\\W+").length - 2];

        try {
            Class.forName("org.sqlite.JDBC");
            path = "jdbc:sqlite:".concat(path);
            conn = DriverManager.getConnection(path);
            System.out.println("successfully connected to '" + databaseName + "'");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    // Добавляем запись в бд
    // Этот же метод используется для изменения уже существующих данных
    public void addData(String id, String token) throws SQLException {
        // Создаем ввод команды SQLite
        String query =
                "INSERT INTO " + databaseName + " (%s, %s) ".formatted("id", "token") +
                        "VALUES ('%s', '%s');".formatted(id, token);
        // Пробуем создать новую запись
        try {
            Statement statement = conn.createStatement();
            statement.executeUpdate(query);
        } catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        // Если запись с таки id уже существует, изменяем ее
        finally {
            query =
                    "UPDATE " + databaseName + " " +
                            "SET token = \"%s\"".formatted(token) +
                            "WHERE id = \"%s\";".formatted(id);
            Statement statement = conn.createStatement();
            statement.executeUpdate(query);
        }

        System.out.println("data added");
    }

    // По названию и так понятен функционал
    public String getTokenById(String id) throws SQLException {

        ResultSet resSet = null;
        String res = null;

        String query =
                "SELECT token FROM " + databaseName + " WHERE id = \"" + id + "\";";

        try
        {
            Statement statement = conn.createStatement();
            resSet = statement.executeQuery(query);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        while (Objects.requireNonNull(resSet).next()) {
            res = resSet.getString("token");
        }

        return res;
    }
}
