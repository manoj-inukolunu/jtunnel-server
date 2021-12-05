package com.jtunnel.server;

import static com.jtunnel.server.AppData.channelMap;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import java.util.UUID;
import proto.Message;


@Sharable
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {


  static class HttpMessageHandler extends ChannelOutboundHandlerAdapter {

    private final ChannelPipeline pipeline;
    private final String token;
    private final String messageId;

    public HttpMessageHandler(ChannelPipeline channel, String token, String messageId) {
      this.pipeline = channel;
      this.token = token;
      this.messageId = messageId;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
      ByteBuf byteBuf = (ByteBuf) msg;
      byte[] data = ByteBufUtil.getBytes(byteBuf.retain());
      Message message = new Message();
      message.setType(2);
      message.setMessageId(messageId);
      message.setTokenLen(token.length());
      message.setToken(token);
      message.setDataLen(data.length);
      message.setData(data);
      pipeline.writeAndFlush(message).syncUninterruptibly();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
      cause.printStackTrace();
      super.exceptionCaught(ctx, cause);
    }
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest) throws Exception {
    FullHttpRequest request = fullHttpRequest.retain();
    try {
      String subdomain = request.headers().get("host");
      String messageId = UUID.randomUUID().toString();
//      AppData.httpChannelMap.put(AppData.hostToUUIDMap.get(subdomain), ctx.pipeline());
      ChannelPipeline clientPipeline = channelMap.get(subdomain);
      if (clientPipeline != null) {
        EmbeddedChannel embeddedChannel =
            new EmbeddedChannel(new HttpMessageHandler(clientPipeline, AppData.hostToUUIDMap.get(subdomain), messageId),
                new HttpObjectAggregator(Integer.MAX_VALUE), new HttpRequestEncoder());
        ChannelFuture future = embeddedChannel.writeAndFlush(request);
        Message last = new Message();
        last.setType(-1);
        last.setMessageId(messageId);
        last.setTokenLen(AppData.hostToUUIDMap.get(subdomain).length());
        last.setToken(AppData.hostToUUIDMap.get(subdomain));
        last.setDataLen(0);
        last.setData(new byte[] {});
        clientPipeline.writeAndFlush(last);
        future.addListener(new GenericFutureListener<Future<? super Void>>() {
          @Override
          public void operationComplete(Future<? super Void> future) throws Exception {
            System.out.println("HEre");
          }
        });
      } else {
        System.out.println("Channel Not found for " + subdomain);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
