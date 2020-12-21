import me.ivmg.telegram.Bot
import me.ivmg.telegram.bot
import me.ivmg.telegram.dispatch
import me.ivmg.telegram.dispatcher.text
import me.ivmg.telegram.entities.KeyboardButton
import me.ivmg.telegram.entities.KeyboardReplyMarkup
import me.ivmg.telegram.entities.ReplyMarkup
import me.ivmg.telegram.entities.Update
import java.lang.Exception




class TelegramBot(private val dataBase:DataBase){
    enum class UserPosition{
        Default, Request, ApiKey
    }

    private val usersPositions = mutableMapOf<Int, UserPosition>()

    fun start() {
        val bot = bot {
            token = Config().token
            val gitActions = GitHubInteraction()
            val startKeyboard = KeyboardReplyMarkup(
                KeyboardButton("/request"),
                KeyboardButton("/start"),
                KeyboardButton("/setApiKey"))
            dispatch {
                text { bot, update ->
                    println(update)
                    val text = update.message?.text ?: "Error"
                    val args = text.split(" ")
                    if (!usersPositions.containsKey(update.message!!.chat.id.toInt()))
                        usersPositions[update.message!!.chat.id.toInt()] = UserPosition.Default
                    when (usersPositions[update.message!!.chat.id.toInt()]) {
                        UserPosition.Default ->
                        when (args[0]) {
                            "/start" -> bot.sendMessage(
                                chatId = update.message!!.chat.id,
                                text = "Приветствую, мой друг",
                                replyMarkup = startKeyboard
                            )
                            "/request" -> {
                                usersPositions[update.message!!.chat.id.toInt()] = UserPosition.Request
                                bot.sendMessage(
                                    chatId = update.message!!.chat.id,
                                    text = "отправьте следующим сообщением ссылку на pull Request"
                                )
                            }
                            "/setApiKey" -> {
                                usersPositions[update.message!!.chat.id.toInt()] = UserPosition.ApiKey
                                bot.sendMessage(
                                    chatId = update.message!!.chat.id,
                                    text = "отправьте следующим сообщением ваш Api_key"
                                )
                            }
                            else -> bot.sendMessage(chatId = update.message!!.chat.id, text = "Не понял ваше сообщение")
                        }
                        UserPosition.Request -> addRequest(args, bot, update, gitActions)
                        UserPosition.ApiKey -> setApiKey(args, bot, update)
                        else -> throw Exception("Лишняя ветвь when, что-то пошло не так")
                    }
                }
            }
        }
        bot.startPolling()
    }

    private fun setApiKey(
        args: List<String>,
        bot: Bot,
        update: Update
    ) {
        try {
            bot.sendMessage(chatId = update.message!!.chat.id, text = "Ожидайте...")
            addToDataBase(update.message!!.chat.id.toString(), args[0])
            bot.sendMessage(
                chatId = update.message!!.chat.id,
                text = "ваш токен из бд\n" + getToken(update.message!!.chat.id.toString())
            ).toString()
        } catch (e: Exception) {
            bot.sendMessage(chatId = update.message!!.chat.id, text = "ошибка с установкой токена")
            e.printStackTrace()
        }
        usersPositions[update.message!!.chat.id.toInt()] = UserPosition.Default
    }

    private fun addRequest(
        args: List<String>,
        bot: Bot,
        update: Update,
        gitActions: GitHubInteraction
    ) {
        val pullRequest = args[0]
        var answer = "Что-то пошло не так..."
        try {
            gitActions.downloadPullRequest(pullRequest)
            //тут получаем список машин и отправляем пользователю,
            // он выбирает, а потом мы туда загружаем проект
            answer = "url"
        } catch (e: Exception) {
            e.printStackTrace()
        }
        bot.sendMessage(chatId = update.message!!.chat.id, text = answer)
        usersPositions[update.message!!.chat.id.toInt()] = UserPosition.Default
    }

    private fun addToDataBase(id: String, token: String)
    {
        dataBase.addData(id, token)
    }

    private  fun getToken(id: String): String
    {
        return dataBase.getTokenById(id)
    }
}
