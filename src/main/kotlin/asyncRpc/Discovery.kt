package asyncRpc

import io.vertx.core.AbstractVerticle
import io.vertx.core.MultiMap
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.json.Json
import org.slf4j.LoggerFactory

class DiscoveryVerticle : AbstractVerticle() {
    private val log = LoggerFactory.getLogger("discovery-verticle")
    private val serverAddrMap = HashMap<String, HashSet<String>>()
    private val iteratorMap = HashMap<String, Iterator<String>>()

    private fun next(serviceClass: String): String {
        if (iteratorMap[serviceClass] == null || !iteratorMap[serviceClass]!!.hasNext()) {
            iteratorMap[serviceClass] = serverAddrMap[serviceClass]!!.iterator()
        }
        return iteratorMap[serviceClass]!!.next()
    }

    override fun start() {
        val bus = vertx.eventBus()
        // service register
        bus.consumer<String>(AsyncRpcConstants.SERVICE_PUBLISH_ADDRESS) {
            val serviceInfo: ServiceInfo = Json.decodeValue(it.body(), ServiceInfo::class.java)
            log.info("service register: $serviceInfo")
            serviceInfo.apply {
                serverAddrMap[serviceClass] = serverAddrMap[serviceClass] ?: HashSet()
                serverAddrMap[serviceClass]!!.add(address)
            }
        }

        // service request
        bus.consumer<String>(AsyncRpcConstants.SERVICE_REQUEST_ADDRESS) {
            val rpcRequest: RpcRequest = Json.decodeValue(it.body(), RpcRequest::class.java)
            log.info("service request: $rpcRequest")
            val receiverAddress: String = it.headers()[AsyncRpcConstants.RECEIVER_ADDRESS]
            if (serverAddrMap[rpcRequest.serviceClass] == null) {
                bus.send(receiverAddress, Json.encode(
                    RpcResponse(promiseId = rpcRequest.promiseId,cause = "No Such Service Provider!")
                ))
            } else {
                // send to serviceProvider
                kotlin.run {
                    val serverAddr: String = next(rpcRequest.serviceClass)
                    val deliveryOptions = DeliveryOptions().setHeaders(
                        MultiMap.caseInsensitiveMultiMap().add(AsyncRpcConstants.RECEIVER_ADDRESS, receiverAddress)
                    )
                    vertx.eventBus().send(serverAddr, it.body(), deliveryOptions)
                }
            }
        }
    }
}