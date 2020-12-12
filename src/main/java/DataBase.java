import java.sql.*;

public class DataBase
{
    static Connection conn;
    String databaseName;

    //Конструктор с  подключением к бд
    //В переменной path должно находится расположние файла с бд (имя файла и самой таблицы должны сопадать)
    public DataBase(String path) throws SQLException
    {
        databaseName = path.split("\\W+")[path.split("\\W+").length-2];

        try
        {
            Class.forName("org.sqlite.JDBC");
            path = "jdbc:sqlite:".concat(path);
            conn = DriverManager.getConnection(path);
            System.out.println("successfully connected to \'"+databaseName+"\'");

        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }


    //Добавляем запись в бд
    //Этот же метод используется для изменения уже существующих данных
    public  void AddData(String id, String token, String creation) throws SQLException
    {
        //Создаем ввод команды SQLite
        String query =
                "INSERT INTO "+databaseName+" (%s, %s, %s) ".formatted("id", "token", "creation") +
                "VALUES ('%s', '%s', '%s');".formatted(id, token, creation);
        //Пробуем создать новую запись
        try
        {
            Statement statement = conn.createStatement();
            statement.executeUpdate(query);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        //Если запись с таки id уже существует, изменяем ее
        finally {
            query =
                    "UPDATE "+databaseName+" "+
                    "SET token = \"%s\"".formatted(token) +
                    "WHERE name = \"%s\";".formatted(id);
            Statement statement = conn.createStatement();
            statement.executeUpdate(query);
        }



        System.out.println("data added");



    }







}
