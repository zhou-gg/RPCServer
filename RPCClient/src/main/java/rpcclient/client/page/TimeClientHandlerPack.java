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
package rpcclient.client.page;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.util.logging.Logger;

/**
 * @author lilinfeng
 * @date 2014年2月14日
 * @version 1.0
 */
public class TimeClientHandlerPack extends ChannelHandlerAdapter {

    private int counter;
    private byte[] req;

    /**
     * Creates a client-side handler.
     */
    public TimeClientHandlerPack() {
        req = ("QUERY TIME ORDER" + System.getProperty("line.separator")).getBytes();

    }

    public void channelActive(ChannelHandlerContext ctx) {
        for (int i=0;i<100;i++){
            ByteBuf firstMessage = Unpooled.buffer(req.length);
            firstMessage.writeBytes(req);
            ctx.writeAndFlush(firstMessage);
        }
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
	    ByteBuf buf = (ByteBuf) msg;
	    byte[] req = new byte[buf.readableBytes()];
	    buf.readBytes(req);
	    String body = new String(req, "UTF-8");
	    System.out.println("Now is : " + body + " is counter :" + ++counter);
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
	    //释放资源
	    ctx.close();
    }
}
