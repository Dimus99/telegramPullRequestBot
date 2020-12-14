import me.ivmg.telegram.bot
import me.ivmg.telegram.dispatch
import me.ivmg.telegram.dispatcher.text
import java.lang.Exception
import database.db;


class TelegramBot constructor(db: db){

    fun Start() {

        val bot = bot {
            token = "1307302342:AAF8z1THWcnkv_ZZ5vtlwROuMsnU8nZz7fU"
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
                            // тут нужно сделать что-то вроде сохранения токена с netangels в бд(нужно создать бд)
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
        //db.AddData(id, token);
        //выдает ошибку
    }

    private fun GetToken(id: String)
    {
        //db.GetTokenById(id);
        //тоже выдает ошибку
    }


}