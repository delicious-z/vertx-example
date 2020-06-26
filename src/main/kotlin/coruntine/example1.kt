package coruntine

import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.awaitEvent
import io.vertx.kotlin.coroutines.awaitResult
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

suspend fun main() {
    val vertx = Vertx.vertx()
    val log = LoggerFactory.getLogger("my log")

    /**
     * The vertx.dispatcher() returns a coroutine dispatcher that
     * execute coroutines using the Vert.x event loop.
     */
    log.info("1")
    GlobalScope.launch(vertx.dispatcher()) {
        /**
         * The awaitEvent function suspends the execution of the coroutine until
         * the timer fires and resumes the coroutines with the value that
         * was given to the handler.
         */
        val timerId = awaitEvent<Long> { handler->
            log.info("2")
            vertx.setTimer(1000,handler)
        }
        log.info("Event fired from timer with id $timerId")
    }
    log.info("3")
}