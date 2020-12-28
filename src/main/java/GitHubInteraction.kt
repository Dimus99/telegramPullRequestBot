import java.io.File
import java.io.IOException


class GitHubInteraction {
    fun downloadPullRequest(urlPullRequest:String): String? {
        //разделяем ссылку pull request на ссылку на проект и номер пулл реквеста
        val urlSplit = urlPullRequest.split("/")
        if (urlSplit.count() != 7 || urlSplit[0] != "https:" || urlSplit[2] != "github.com")
            return null
        val urlProject = urlSplit.take(5).joinToString(separator = "/")
        val pullNumber = urlSplit[6]
        var directoryForGitProject = "./gitCopies"
        val command1 = "git clone $urlProject ${urlSplit[4]}_${urlSplit[6]}"
        val command2 = "git pull origin pull/$pullNumber/head"
        //далее пишем команды в консоль
        for (command in listOf(command1, command2)) {
            try {
                println("команда$command")
                val proc = Runtime.getRuntime().exec(command,null, File(directoryForGitProject))
                proc.waitFor()
                proc.destroy()
            } catch (e: IOException) {
                e.printStackTrace()
                return null
            } catch (e: InterruptedException) {
                e.printStackTrace()
                return null
            }
            directoryForGitProject += "/${urlSplit[4]}_${urlSplit[6]}"
        }
        return "${urlSplit[4]}_${urlSplit[6]}"
    }
}