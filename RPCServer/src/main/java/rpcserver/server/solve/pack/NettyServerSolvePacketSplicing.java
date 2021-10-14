package rpcserver.server.solve.pack;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

public class NettyServerSolvePacketSplicing {

    public void bind(int port) throws Exception{
        //配置服务端的NIO线程组：这两个线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try{
            ServerBootstrap s = new ServerBootstrap();
            s.group(bossGroup,workerGroup)
                    //创建ServerSocketChannel 为非堵塞模式
                    .channel(NioServerSocketChannel.class)
                    //绑定监听，配置TCP参数等
                    .option(ChannelOption.SO_BACKLOG,1024)
                    .childHandler(new ChildChannelHandler());
            //等待服务端关闭
            ChannelFuture f = s.bind(port).sync();
            f.channel().closeFuture().sync();
        }catch (Exception exception){

        }finally {
            //优雅的退出释放资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private class ChildChannelHandler extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel arg0) throws Exception {
            arg0.pipeline().addLast(new TimeServerSolveHandlerPack());
            //防止出现 PacketSplicing
            arg0.pipeline().addLast(new StringDecoder());
            arg0.pipeline().addLast(new LineBasedFrameDecoder(1024));
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 8081;
        new NettyServerSolvePacketSplicing().bind(port);
    }

}
