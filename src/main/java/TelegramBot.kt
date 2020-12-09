import me.ivmg.telegram.bot
import me.ivmg.telegram.dispatch
import me.ivmg.telegram.dispatcher.command
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
                            val pull_request = args[1]
                            bot.sendMessage(chatId = update.message!!.chat.id, text = "https://aaa.com")
                        }
                        else -> bot.sendMessage(chatId = update.message!!.chat.id, text = "Не знаю")
                    }
                    bot.sendMessage(chatId = update.message!!.chat.id, text = text)
                }
            }
        }
        bot.startPolling()
    }