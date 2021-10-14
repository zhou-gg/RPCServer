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
public class NettyServerHandler extends ChannelHandlerAdapter {

    private final Map<String, Object> serviceMap;

    public NettyServerHandler(Map<String, Object> serviceMap) {
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
            RpcRequest rpcRequest = JSON.parseObject(msg.toString(),RpcRequest.class);
            if("heartBeat".equalsIgnoreCase(rpcRequest.getMethodName())){
                log.info("<RPC heartBeat>");
            }else {
                log.info("<RPC客户端请求> 接口名称: [{}] , 方法名称: [{}]",rpcRequest.getClassName(),rpcRequest.getMethodName());
                RpcResponse rpcResponse = new RpcResponse();
                rpcResponse.setRequestId(rpcRequest.getId());
                try{
                    Object handler = this.handler(rpcRequest);
                    rpcResponse.setData(handler);
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

    /**
     * 通过反射，执行本地方法
     */
    private Object handler(RpcRequest request) throws Throwable{
        String className = request.getClassName();
        Object serviceBean = serviceMap.get(className);

        if (serviceBean!=null){
            Class<?> serviceClass = serviceBean.getClass();
            String methodName = request.getMethodName();
            Class<?>[] parameterTypes = request.getParameterTypes();
            Object[] parameters = request.getParameters();
            Method method = serviceClass.getMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return method.invoke(serviceBean, getParameters(parameterTypes,parameters));
        }else{
            throw new Exception("未找到服务接口,请检查配置!:"+className+"#"+request.getMethodName());
        }
    }

    /**
     * 获取参数列表
     * @param parameterTypes
     * @param parameters
     */
    private Object[] getParameters(Class<?>[] parameterTypes,Object[] parameters){
        if (parameters==null || parameters.length==0){
            return parameters;
        }else{
            Object[] new_parameters = new Object[parameters.length];
            for(int i=0;i<parameters.length;i++){
                new_parameters[i] = JSON.parseObject(parameters[i].toString(),parameterTypes[i]);
            }
            return new_parameters;
        }
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
