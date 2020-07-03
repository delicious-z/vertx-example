package asyncRpc

import io.vertx.core.Future
import io.vertx.core.Promise


interface MathService {
    fun add(a: Int, b: Int): Future<Int>?
}
