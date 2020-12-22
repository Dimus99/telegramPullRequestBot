import me.ivmg.telegram.Bot
import me.ivmg.telegram.bot
import me.ivmg.telegram.dispatch
import me.ivmg.telegram.dispatcher.callbackQuery
import me.ivmg.telegram.dispatcher.text
import me.ivmg.telegram.entities.*
import java.lang.Exception


class TelegramBot(private val dataBase:DataBase){
    enum class UserPosition{
        Default, Request, ApiKey, ChoseVDSForRequest, ManageVDS, ManageActionVDS
    }

    private val usersPositions = mutableMapOf<Int, UserPosition>()

    fun start() {
        val bot = bot {
            token = Config().token
            val gitActions = GitHubInteraction()
            val startKeyboard = KeyboardReplyMarkup(
                KeyboardButton("/request"),
                KeyboardButton("/start"),
                KeyboardButton("/setApiKey"),
                KeyboardButton(("/manageVDS"))
            )
            dispatch {
                text { bot, update ->
                    println(update)
                    val text = update.message?.text ?: "Error"
                    val args = text.split(" ")
                    if (!usersPositions.containsKey(update.message!!.chat.id.toInt()))
                        usersPositions[update.message!!.chat.id.toInt()] = UserPosition.Default
                    if (text[0] == '/')
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
                                        text = "Отправьте следующим сообщением ссылку на pull Request"
                                    )
                                }
                                "/setApiKey" -> {
                                    usersPositions[update.message!!.chat.id.toInt()] = UserPosition.ApiKey
                                    bot.sendMessage(
                                        chatId = update.message!!.chat.id,
                                        text = "Отправьте следующим сообщением ваш Api_key"
                                    )
                                }
                                "/manageVDS" -> {
                                    usersPositions[update.message!!.chat.id.toInt()] = UserPosition.ManageVDS
                                    bot.sendMessage(
                                        chatId = update.message!!.chat.id,
                                        text = "Запустить или выключить?",
                                        replyMarkup = getInlineKeyboard(
                                            listOf("Запустить", "Выключить"),
                                            listOf("запустить", "выключить")
                                        )
                                    )
                                }
                                else -> bot.sendMessage(
                                    chatId = update.message!!.chat.id,
                                    text = "Не понял ваше сообщение"
                                )
                            }
                        UserPosition.Request -> addRequest(args, bot, update, gitActions)
                        UserPosition.ApiKey -> setApiKey(args, bot, update)
                        UserPosition.ChoseVDSForRequest, UserPosition.ManageActionVDS, UserPosition.ManageVDS ->
                            bot.sendMessage(
                            chatId = update.message!!.chat.id,
                            text = "нажмите на кнопку, или, для выхода нажмите /start"
                        )

                        else -> throw Exception(
                            "Лишняя ветвь when, что-то пошло не так," +
                                    usersPositions[update.message!!.chat.id.toInt()]
                        )
                    }
                }
                callbackQuery { bot, update ->
                    print(update)

                    when (usersPositions[update.callbackQuery!!.message!!.chat.id.toInt()]){
                        UserPosition.ChoseVDSForRequest -> uploadRequest(bot, update)
                        UserPosition.ManageVDS -> manageVDS(bot, update)
                        UserPosition.ManageActionVDS -> manageActionVDS(bot, update)
                        else -> {bot.sendMessage(chatId = update.callbackQuery!!.message!!.chat.id, text = "Заново сделайте запрос")}
                    }
                }
            }
        }
        bot.startPolling()
    }

    private fun manageActionVDS(bot: Bot, update: Update) {
        val request = update.callbackQuery!!.data.split(" ")
        if (request.count() != 3)
        {
            bot.sendMessage(chatId = update.callbackQuery!!.message!!.chat.id, text = "заного нажмите /manageVDS")
            usersPositions[update.callbackQuery!!.message!!.chat.id.toInt()] = UserPosition.Default
            return
        }
        val token = NetAngelsInteraction().getToken(getApiKey(update.callbackQuery!!.message!!.chat.id.toString()))
        if (request[2] == "запустить")
            NetAngelsInteraction().startVM(token, request[0])
        else
            NetAngelsInteraction().stopVM(token, request[0])
        bot.sendMessage(chatId = update.callbackQuery!!.message!!.chat.id,
            text = "выполнил команду \"$request\"")
        usersPositions[update.callbackQuery!!.message!!.chat.id.toInt()] = UserPosition.Default
    }

    private fun manageVDS(bot: Bot, update: Update) {
        val request = update.callbackQuery!!.data

        //val message = if (request == "запустить") "Запускаю" else "Выключаю"
        val vms = getMachines(bot, update.callbackQuery!!.message!!.chat.id)
        if (vms != null) {
            val neededVMS = vms.filter { x ->
                x.value["active"] == if (request == "выключить") "Active" else "Stopped"
            }
            if (neededVMS.count() == 0) {
                bot.sendMessage(
                    chatId = update.callbackQuery!!.message!!.chat.id, text = "Нет подходящих VDS"
                )
                return
            }
            bot.sendMessage(
                chatId = update.callbackQuery!!.message!!.chat.id, text = "Выберите вм",
                replyMarkup = getKeyboardVDS(neededVMS, request)
            )
            usersPositions[update.callbackQuery!!.message!!.chat.id.toInt()] = UserPosition.ManageActionVDS
        }
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
                text = "Ваш apiKey из бд\n" + getApiKey(update.message!!.chat.id.toString())
            ).toString()
        } catch (e: Exception) {
            bot.sendMessage(chatId = update.message!!.chat.id, text = "Ошибка с установкой токена")
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
        bot.sendMessage(chatId = update.message!!.chat.id, text = "Скачиваем ваш pull request, ожидайте...")
        val pullRequest = args[0]
        try {
            if (!gitActions.downloadPullRequest(pullRequest)) {
                bot.sendMessage(chatId = update.message!!.chat.id, text = "Ссылка не валидная, отправьте верную, или нажмите /start")
                return
            }
            val machines = getMachines(bot, update.message!!.chat.id)
            if (machines != null) {
                val keyboard = getKeyboardVDS(machines, "")
                bot.sendMessage(
                    chatId = update.message!!.chat.id,
                    text = "Выберете сервер для загрузки реквеста \n(я поменяю пароль и отправлю вам данные для подключения)",
                    replyMarkup = keyboard
                )
                usersPositions[update.message!!.chat.id.toInt()] = UserPosition.ChoseVDSForRequest
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getKeyboardVDS(machines: Map<String, Map<String, String>>, arg:String): ReplyMarkup {

        val machinesList: MutableList<String> = mutableListOf()
        val ids: MutableList<String> = mutableListOf()
        for (vds: Map<String, String> in machines.values) {
            ids.add(vds["id"] + " " + vds["active"] + " " + arg)
            machinesList.add(
                vds["name"].toString() + "\n" +
                        vds["ip"].toString() +
                        " status=" + vds["active"].toString() +
                        " id=" + vds["id"].toString()
            )
        }
        return getInlineKeyboard(machinesList, ids)
    }

    private fun addToDataBase(id: String, token: String)
    {
        dataBase.addData(id, token)
    }

    private fun getApiKey(id: String): String
    {
        return dataBase.getTokenById(id)
    }

    private fun uploadRequest(bot: Bot, update: Update) {
        print(update.callbackQuery.toString() + "\n\n\n")
        val vdsId = update.callbackQuery!!.data.split(" ")[0]
        val apiKey = getApiKey(update.callbackQuery!!.message!!.chat.id.toString())
        val token = NetAngelsInteraction().getToken(apiKey)
        print(update.callbackQuery!!.message!!.replyMarkup!!.inlineKeyboard)
        print("\n\n")
        val statusVDS = update.callbackQuery!!.data.split(" ")[1]
        print(statusVDS)
        if (statusVDS != "Active") {
            bot.sendMessage(
                chatId = update.callbackQuery!!.message!!.chat.id,
                text = "сначала включите VM в /manageVDS"
            )
            //usersPositions[update.callbackQuery!!.message!!.chat.id.toInt()] = UserPosition.Default
            return
        }

        val vdsData = NetAngelsInteraction().getLoginAndPassword(token, vdsId)
        //вот тут вызов функции, которая загрузит проект

        bot.sendMessage(chatId = update.callbackQuery!!.message!!.chat.id, text =
        "Я поменял пароль на этой VDS $vdsId , вот данные для подключения: $vdsData")
        usersPositions[update.callbackQuery!!.message!!.chat.id.toInt()] = UserPosition.Default
    }

    private fun getMachines(bot:Bot, chatID:Long): MutableMap<String, Map<String, String>>? {

        val apiKey = getApiKey(chatID.toString())
        if (apiKey.isEmpty())
        {
            bot.sendMessage(chatId = chatID, text = "Ваш apiKey невалидный/вы его не загрузили")
            bot.sendMessage(chatId = chatID, text = "Для загрузки воспользуйтесь /setApiKey")
            return null
        }
        else {
            val token = NetAngelsInteraction().getToken(apiKey) // may edit
            if (token.isEmpty()) {
                bot.sendMessage(
                    chatId = chatID,
                    text = "У вас не валидный ApiKey, установите валидный ключ от netangels api в /setApiKey"
                )
                usersPositions[chatID.toInt()] = UserPosition.Default
                return null
            }
            return NetAngelsInteraction().getMachines(token)
        }
    }

    private fun getInlineKeyboard(list:List<String>, ids:List<String>): InlineKeyboardMarkup {
        val rows : MutableList<MutableList<InlineKeyboardButton>> = mutableListOf()
        for (row in 1..list.count())
            rows.add(mutableListOf())
        for ((numberButton, elem) in list.withIndex()){
            rows[numberButton].add(InlineKeyboardButton(text = elem, callbackData = ids[numberButton]))
        }
        return InlineKeyboardMarkup(rows)
    }
}
