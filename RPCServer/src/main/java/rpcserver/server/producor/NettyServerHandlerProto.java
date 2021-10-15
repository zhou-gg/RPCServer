package rpcserver.server.producor;

import com.alibaba.fastjson.JSONException;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
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
        RpcProto.RpcRequest rpcRequest = (RpcProto.RpcRequest) msg;
        log.info("得到消息："+rpcRequest);
        RpcProto.RpcResponse.Builder builder = RpcProto.RpcResponse.newBuilder();
        try{
            if("heartBeat".equalsIgnoreCase(rpcRequest.getMethodName())){
                log.info("<RPC heartBeat>");
            }else {
                log.info("<RPC客户端请求> 接口名称: [{}] , 方法名称: [{}]",rpcRequest.getClassName(),rpcRequest.getMethodName());
                String id = rpcRequest.getId();
                builder.setRequestId(id);
                try{
                    System.out.println();
                }catch (Throwable throwable) {
                    log.info("<RPC客户端请求> 请求处理出现异常");
                    throwable.printStackTrace();
                }
            }
            builder.setCode(200);
        }catch (JSONException exception){
            log.info("<RPC客户端请求>请求数据格式转换错误");
            builder.setErrorMsg(getStackTrace(exception));
        }catch (Exception exception){
            log.info("<RPC客户端请求>出现错误");
            builder.setErrorMsg(getStackTrace(exception));
        }
        channelHandlerContext.writeAndFlush(builder.build());
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

    public static String getStackTrace(Throwable t) {
        try(StringWriter sw = new StringWriter();PrintWriter pw = new PrintWriter(sw, true)){
            t.printStackTrace(pw);
            return sw.toString();
        } catch (IOException e) {
            return "《读取异常错误：IOException》";
        }
    }
}
