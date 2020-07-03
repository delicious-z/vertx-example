package asyncRpc

import io.vertx.core.Vertx
import io.vertx.core.json.Json

interface ServiceDiscoveryClient {
    fun register(service: Any)
}

abstract class AbstractServiceDiscoveryClient:ServiceDiscoveryClient{

}

class VertxServiceDiscoveryClient(val vertx: Vertx,val address:String):AbstractServiceDiscoveryClient(){

    override fun register(service: Any) {
        val serviceInfo = ServiceInfo(getRpcClassStr(service),address)
        println("publish service: $serviceInfo")
        vertx.eventBus().publish(AsyncRpcConstants.SERVICE_PUBLISH_ADDRESS,Json.encode(serviceInfo))
    }
}
