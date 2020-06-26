package cluster.hazelcast

import eventBus.SampleConsumerA
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.spi.cluster.ClusterManager
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager
import org.slf4j.LoggerFactory

private class Consumer

fun main() {
    val log = LoggerFactory.getLogger(Consumer::class.java)
    val options = VertxOptions()
    val manager:ClusterManager = HazelcastClusterManager()
    options.clusterManager = manager

    val future = Vertx.clusteredVertx(options)
    future.onComplete {
        if (it.failed()) log.error("get cluster vertx failed.",it.cause())
        else {
            it.result().deployVerticle(SampleConsumerA())
        }
    }

}