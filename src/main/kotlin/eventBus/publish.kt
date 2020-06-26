package eventBus

import io.vertx.core.AbstractVerticle
import io.vertx.core.Vertx
import org.slf4j.LoggerFactory

class SampleConsumerA: AbstractVerticle(){
    override fun start() {
        val bus = vertx.eventBus()
        bus.consumer<Any>("com")
            .handler { request-> run {
                println("A: ${request.body()}")
            } }
    }
}

class SampleConsumerB: AbstractVerticle() {
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

class C: AbstractVerticle(){
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
    vertx.deployVerticle(SampleConsumerA())
        .onSuccess { vertx.deployVerticle(SampleConsumerB()) }
        .onSuccess { vertx.deployVerticle(C()) }
        .onComplete {
            if (it.succeeded()) log.info("all success.")
            else log.error("failed.",it.cause())
        }
}