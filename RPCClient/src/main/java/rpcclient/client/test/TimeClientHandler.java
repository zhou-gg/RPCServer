package rpcclient.client.test;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.Any;
import com.google.protobuf.ProtocolStringList;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.util.List;


@Slf4j
public class TimeClientHandler extends ChannelHandlerAdapter {

    private RpcProto.RpcRequest rpcResponse;

    public TimeClientHandler(RpcProto.RpcRequest rpcResponse){
        this.rpcResponse = rpcResponse ;
    }

    public void channelActive(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(rpcResponse);
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg){
        try{
            RpcProto.RpcResponse response = (RpcProto.RpcResponse) msg;
            RpcResponse responseData = new RpcResponse();
            log.info("<得到消息>：" + response);
            responseData.setRequestId(response.getRequestId());
            responseData.setCode(response.getCode());
            responseData.setErrorMsg(response.getErrorMsg());
            List<Any> resultList = response.getResultList();
            if(resultList.size()>0){
                String[] results = new String[resultList.size()];
                Integer index = 0;
                for (Any any:resultList){
                    try {
                        results[index] = any.getValue().toString("UTF-8");
                        index++;
                    } catch (UnsupportedEncodingException e) {
                        log.info("<Netty:结果转换异常>：[{}]",e);
                    }
                }
                responseData.setResult(results);
            }
            RpcResult.add(responseData);
        }finally {
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)   {
        log.info("<Netty:RPC服务捕捉异常> 通道名称:[{}] , 异常信息 {}",ctx.name(),cause.getMessage());
        ctx.close();
        log.info("<Netty:通道已关闭>");
    }
}