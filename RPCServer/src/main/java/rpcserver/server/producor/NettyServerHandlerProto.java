package rpcserver.server.producor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Map;

@Slf4j
public class NettyServerHandlerProto extends ChannelHandlerAdapter {

    private final Map<String, Object> serviceMap;

    public NettyServerHandlerProto(Map<String, Object> serviceMap) {
        this.serviceMap = serviceMap;
    }
    @Override
    public void channelActive(ChannelHandlerContext ctx)   {
        log.info("客户端连接成功!"+ctx.channel().remoteAddress());
    }
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("客户端断开连接!{}", ctx.channel().remoteAddress());
        ctx.channel().close();
    }

    @Override
    public  void channelRead(ChannelHandlerContext channelHandlerContext, Object msg){
        log.info("得到消息："+msg);
        try{
            RpcRequest rpcRequest = JSON.parseObject((String) msg,RpcRequest.class);
            if("heartBeat".equalsIgnoreCase(rpcRequest.getMethodName())){
                log.info("<RPC heartBeat>");
            }else {
                log.info("<RPC客户端请求> 接口名称: [{}] , 方法名称: [{}]",rpcRequest.getClassName(),rpcRequest.getMethodName());
                RpcResponse rpcResponse = new RpcResponse();
                rpcResponse.setRequestId(rpcRequest.getId());
                try{
                    //Object handler = this.handler(rpcRequest);
                    //rpcResponse.setData(handler);
                    channelHandlerContext.writeAndFlush(rpcResponse);
                }catch (Throwable throwable) {
                    log.info("<RPC客户端请求> 请求处理出现异常");
                    throwable.printStackTrace();
                }
            }
        }catch (JSONException exception){
            log.info("<RPC客户端请求>请求数据格式转换错误");
            channelHandlerContext.writeAndFlush(exception);
            channelHandlerContext.close();
        }catch (Exception exception){
            log.info("<RPC客户端请求>出现错误");
            channelHandlerContext.writeAndFlush(exception);
            channelHandlerContext.close();
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt)throws Exception {
        if (evt instanceof IdleStateEvent){
            IdleStateEvent event = (IdleStateEvent)evt;
            if (event.state()== IdleState.ALL_IDLE){
                log.info("客户端已超过60秒未读写数据,关闭连接.{}",ctx.channel().remoteAddress());
                ctx.channel().close();
            }
        }else{
            super.userEventTriggered(ctx,evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)   {
        log.info("<Netty:RPC服务捕捉异常> 通道名称:[{}] , 异常信息 {}",ctx.name(),cause.getMessage());
        ctx.close();
        log.info("<Netty:通道已关闭>");
    }
}
