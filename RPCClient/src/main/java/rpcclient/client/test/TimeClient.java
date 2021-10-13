package rpcclient.client.test;

import com.alibaba.fastjson.JSON;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.springframework.beans.factory.annotation.Value;

public class TimeClient {

//    @Value("${rpc.server.address}")
    private static String host = "127.0.0.1";

//    @Value("${rpc.server.address}")
    private static int port = 8081;

    public static void RPCRun(RpcRequest rpcRequest) throws Exception {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap(); // (1)
            b.group(workerGroup); // (2)
            b.channel(NioSocketChannel.class); // (3)
            b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new TimeClientHandler());
                }
            });

            // Start the client.
            ChannelFuture f = b.connect(host, port).sync(); // (5)
            Channel channel = f.channel();
            channel.writeAndFlush(JSON.toJSONString(rpcRequest));
            // Wait until the connection is closed.
            channel.closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setId("6666666");
        rpcRequest.setClassName("UserServiceImpl");
        rpcRequest.setMethodName("name");
        Class<String>[] clazz = new Class[1];
        clazz[0]=String.class;
        rpcRequest.setParameterTypes(clazz);
        String[] params = new String[1];
        params[0]="测试名称";
        rpcRequest.setParameters(params);
        TimeClient.RPCRun(rpcRequest);
        RpcResponse rpcResponse = RpcResult.get("6666666");
        System.out.println(JSON.toJSONString(rpcResponse));
    }
}