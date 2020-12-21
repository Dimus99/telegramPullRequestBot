public class Program {
    public static void main(String[] args) {
        DataBase database = new DataBase("databases/users.db");

        /*NetAngelsInteraction vdsAPI = new NetAngelsInteraction();
        var token = vdsAPI.getToken("hmnYc7RyC7aYtUTi87xf7kJ1fmcKSQXHtCdQ4fpvqipMGoBpdMZcUjjV");
        var machines = vdsAPI.getMachines(token);
        System.out.println("\n");
        System.out.println(machines.toString());
        System.out.println("\n");

        for (String vm: machines.keySet()
             ) {
            //vdsAPI.stopVM(token, machines.get(vm).get("id"));
            var data = vdsAPI.getLoginAndPassword(token, machines.get(vm).get("id"));
            System.out.println(data);
        }*/



        TelegramBot bot = new TelegramBot(database);
        bot.start();
    }
}
