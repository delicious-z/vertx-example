package asyncRpc
import io.vertx.core.Promise
import java.lang.reflect.Method
import java.util.*
import java.util.stream.Collectors
import kotlin.reflect.full.superclasses


fun getMethodId(method: Method): String {
    return Arrays.stream(method.parameters)
        .map { it.type.name }
        .collect(Collectors.joining(" ", "${method.name} ", " ${method.genericReturnType}"))
}

fun getRpcClassStr(target:Any):String{
    return target::class.superclasses[0].toString()
}

fun main() {
    val mathService = object :MathService{
        override fun add(a: Int, b: Int): Promise<Int> {
            TODO("Not yet implemented")
        }
    }
    println(getRpcClassStr(mathService))
}