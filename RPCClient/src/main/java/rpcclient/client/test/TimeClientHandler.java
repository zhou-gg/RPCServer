package rpcclient.client.test;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class TimeClientHandler extends ChannelHandlerAdapter {

    private RpcRequest rpcResponse;

    public TimeClientHandler(RpcRequest rpcResponse){
        this.rpcResponse = rpcResponse ;
    }

    public void channelActive(ChannelHandlerContext ctx) {
        byte[] bytes = JSON.toJSONString(rpcResponse).getBytes();
        ByteBuf firstMessage = Unpooled.buffer(bytes.length);
        firstMessage.writeBytes(bytes);
        ctx.writeAndFlush(firstMessage);
        log.info("<发送信息>： {}",JSON.toJSONString(rpcResponse));
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        RpcResponse rpcResponse = JSON.parseObject(msg.toString(),RpcResponse.class);
        log.info("<得到消息>：" + JSON.toJSONString(rpcResponse));
        RpcResult.add(rpcResponse);
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)   {
        log.info("<Netty:RPC服务捕捉异常> 通道名称:[{}] , 异常信息 {}",ctx.name(),cause.getMessage());
        ctx.close();
        log.info("<Netty:通道已关闭>");
    }
}