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
            print("--- Не удалось получить ТОКЕН")
            return ""
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
            val name : String = vmObj["name"] as String
            val host : String = vmObj["hostname"] as String
            val active : String = vmObj["state"] as String
            val ip : String = vmObj["main_ip"] as String
            val id : String = vmObj["id"].toString()
            machines[ip] = mapOf(
                "name" to name,
                "active" to active,
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


    private fun changePassword(token:String, id:String, password:String): Response {
        val response: Response = khttp.post(
            url = "https://api-ms.netangels.ru/api/v1/cloud/vms/$id/change-password/",
            headers = mapOf("Authorization" to "Bearer $token"),
            json = mapOf("password" to password)
        )
        val obj =response.jsonObject
        print("$response $obj")
        return response
    }

    fun getLoginAndPassword(token: String, id: String): Map<String, String> {
        val password = "dfgE3HKJ8c0DFj99d" // may edit
        val response = changePassword(token, id, password)
        if (response.statusCode == 200) {
            return mapOf(
                "host" to response.jsonObject["main_ip"].toString(),
                "login" to "root",
                "password" to password
            )
        }
        throw Exception("НЕ ПОЛУЧИЛОСЬ ПОЛУЧИТЬ ЛОГИН И ПАРОЛЬ")
    }
}
