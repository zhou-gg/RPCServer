package rpcserver.server.solve.pack;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.util.Date;

public class TimeServerSolveHandlerPack extends ChannelHandlerAdapter {

    private int counter;

    public TimeServerSolveHandlerPack() {
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        String body = (String)msg;
        System.out.println("The time server receive order : " + body +" the counter is : " + ++counter);
        String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body) ? (new Date(System.currentTimeMillis())).toString() : "BAD ORDER";
        currentTime = currentTime + System.getProperty("line.separator");
        ByteBuf resp = Unpooled.copiedBuffer(currentTime.getBytes());
        ctx.writeAndFlush(resp);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
    }

}
