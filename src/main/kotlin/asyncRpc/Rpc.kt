package asyncRpc

import io.vertx.core.Promise
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.Json
import io.vertx.servicediscovery.types.HttpEndpoint.getClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import kotlin.reflect.KClass
import kotlin.reflect.full.superclasses


interface RpcRequestSender {
    fun send(promise: Promise<Any>, rpcRequest: RpcRequest): Promise<Any>
    fun receiverAddress():String
}

abstract class AbstractRpcRequestSender : RpcRequestSender {
    protected val promiseMap: HashMap<Int, Promise<Any>> = HashMap()
    override fun send(promise: Promise<Any>, rpcRequest: RpcRequest): Promise<Any> {
        val promiseId = promise.hashCode()
        rpcRequest.promiseId = promiseId
        rpcRequest.receiverAddress = receiverAddress()
        promiseMap.put(promiseId, promise)
        doSend(rpcRequest)
        return promise
    }


    abstract fun doSend(rpcRequest: RpcRequest)
}

class VertxRpcRequestSender(val eventBus: EventBus, private val receiverAddress: String) : AbstractRpcRequestSender() {
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
        log.info("send: $rpcRequest")
        eventBus.send("rpc", Json.encode(rpcRequest))
    }

    override fun receiverAddress(): String {
        return receiverAddress
    }

}


data class RpcRequest(
    val className: String = "",
    val methodId: String = "",
    var promiseId: Int = 0,
    val args: Array<out Any>? = null,
    var receiverAddress: String = ""
)

data class RpcResponse(
    val resultJson: String = "",
    val promiseId: Int = 0,
    val succeeded: Boolean = false,
    val cause: String = ""
)


class AsyncRpc(
    private val rpcRequestSender: RpcRequestSender
) {

    private val proxyCache:HashMap<String,Any> = HashMap()

    fun getClient(target: Any): Any {
        val key = getRpcClassStr(target)
        if (!proxyCache.containsKey(key)){
            proxyCache[key] = Proxy.newProxyInstance(
                target::class.java.classLoader,
                target.javaClass.interfaces,
                DynamicProxyHandler(target, rpcRequestSender)
            )
        }
        return proxyCache[key]!!
    }

    private class DynamicProxyHandler(
        private var target: Any,
        private val rpcRequestSender: RpcRequestSender
    ) : InvocationHandler {
        override fun invoke(proxy: Any, method: Method, args: Array<out Any>?): Promise<Any>? {
            val res = Promise.promise<Any>()
            val rpcRequest = RpcRequest(
                className = getRpcClassStr(target),
                methodId = getMethodId(method),
                args = args
            )
            rpcRequestSender.send(res, rpcRequest)
            return res
        }
    }
}



fun main() {
    val mathService = object :MathService{
        override fun add(a: Int, b: Int): Promise<Int> {
            return Promise.promise()
        }

    }
    val eventBus = Vertx.vertx().eventBus()
    val c = AsyncRpc(VertxRpcRequestSender(eventBus,"a")).getClient(mathService) as MathService
    c.add(1,2)

}
