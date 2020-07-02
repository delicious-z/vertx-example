package asyncRpc

import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.Vertx
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

private class Verticle_A : AbstractVerticle() {

    var rpcRequestSender: RpcRequestSender? = null

    override fun start() {
        rpcRequestSender = VertxRpcRequestSender(vertx.eventBus(), context.deploymentID())
    }
}

fun main() {

//    val mathService = object : MathService {
//        override fun add(a: Int, b: Int): Promise<Int>{
//            return Promise.promise()
//        }
//    }
//
//    val proxy = Proxy.newProxyInstance(
//        MathService::class.java.classLoader,
//        arrayOf(MathService::class.java),
//        DynamicProxyHandler(mathService)
//    )  as MathService
//    print(proxy.add(4,5))
//
//
//    val jsonStr = Json.encode(mathService.add(1,2))
//
//    println()
//    println("JsonRes: $jsonStr")

    Vertx.vertx().deployVerticle(Verticle_A())
}