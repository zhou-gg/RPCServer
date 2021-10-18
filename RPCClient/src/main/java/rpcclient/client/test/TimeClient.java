package rpcclient.client.test;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.Any;
import com.google.protobuf.ByteOutput;
import com.google.protobuf.ByteString;
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

import java.nio.charset.Charset;

public class TimeClient {

    //@Value("${rpc.client.address}")
    private static String host = "127.0.0.1";

    //@Value("${rpc.client.port}")
    private static int port = 7777;

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
        Any zz = Any.newBuilder().setValue(ByteString.copyFrom("周洪召", Charset.forName("UTF-8"))).build();

        rpcRequest.setId("6666666");
        rpcRequest.setClassName("UserServiceImpl");
        rpcRequest.setMethodName("name");
        rpcRequest.addParamsBuilder(0);
        rpcRequest.setParams(0,zz);
        TimeClient.RPCRun(rpcRequest.build());
        RpcResponse rpcResponse = RpcResult.get("6666666");
        System.out.println("==============================输出结果集==============================");
        System.out.println(JSON.toJSONString(rpcResponse));
    }
}