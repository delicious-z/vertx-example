package asyncRpc

import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.Vertx
import io.vertx.core.json.Json

object MathServiceObject : MathService {
    override fun add(a: Int, b: Int): Promise<Int> {
        return Promise.promise()
    }
}

private class Verticle_A : AbstractVerticle() {

    private var asyncRpc: AsyncRpc? = null

    override fun start() {
        val bus = vertx.eventBus()
        asyncRpc = AsyncRpc(
            VertxRpcRequestSender(bus, "rpc-result-${context.deploymentID()}")
        )
        val mathService = asyncRpc!!.getClient(MathServiceObject) as MathService
        vertx.setPeriodic(2000) { mathService.add(1, 2) }
    }
}

private class Verticle_B : AbstractVerticle() {
    override fun start() {
        val bus = vertx.eventBus()
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
    vertx.deployVerticle(Verticle_A())
        .onSuccess { vertx.deployVerticle(Verticle_B()) }
}