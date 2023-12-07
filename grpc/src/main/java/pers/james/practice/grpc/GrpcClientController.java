package pers.james.practice.grpc;

import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.examples.lib.HelloReply;
import net.devh.boot.grpc.examples.lib.HelloRequest;
import net.devh.boot.grpc.examples.lib.SimpleGrpc;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Michael (yidongnan@gmail.com)
 * @since 2016/12/4
 */
@RestController
public class GrpcClientController {

    private static final Logger logger = Logger.getLogger(GrpcClientController.class.getName());

    @Value("${valueFromFile:123}")
    private String valueFromFile;

    @RequestMapping("/")
    public String printMessage(@RequestParam(defaultValue = "Michael") String name) throws Exception {
        logger.info("开始grpc " + name + " ...");
        logger.info("valueFromFile " + valueFromFile + " ...");
        String target = "192.168.0.106:9898";

        ManagedChannel channel = Grpc.newChannelBuilder(target, InsecureChannelCredentials.create())
                .build();
        try {
            logger.info("Will try to greet " + name + " ...");
            HelloRequest request = HelloRequest.newBuilder().setName(name).build();
            HelloReply response = null;
            try {
                response = SimpleGrpc.newBlockingStub(channel).sayHello(request);
            } catch (StatusRuntimeException e) {
                logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            }
            logger.info("Greeting: " + response.getMessage());
        } finally {
            // ManagedChannels use resources like threads and TCP connections. To prevent leaking these
            // resources the channel should be shut down when it will no longer be used. If it may be used
            // again leave it running.
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }

        return "";
    }

}