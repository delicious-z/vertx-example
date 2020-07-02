package asyncRpc

import io.vertx.core.Promise
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.Json
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method


interface RpcRequestSender {
    fun send(promise: Promise<Any>, rpcRequest: RpcRequest): Promise<Any>
}

abstract class AbstractRpcRequestSender : RpcRequestSender {
    protected val promiseMap: HashMap<Int, Promise<Any>> = HashMap()
    override fun send(promise: Promise<Any>, rpcRequest: RpcRequest): Promise<Any> {
        val promiseId = promise.hashCode()
        rpcRequest.promiseId = promiseId
        promiseMap.put(promiseId, promise)
        doSend(rpcRequest)
        return promise
    }

    abstract fun doSend(rpcRequest: RpcRequest)
}

class VertxRpcRequestSender(val eventBus: EventBus, receiverAddress: String) : AbstractRpcRequestSender() {
    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    init {
        eventBus.consumer<String>(receiverAddress) {
            log.info(it.body())
            val rpcResponse = Json.decodeValue(it.body(), RpcResponse::class.java)
            val promiseId: Int = rpcResponse.promiseId
            val promise = promiseMap.get(promiseId)
            promiseMap.remove(promiseId)
            if (rpcResponse.succeeded) {
                promise!!.complete(Json.decodeValue(rpcResponse.resultJson))
            } else {
                promise!!.fail(rpcResponse.cause)
            }
        }
    }

    override fun doSend(rpcRequest: RpcRequest) {
        eventBus.send("rpc", Json.encode(rpcRequest))
    }
}


data class RpcRequest(
    val className: String = "",
    val methodId: String = "",
    var promiseId: Int = 0,
    val args: Array<out Any>? = null,
    val receiverAddress: String = ""
)

data class RpcResponse(
    val resultJson: String = "",
    val promiseId: Int = 0,
    val succeeded: Boolean = false,
    val cause: String = ""
)




class AsyncRpc {

    fun getClient(){

    }

    private class DynamicProxyHandler(private var target: Any, private val rpcRequestSender: RpcRequestSender) :
        InvocationHandler {
        override fun invoke(proxy: Any, method: Method, args: Array<out Any>?): Promise<Any>? {
            val res = Promise.promise<Any>()
            val rpcRequest = RpcRequest()
            rpcRequestSender.send(res, rpcRequest)
            return res
        }
    }
}

//data class RpcResponse(
//
//)




fun main() {
    val vertx = Vertx.vertx()

}
