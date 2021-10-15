package rpcclient.client.test;

import com.alibaba.fastjson.JSON;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import org.springframework.beans.factory.annotation.Value;

public class TimeClient {

    //@Value("${rpc.client.address}")
    private static String host = "localhost";

    //@Value("${rpc.client.port}")
    private static int port = 8081;

    public static void RPCRun(RpcProto.RpcRequest rpcRequest) throws Exception {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.TCP_NODELAY, true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new ProtobufVarint32FrameDecoder());
                    ch.pipeline().addLast(new ProtobufDecoder(RpcProto.RpcResponse.getDefaultInstance()));
                    ch.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
                    ch.pipeline().addLast(new ProtobufEncoder());
                    ch.pipeline().addLast(new TimeClientHandler(rpcRequest));
                }
            });
            // Start the client.
            ChannelFuture f = b.connect(host, port).sync();
            // Wait until the connection is closed.
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        RpcProto.RpcRequest.Builder rpcRequest = RpcProto.RpcRequest.newBuilder();
        rpcRequest.setId("6666");
        rpcRequest.setClassName("ClassName");
        rpcRequest.setMethodName("Method");
        TimeClient.RPCRun(rpcRequest.build());
        //RpcResponse rpcResponse = RpcResult.get("6666666");
        //System.out.println(JSON.toJSONString(rpcResponse));
    }
}