package rpcserver.server.producor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
            String methodName = rpcRequest.getMethodName();
            if("heartBeat".equalsIgnoreCase(methodName)){
                log.info("<RPC heartBeat>");
            }else {
                String className = rpcRequest.getClassName();
                log.info("<RPC客户端请求> 接口名称: [{}] , 方法名称: [{}]", className, methodName);
                builder.setRequestId(rpcRequest.getId());
                try{
                    Object handler = handler(className, methodName, rpcRequest.getParamsList());
                    if(handler!=null&&!handler.equals("")){
                        Class<?> aClass = handler.getClass();
                        Any.Builder resultBuild;
                        if(aClass.isPrimitive()||aClass==String.class||aClass==Long.class||aClass==Double.class||aClass==Float.class||aClass==Boolean.class||aClass==StringBuffer.class){
                            resultBuild = Any.newBuilder().setValue(ByteString.copyFrom(handler.toString(), "UTF-8"));
                        }else {
                            resultBuild = Any.newBuilder().setValue(ByteString.copyFrom(JSON.toJSONString(handler), "UTF-8"));
                        }
                        builder.addResult(resultBuild.build());
                    }
                    builder.setErrorMsg("成功");
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

    private Object handler(String className, String methodName, List<com.google.protobuf.Any> params) throws Exception {
        Object serviceBean = null;
        Set<Map.Entry<String, Object>> entries = serviceMap.entrySet();
        for(Map.Entry<String, Object> entry:entries){
            if(entry.getKey().equalsIgnoreCase(className)){
                serviceBean=entry.getValue();
            }
        }
        if (serviceBean!=null){
            Class<?> serviceClass = serviceBean.getClass();
            Method[] methods = serviceClass.getMethods();
            for (Method method:methods){
                if(method.getName().equals(methodName)){
                    method.setAccessible(true);
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    return method.invoke(serviceBean, getParameters(parameterTypes, params));
                }
            }
        }
        throw new Exception("未找到服务接口,请检查配置!:"+className+"#"+methodName);
    }
    /**
     * 获取参数列表
     * @param parameterTypes
     * @param params
     */
    private Object[] getParameters(Class<?>[] parameterTypes,List<com.google.protobuf.Any> params){
        if (params==null || params.size()==0){
            return new Object[0];
        }else{
            Object[] new_parameters = new Object[params.size()];
            for(int i=0;i<params.size();i++){
                Any any = params.get(i);
                Class<?> parameterType = parameterTypes[i];
                if(parameterType.isInstance(any.getValue().toString(Charset.forName("UTF-8")))){
                    new_parameters[i] = parameterType.cast(any.getValue().toString(Charset.forName("UTF-8")));
                }else if(parameterType.isAssignableFrom(List.class)){
                    new_parameters[i] = JSONArray.parseArray(any.getValue().toString(Charset.forName("UTF-8")), parameterType);
                }else {
                    new_parameters[i] = JSON.parseObject(any.getValue().toString(Charset.forName("UTF-8")), parameterType);
                }
            }
            return new_parameters;
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

    public static String getStackTrace(Throwable t) {
        try(StringWriter sw = new StringWriter();PrintWriter pw = new PrintWriter(sw, true)){
            t.printStackTrace(pw);
            return sw.toString();
        } catch (IOException e) {
            return "《读取异常错误：IOException》";
        }
    }
}
