package com.jtunnel.server;

import static com.jtunnel.server.AppData.channelMap;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class ClientChannelInboundHandler extends ChannelInboundHandlerAdapter {


  private Channel parentChannel;

  public ClientChannelInboundHandler() {
//    this.parentChannel = parentChannel;
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    ByteBuf buff = (ByteBuf) msg;
    buff.toString(Charset.defaultCharset());
//    channelMap.put(data, ctx.pipeline());
  }

}
