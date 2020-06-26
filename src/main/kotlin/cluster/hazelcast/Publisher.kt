package cluster.hazelcast

import eventBus.C
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.spi.cluster.ClusterManager
import io.vertx.spi.cluster.hazelcast.ConfigUtil
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager
import org.slf4j.LoggerFactory

private class Publisher

fun main() {
    val log = LoggerFactory.getLogger(Publisher::class.java)
    val options = VertxOptions()
    val config = ConfigUtil.loadConfig()
    val manager: ClusterManager = HazelcastClusterManager(config)
    options.clusterManager = manager

    val future = Vertx.clusteredVertx(options)
    future.onComplete {
        if (it.failed()) log.error("get cluster vertx failed.", it.cause())
        else {
            it.result().deployVerticle(C())
        }

    }
}