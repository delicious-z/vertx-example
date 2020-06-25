package coruntine

import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.awaitEvent
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

suspend fun main() {
    val vertx = Vertx.vertx()

    /**
     * The vertx.dispatcher() returns a coroutine dispatcher that
     * execute coroutines using the Vert.x event loop.
     */
    GlobalScope.launch(vertx.dispatcher()) {
        /**
         * The awaitEvent function suspends the execution of the coroutine until
         * the timer fires and resumes the coroutines with the value that
         * was given to the handler.
         */
        val timerId = awaitEvent<Long> { handler->
            vertx.setTimer(1000,handler)
        }
        println("Event fired from timer with id $timerId")
    }
}