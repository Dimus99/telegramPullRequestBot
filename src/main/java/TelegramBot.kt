import me.ivmg.telegram.bot
import me.ivmg.telegram.dispatch
import me.ivmg.telegram.dispatcher.text

class TelegramBot
    fun main(args: Array<String>) {
        val bot = bot {
            token = "1307302342:AAF8z1THWcnkv_ZZ5vtlwROuMsnU8nZz7fU"
            dispatch {
                text { bot, update ->
                    print(update)
                    val text = update.message?.text ?: "Error"
                    val args = text.split(" ")
                    when (args[0]){
                        "/start" -> bot.sendMessage(chatId = update.message!!.chat.id, text = "Приветствую, мой друг")
                        "!request" -> {
                            val pullRequest = args[1]
                            bot.sendMessage(chatId = update.message!!.chat.id, text = "https://aaa.com")
                        }
                        "SetToken" -> {
                            // тут нужно сделать что-то вроде сохранения токена с netangels в бд(нужно создать бд)
                        }
                        else -> bot.sendMessage(chatId = update.message!!.chat.id, text = "Не знаю")
                    }
                }
            }
        }
        bot.startPolling()
    }