package sync;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.sync.Sync;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * Vert.x uses Quasar which implements fibers by using byte code instrumentation.
 * This is done at run-time using a java agent.
 *
 *  In order for this to work you must
 *  start the JVM specifying the java agent jar
 *  which is located in the quasar-core jar.
 */
@Slf4j
public class Example1 {

    private static class SampleProvider extends AbstractVerticle{
        @Override
        public void start() {
            vertx.eventBus().localConsumer("com")
                    .handler(message->{
                        vertx.setTimer(2000,id->message.reply("result message"));
//                        Sync.awaitEvent(handler -> vertx.setTimer(2000,handler));
                    });
        }
    }

    private static class SampleReceiver extends AbstractVerticle{
        @Override
        public void start() {
            vertx.setPeriodic(2000,id->log.info("periodic job..."));
            EventBus bus = vertx.eventBus();
            Message<String> reply = Sync.awaitEvent(handler->bus.request("com",1));
            log.info(reply.body());
        }
    }

    public static void main(String[] args) {
        val vertx = Vertx.vertx();
        vertx.deployVerticle(new SampleProvider())
                .onSuccess(id->vertx.deployVerticle(new SampleReceiver()))
                .onSuccess(id->log.info("start success."));
    }
}
