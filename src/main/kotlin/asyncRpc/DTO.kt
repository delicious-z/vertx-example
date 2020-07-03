package asyncRpc


data class ServiceInfo(
    val serviceClass:String,
    val address:String
)

data class RpcRequest(
    val serviceClass: String = "",
    val methodId: String = "",
    var promiseId: Int = 0,
    val args: Array<out Any>? = null
)

data class RpcResponse(
    val resultJson: String = "",
    val promiseId: Int = 0,
    val succeeded: Boolean = false,
    val cause: String = ""
)

