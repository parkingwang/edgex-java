package net.nextabc.edgex;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.apache.log4j.Logger;

import java.util.concurrent.TimeUnit;

/**
 * @author 陈哈哈 (yoojiachen@gmail.com)
 */
public class ExecutorImpl implements Executor {

    public static final Logger log = Logger.getLogger(ExecutorImpl.class);

    private static final ExpiringMap<String, ManagedChannel> CACHE_CHANNELS = new ExpiringMap<>(ManagedChannel::shutdown);

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(CACHE_CHANNELS::clear));
    }

    private final Globals globals;

    public ExecutorImpl(Globals globals) {
        this.globals = globals;
    }

    @Override
    public Message execute(String endpointAddress, Message in, int timeoutSec) throws Exception {
        log.debug("GRPC调用Endpoint: " + endpointAddress);
        final ManagedChannel channel = getChannel(endpointAddress);
        final ExecuteGrpc.ExecuteFutureStub stub = ExecuteGrpc.newFutureStub(channel);
        final Data req = Data.newBuilder()
                .setFrames(ByteString.copyFrom(in.getFrames()))
                .build();
        return Message.parse(stub
                .execute(req)
                .get(timeoutSec, TimeUnit.SECONDS)
                .getFrames()
                .toByteArray());
    }

    private synchronized ManagedChannel getChannel(String endpointAddress) {
        final ManagedChannel channel = CACHE_CHANNELS.get(endpointAddress);
        if (channel != null) {
            return channel;
        }
        final ManagedChannelBuilder builder = ManagedChannelBuilder
                .forTarget(endpointAddress)
                .usePlaintext();
        if (this.globals.grpcKeepAlive) {
            builder.keepAliveWithoutCalls(true)
                    .keepAliveTimeout(this.globals.grpcKeepAliveTimeoutSec, TimeUnit.SECONDS);
        }
        final ManagedChannel newChannel = builder.build();
        CACHE_CHANNELS.put(endpointAddress, newChannel, this.globals.grpcConnectionCacheTTL);
        return newChannel;
    }
}
