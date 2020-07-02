package asyncRpc


data class RpcRequest(
    val className: String = "",
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

