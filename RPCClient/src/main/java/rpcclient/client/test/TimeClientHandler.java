package rpcclient.client.test;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class TimeClientHandler extends ChannelHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        RpcResponse rpcResponse = JSON.parseObject(msg.toString(),RpcResponse.class);
        RpcResult.add(rpcResponse);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)   {
        log.info("<Netty:RPC服务捕捉异常> 通道名称:[{}] , 异常信息 {}",ctx.name(),cause.getMessage());
        ctx.close();
        log.info("<Netty:通道已关闭>");
    }
}