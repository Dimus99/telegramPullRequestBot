import java.io.File
import java.io.IOException


class GitHubInteraction {
    fun downloadPullRequest(urlPullRequest:String){
        //разделяем ссылку pull request на ссылку на проект и номер пулл реквеста
        val urlSplit = urlPullRequest.split("/")
        val urlProject = urlSplit.take(5).joinToString(separator = "/")
        val pullNumber = urlSplit[6]
        var directoryForGitProject = "./gitCopies"
        val command1 = "git clone $urlProject ${urlSplit[3]}_${urlSplit[6]}"
        val command2 = "git pull origin pull/$pullNumber/head"
        //далее пишем команды в консоль
        // возможно стоит исправить этот костыль с изменением directoryForGitProject
        // этот костыль связан с неудобством нормально создать директорию
        for (command in listOf(command1, command2)) {
            try {
                println("команда$command")
                val proc = Runtime.getRuntime().exec(command,null, File(directoryForGitProject))
                proc.waitFor()
                proc.destroy()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            directoryForGitProject += "/${urlSplit[3]}_${urlSplit[6]}"
        }
        // сделать так, чтобы можно было по ссылке получить проект / готово
        // потом этот проект можно будет отправить на виртуальную машину
    }
}