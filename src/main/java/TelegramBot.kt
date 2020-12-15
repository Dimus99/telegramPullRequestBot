import me.ivmg.telegram.bot
import me.ivmg.telegram.dispatch
import me.ivmg.telegram.dispatcher.text
import java.lang.Exception
import database.db;


class TelegramBot(val dataBase: DataBase){


    fun Start() {

        val bot = bot {
            token = "1307302342:AAF8z1THWcnkv_ZZ5vtlwROuMsnU8nZz7fU"
            //val gitActions = GitHubInteraction()
            val gitActions = GitHubInteraction()

            dispatch {
                text { bot, update ->
                    println(update)
                    val text = update.message?.text ?: "Error"
                    val args = text.split(" ")
                    when (args[0]) { // надо добавить трай кетчи на все отправки сообщений, вдруг интернет пропадет :(
                        "/start" -> bot.sendMessage(chatId = update.message!!.chat.id, text = "Приветствую, мой друг")
                        "!request" -> {
                            val pullRequest = args[1]
                            var answer = "Что-то пошло не так..."
                            try {
                                gitActions.downloadPullRequest(pullRequest)
                                answer = "url"
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            bot.sendMessage(chatId = update.message!!.chat.id, text = answer)
                        }
                        "!setToken" -> {
                            AddToDataBase(update.message!!.chat.id.toString(), args[1])

                        }
                        else -> bot.sendMessage(chatId = update.message!!.chat.id, text = "Не знаю")
                    }
                }
            }
        }
        bot.startPolling()
    }

    private fun AddToDataBase(id: String, token: String)
    {
        dataBase.AddData(id, token)
    }

    private  fun GetToken(id: String): String
    {
        return dataBase.GetTokenById(id);
    }


}