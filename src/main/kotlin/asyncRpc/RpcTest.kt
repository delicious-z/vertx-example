package asyncRpc

import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.Promise
import io.vertx.core.Vertx
import io.vertx.core.json.Json
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object MathServiceObject : MathService {
    override fun add(a: Int, b: Int): Future<Int> {
        return Promise.promise<Int>().future()
    }
}

private class Verticle_A : CoroutineVerticle() {

    private var asyncRpc: AsyncRpc? = null

    override suspend fun start() {
        val bus = vertx.eventBus()
        asyncRpc = AsyncRpc(
            VertxRpcRequestSender(bus, "rpc-result-${context.deploymentID()}")
        )
        val mathService = asyncRpc!!.getClient(MathServiceObject) as MathService
        GlobalScope.launch {
            delay(1000)
            val res = mathService.add(1, 2)!!.await()
            println("rpc receive result: $res")
        }
    }
}

private class Verticle_B : AbstractVerticle() {


    override fun start() {
        val bus = vertx.eventBus()
        val receiveAddress = "rpc-result-${context.deploymentID()}"
        val sender = VertxRpcRequestSender(bus, receiveAddress)
        val serviceDiscoveryClient = VertxServiceDiscoveryClient(vertx, "rpc-consume-${context.deploymentID()}")

        val mathService = object : MathService {
            override fun add(a: Int, b: Int): Future<Int>? {
                println("add invoke!!!")
                return Future.succeededFuture(a + b)
            }
        }
        serviceDiscoveryClient.register(mathService)
        bus.consumer<String>(AsyncRpcConstants.SERVICE_REQUEST_ADDRESS)
            .handler {
                val rpcRequest = Json.decodeValue(it.body(), RpcRequest::class.java)
                println("receive rpcRequest: $rpcRequest")
                println("requestAddress: ${it.headers()[AsyncRpcConstants.RECEIVER_ADDRESS]}")
            }
    }
}

fun main() {
    val vertx = Vertx.vertx()
    vertx.apply {
        deployVerticle(DiscoveryVerticle())
            .onSuccess { deployVerticle(Verticle_B()) }
            .onSuccess { deployVerticle(Verticle_A()) }
    }
}
