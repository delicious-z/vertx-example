package eventBus

import io.vertx.core.AbstractVerticle
import io.vertx.core.Vertx
import org.slf4j.LoggerFactory

private class A: AbstractVerticle(){
    override fun start() {
        val bus = vertx.eventBus()
        bus.consumer<Any>("com")
            .handler { request-> run {
                println("A: ${request.body()}")
            } }
    }
}

private class B: AbstractVerticle() {
    override fun start() {
        val bus = vertx.eventBus()
        bus.consumer<Any>("com")
            .handler { request ->
                run {
                    println("B: ${request.body()}")
                }
            }
    }
}

private class C: AbstractVerticle(){
    override fun start() {
        val bus = vertx.eventBus()
        vertx.setPeriodic(1000) {
            run {
                bus.publish("com", 1)
            }
        }
    }
}

sealed class PublishExample

fun main() {
    val vertx = Vertx.vertx()
    val log = LoggerFactory.getLogger(PublishExample::class.java)
    vertx.deployVerticle(A())
        .onSuccess { vertx.deployVerticle(B()) }
        .onSuccess { vertx.deployVerticle(C()) }
        .onComplete {
            if (it.succeeded()) log.info("all success.")
            else log.error("failed.",it.cause())
        }
}