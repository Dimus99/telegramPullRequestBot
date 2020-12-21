import khttp.responses.Response
import org.json.JSONArray
import org.json.JSONObject
// api https://api.netangels.ru/modules/gateway_api.api.cloud.vms/#_1


class NetAngelsInteraction {

    fun getToken(apiKey:String): String {
        val response : Response = khttp.post(
            url = "https://panel.netangels.ru/api/gateway/token/",
            data = mapOf("api_key" to apiKey)
        )
        val obj : JSONObject = response.jsonObject
        print(obj)
        print(response)
        if (obj.has("token")){
            return obj["token"].toString()
        }
        else{
            throw Exception("неверный апи ключ")
        }

    }

    fun getMachines(token: String): MutableMap<String, Map<String, String>> {
        val response: Response = khttp.get(
            url = "https://api-ms.netangels.ru/api/v1/cloud/vms/",
            headers = mapOf("Authorization" to "Bearer $token")
        )
        val obj: JSONObject = response.jsonObject
        print(obj)
        val vms : JSONArray = obj["entities"] as JSONArray
        val machines : MutableMap<String, Map<String, String>> = mutableMapOf()
        for (vm in vms){
            val vmObj = vm as JSONObject
            print("vm $vm")
            val host : String = vmObj["hostname"] as String
            val active : String = vmObj["state"] as String
            val ip : String = vmObj["main_ip"] as String
            val id : String = vmObj["id"].toString()
            machines[ip] = mapOf(
                "Active" to (active=="Active").toString(),
                "host" to host,
                "ip" to ip,
                "id" to id
            )
        }
        return machines
    }

    fun startVM(token:String, id:String): Boolean {
        val response: Response = khttp.post(
            url = "https://api-ms.netangels.ru/api/v1/cloud/vms/$id/start/",
            headers = mapOf("Authorization" to "Bearer $token")
        )
        val starting = (response.jsonObject["state"] as JSONObject)["Starting"]
        print("$id запуск= $starting")
        return response.statusCode==200
    }

    fun stopVM(token:String, id:String): Boolean {
        val response: Response = khttp.post(
            url = "https://api-ms.netangels.ru/api/v1/cloud/vms/$id/stop/",
            headers = mapOf("Authorization" to "Bearer $token")
        )

        print(response.toString()+" "+ response.jsonObject)

        val state = response.jsonObject["state"] as String
        print("$id запуск= $state")
        return response.statusCode==200
    }

    fun getVNCConsoleData(token: String, id: String): Map<String, String> {
        val response: Response = khttp.post(
            url = "https://api-ms.netangels.ru/api/v1/cloud/vms/$id/vnc/",
            headers = mapOf("Authorization" to "Bearer $token")
        )
        val obj =response.jsonObject
        print(response.toString()+" "+ obj)
        if (response.statusCode == 200) {
            return mapOf(
                "host" to obj["host"].toString(),
                "password" to obj["password"].toString(),
                "port" to obj["port"].toString(),
                "wsport" to obj["wsport"].toString()
            )
        }
        throw Exception("НЕ УДАЛОСЬ ПОЛУЧИТЬ ДАННЫЙ ДЛЯ VNC")
    }

    fun changePassword(token:String, id:String, password:String): Boolean {
        val response: Response = khttp.post(
            url = "https://api-ms.netangels.ru/api/v1/cloud/vms/$id/change-password/",
            headers = mapOf("Authorization" to "Bearer $token"),
            json = mapOf("password" to password)
        )
        val obj =response.jsonObject
        print(response.toString()+" "+ obj)
        return response.statusCode == 200
    }

    fun getLoginAndPassword(token: String, id: String): Map<String, String> {
        val password = "dfgE3HKJ8c0DFj99d"
        if (changePassword(token, id, password)) {
            return mapOf(
                "login" to "root",
                "password" to password
            )
        }
        throw Exception("НЕ ПОЛУЧИЛОСЬ ПОЛУЧИТЬ ПАРОЛЬ")
    }
}
