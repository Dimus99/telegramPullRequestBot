import me.ivmg.telegram.bot
import me.ivmg.telegram.dispatch
import me.ivmg.telegram.dispatcher.text
import java.lang.Exception


class TelegramBot(private val dataBase: DataBase){

    fun Start() {
        val bot = bot {
            token = "1307302342:AAEctqW04p3Tam2Zq6Eu2ThGOZ8B3IhxalY"
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
                            try {
                                bot.sendMessage(chatId = update.message!!.chat.id, text = "Ожидайте...")
                                AddToDataBase(update.message!!.chat.id.toString(), args[1])
                                bot.sendMessage(chatId = update.message!!.chat.id, text = "ваш токен из бд" + GetToken(update.message!!.chat.id.toString())).toString()
                            }
                            catch (e : Exception){
                                e.printStackTrace()
                            }

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