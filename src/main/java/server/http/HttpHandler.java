package server.http;

import static com.jtunnel.server.AppData.channelMap;


import com.jtunnel.proto.MessageType;
import com.jtunnel.server.AppData;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
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
import lombok.extern.slf4j.Slf4j;
import com.jtunnel.proto.ProtoMessage;


@Slf4j
public class HttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

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
      if (msg instanceof ProtoMessage) {
        pipeline.writeAndFlush(msg);
      } else {
        ByteBuf byteBuf = (ByteBuf) msg;
        int len = byteBuf.readableBytes();
        byte[] data = new byte[len];
        byteBuf.readBytes(data);
        ProtoMessage message = new ProtoMessage();
        message.setSessionId(messageId);
        message.setMessageType(MessageType.HTTP_RESPONSE);
        message.setBody(new String(data));
        pipeline.writeAndFlush(message);
      }
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
      String sessionId = UUID.randomUUID().toString();
      AppData.httpChannelMap.put(sessionId, ctx);
      ChannelPipeline clientPipeline = channelMap.get(subdomain);
      if (clientPipeline != null) {
        EmbeddedChannel embeddedChannel =
            new EmbeddedChannel(
                new HttpMessageHandler(clientPipeline, AppData.hostToUUIDMap.get(subdomain), sessionId),
                new HttpObjectAggregator(Integer.MAX_VALUE), new HttpRequestEncoder());
        ChannelFuture future = embeddedChannel.writeAndFlush(request);
        future.addListener(new GenericFutureListener<Future<? super Void>>() {
          @Override
          public void operationComplete(Future<? super Void> future) throws Exception {
            log.info("Converted FullHttpRequest to ProtoMessage with sessionId={}" + sessionId);
          }
        });
        embeddedChannel.writeAndFlush(ProtoMessage.finMessage(sessionId));
      } else {
        log.warn("No Client registered for host={}", subdomain);
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}






