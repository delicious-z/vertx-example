package asyncRpc

import io.vertx.core.Vertx
import io.vertx.core.json.Json
import java.lang.reflect.Method

interface ServiceDiscoveryClient {
    fun register(service: Any)
    fun unRegister(service: Any)
}

abstract class AbstractServiceDiscoveryClient:ServiceDiscoveryClient{

}

class VertxServiceDiscoveryClient(val vertx: Vertx, private val consumeAddress:String):AbstractServiceDiscoveryClient(){

    init {
        vertx.eventBus().consumer<String>(consumeAddress){
            val rpcRequest = Json.decodeValue(it.body(),RpcRequest::class.java)
            invoke(rpcRequest,it.headers()[AsyncRpcConstants.RECEIVER_ADDRESS])
        }
    }

    private val serviceObjects:HashMap<String,Any> = HashMap()
    private val serviceMethods:HashMap<String,Method> = HashMap()

    private fun invoke(rpcRequest: RpcRequest,receiverAddr:String){
        println("invoke!")
        println(serviceMethods.size)
        println(serviceObjects.size)
        val obj = serviceObjects[rpcRequest.serviceClass]
        val method = serviceMethods[rpcRequest.methodId]
        vertx.executeBlocking<Any> {
            val res = method!!.invoke(obj, rpcRequest.args)
            vertx.eventBus().send(receiverAddr,Json.encode(res))
        }
    }

    override fun register(service: Any) {
        val serviceInfo = ServiceInfo(getRpcClassStr(service),consumeAddress)
        println("publish : $serviceInfo")
        serviceObjects[serviceInfo.serviceClass] = service
        for (m in getServiceMethods(service)){
            serviceMethods[getMethodId(m)] = m
        }
        vertx.eventBus().publish(AsyncRpcConstants.SERVICE_PUBLISH_ADDRESS,Json.encode(serviceInfo))
    }

    override fun unRegister(service: Any) {
        TODO("Not yet implemented")
    }

}
