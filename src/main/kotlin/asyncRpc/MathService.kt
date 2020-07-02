package asyncRpc

import io.vertx.core.Promise


interface MathService {
    fun add(a: Int, b: Int): Promise<Int>
}
