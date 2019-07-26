package net.nextabc.edgex;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.log4j.Log4j;

import java.util.concurrent.TimeUnit;

/**
 * @author 陈哈哈 (yoojiachen@gmail.com)
 */
@Log4j
public class ExecutorImpl implements Executor {

    private static final ExpiringMap<String, ManagedChannel> CACHE_CHANNELS = new ExpiringMap<>(ManagedChannel::shutdown);

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(CACHE_CHANNELS::clear));
    }

    private final boolean rpcKeepAlive;
    private final int rpcKeepAliveTimeoutSec;
    private final int rpcConnectionCacheTTL;

    ExecutorImpl(Globals globals) {
        this.rpcKeepAlive = globals.isGrpcKeepAlive();
        this.rpcKeepAliveTimeoutSec = globals.getGrpcKeepAliveTimeoutSec();
        this.rpcConnectionCacheTTL = globals.getGrpcConnectionCacheTTL();
    }

    @Override
    public Message execute(String endpointAddress, Message in, int timeoutSec) throws Exception {
        log.debug("GRPC调用Endpoint: " + endpointAddress);
        final ManagedChannel channel = getChannel(endpointAddress);
        final ExecuteGrpc.ExecuteFutureStub stub = ExecuteGrpc.newFutureStub(channel);
        final Data req = Data.newBuilder()
                .setFrames(ByteString.copyFrom(in.bytes()))
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
        if (rpcKeepAlive) {
            builder.keepAliveWithoutCalls(true)
                    .keepAliveTimeout(rpcKeepAliveTimeoutSec, TimeUnit.SECONDS);
        }
        final ManagedChannel newChannel = builder.build();
        CACHE_CHANNELS.put(endpointAddress, newChannel, rpcConnectionCacheTTL);
        return newChannel;
    }
}
