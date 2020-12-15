import me.ivmg.telegram.bot
import me.ivmg.telegram.dispatch
import me.ivmg.telegram.dispatcher.text
import java.lang.Exception


class TelegramBot(private val dataBase:DataBase){
    fun start() {
        val bot = bot {
            token = "1307302342:AAHm4HMSA6CZ-y2Dqquu1955ew8dfrIZFTA"
            val gitActions = GitHubInteraction()

            dispatch {
                text { bot, update ->
                    println(update)
                    val text = update.message?.text ?: "Error"
                    val args = text.split(" ")
                    when (args[0]) { // надо добавить трай кетчи на все отправки сообщений, вдруг интернет пропадет :(
                        "/start" -> bot.sendMessage(chatId = update.message!!.chat.id, text = "Приветствую, мой друг")
                        "!request" -> {
                            if (args.count() == 1){
                                bot.sendMessage(chatId = update.message!!.chat.id, text = "где ссылка на pull request?")
                            }
                            else {
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
                        }
                        "!setToken" -> {
                            if (args.count() == 1){
                                bot.sendMessage(chatId = update.message!!.chat.id, text = "где токен?")
                            }
                            else {
                                try {
                                    bot.sendMessage(chatId = update.message!!.chat.id, text = "Ожидайте...")
                                    addToDataBase(update.message!!.chat.id.toString(), args[1])
                                    bot.sendMessage(chatId = update.message!!.chat.id, text = "ваш токен из бд\n" + getToken(update.message!!.chat.id.toString())).toString()
                                } catch (e: Exception) {
                                    bot.sendMessage(chatId = update.message!!.chat.id, text = "ошибка с установкой токена")
                                    e.printStackTrace()
                                }
                            }
                        }
                        else -> bot.sendMessage(chatId = update.message!!.chat.id, text = "Не знаю")
                    }
                }
            }
        }
        bot.startPolling()
    }

    private fun addToDataBase(id: String, token: String)
    {
        dataBase.AddData(id, token)
    }

    private  fun getToken(id: String): String
    {
        return dataBase.GetTokenById(id)
    }
}
