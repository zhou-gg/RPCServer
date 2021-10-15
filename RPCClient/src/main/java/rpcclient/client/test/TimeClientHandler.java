package rpcclient.client.test;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.ProtocolStringList;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class TimeClientHandler extends ChannelHandlerAdapter {

    private RpcProto.RpcRequest rpcResponse;

    public TimeClientHandler(RpcProto.RpcRequest rpcResponse){
        this.rpcResponse = rpcResponse ;
    }

    public void channelActive(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(rpcResponse);
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        RpcProto.RpcResponse response = (RpcProto.RpcResponse) msg;
        String errorMsg = response.getErrorMsg();
        log.info("<得到消息>：" + JSON.toJSONString(errorMsg));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)   {
        log.info("<Netty:RPC服务捕捉异常> 通道名称:[{}] , 异常信息 {}",ctx.name(),cause.getMessage());
        ctx.close();
        log.info("<Netty:通道已关闭>");
    }
}