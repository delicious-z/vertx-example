package basic

import io.vertx.core.Vertx

fun main() {
    val vertx = Vertx.vertx()
    vertx.executeBlocking<Any>({ promise ->
        // Call some blocking API that takes a significant amount of time to return
        val result = blockingMethod("hello")
        promise.complete(result)
    }, { res ->
        println("The result is: ${res.result()}")
        vertx.close()
    }
    )
}

private fun blockingMethod(s: String): Any? {
    Thread.sleep(2000)
    return "$s world"
}


