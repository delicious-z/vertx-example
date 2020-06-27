package eventBus

import io.vertx.core.AbstractVerticle
import io.vertx.core.Vertx
import org.slf4j.LoggerFactory
import kotlin.math.log

class SampleConsumerA: AbstractVerticle(){
    override fun start() {
        val bus = vertx.eventBus()
        val log = LoggerFactory.getLogger(this.javaClass)
        bus.consumer<Any>("com")
            .handler { request-> run {
                log.info("A: ${request.body()}")
            } }
    }
}

class SampleConsumerB: AbstractVerticle() {
    override fun start() {
        val log = LoggerFactory.getLogger(this.javaClass)
        val bus = vertx.eventBus()
        bus.consumer<Any>("com")
            .handler { request ->
                run {
                    log.info("B: ${request.body()}")
                }
            }
    }
}

class SamplePublisher: AbstractVerticle(){
    override fun start() {
        val bus = vertx.eventBus()
        vertx.setPeriodic(1500) {
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
        .onSuccess { vertx.deployVerticle(SamplePublisher()) }
        .onComplete {
            if (it.succeeded()) log.info("all success.")
            else log.error("failed.",it.cause())
        }
}