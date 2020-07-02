package asyncRpc

import io.vertx.core.Promise

interface RpcInterface{
    fun getInstance():Any
}

interface MathService:RpcInterface {
    fun add(a:Int,b:Int):Promise<Int>

    override fun getInstance(): MathService {
        return object :MathService{
            override fun add(a: Int, b: Int): Promise<Int> {
                return Promise.promise()
            }
        }
    }
}