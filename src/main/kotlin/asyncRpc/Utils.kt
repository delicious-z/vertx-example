package asyncRpc
import java.lang.reflect.Method
import java.util.*
import java.util.stream.Collectors



fun getMethodId(method: Method): String? {
    return Arrays.stream(method.parameters)
        .map { it.type.name }
        .collect(Collectors.joining(" ", "${method.name} ", " ${method.genericReturnType}"))
}