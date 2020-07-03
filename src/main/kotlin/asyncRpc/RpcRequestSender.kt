package asyncRpc

import io.vertx.core.Future
import io.vertx.core.MultiMap
import io.vertx.core.Promise
import io.vertx.core.Vertx
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.Json
import org.slf4j.Logger
import org.slf4j.LoggerFactory


interface RpcRequestSender {
    fun send(future: Promise<Any>, rpcRequest: RpcRequest): Promise<Any>
    fun receiverAddress(): String
}

abstract class AbstractRpcRequestSender : RpcRequestSender {
    protected val promiseMap: HashMap<Int, Promise<Any>> = HashMap()
    override fun send(future: Promise<Any>, rpcRequest: RpcRequest): Promise<Any> {
        val promiseId = future.hashCode()
        rpcRequest.promiseId = promiseId
        promiseMap.put(promiseId, future)
        doSend(rpcRequest)
        return future
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
        val deliveryOptions: DeliveryOptions = DeliveryOptions()
        deliveryOptions.headers = MultiMap.caseInsensitiveMultiMap()
            .add(AsyncRpcConstants.RECEIVER_ADDRESS, receiverAddress)
        eventBus.send(AsyncRpcConstants.SERVICE_REQUEST_ADDRESS, Json.encode(rpcRequest),deliveryOptions)
    }

    override fun receiverAddress(): String {
        return receiverAddress
    }

}



fun main() {
    val mathService = object : MathService {
        override fun add(a: Int, b: Int): Future<Int> {
            return Promise.promise<Int>().future()
        }

    }
    val eventBus = Vertx.vertx().eventBus()
    val c = AsyncRpc(VertxRpcRequestSender(eventBus, "a")).getClient(mathService) as MathService
    c.add(1, 2)

}
