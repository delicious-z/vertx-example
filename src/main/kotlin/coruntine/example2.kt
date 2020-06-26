package coruntine

import io.vertx.core.AbstractVerticle
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.awaitResult
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("log")

suspend fun awaitResultExample(eventbus: EventBus) {
    log.info("Send a message and wait for a reply...")
    val reply = awaitResult<Message<String>> { h ->
        eventbus.request("a.b.c", "ping", h)
    }
    log.info("Reply received: ${reply.body()}")
}

private class ConsumerVerticle : AbstractVerticle() {
    override fun start() {
        val consumer = vertx.eventBus().localConsumer<String>("a.b.c")
        consumer.handler {
            log.info("Consumer received: ${it.body()}")
            val request = it
            vertx.setTimer(3000) { request.reply("this is reply") }
        }
    }
}

private class SenderVerticle : CoroutineVerticle() {
    override suspend fun start() {
        vertx.setPeriodic(1000) { log.info("periodic job.") }
        awaitResultExample(vertx.eventBus())
    }
}

fun main() {
    val vertx = Vertx.vertx()
    vertx.deployVerticle(ConsumerVerticle())
        .onSuccess { vertx.deployVerticle(SenderVerticle()) }
}