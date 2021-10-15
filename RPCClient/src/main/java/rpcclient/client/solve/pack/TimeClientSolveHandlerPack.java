/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package rpcclient.client.solve.pack;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import rpcclient.client.test.RpcRequest;

/**
 * @author lilinfeng
 * @date 2014年2月14日
 * @version 1.0
 */
public class TimeClientSolveHandlerPack extends ChannelHandlerAdapter {

    private int counter;
    private byte[] req;

    /**
     * Creates a client-side handler.
     */
    public TimeClientSolveHandlerPack() {
        req = ("QUERY TIME ORDER" + System.getProperty("line.separator")).getBytes();

    }

    public void channelActive(ChannelHandlerContext ctx) {
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setId("6666666");
        rpcRequest.setClassName("UserServiceImpl");
        rpcRequest.setMethodName("name");
        Class<String>[] clazz = new Class[1];
        clazz[0]=String.class;
        //rpcRequest.setParameterTypes(clazz);
        String[] params = new String[1];
        params[0]="测试名称";
        //rpcRequest.setParameters(params);
        req=JSON.toJSONString(rpcRequest).getBytes();
        for (int i=0;i<100;i++){
            ByteBuf firstMessage = Unpooled.buffer(req.length);
            firstMessage.writeBytes(req);
            ctx.writeAndFlush(firstMessage);
        }
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String body = (String) msg;
	    System.out.println("Now is : " + body + " is counter :" + ++counter);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
	    //释放资源
	    ctx.close();
    }
}
