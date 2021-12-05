package client;


import com.google.common.primitives.Bytes;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpResponseEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.java.Log;
import server.MessageTypeEnum;
import server.ProtoMessage;

@Sharable
@Log
public class LocalHttpResponseHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

  private final String sessionId;
  private final ChannelHandlerContext parentContext;

  static class HttpMessageHandler extends ChannelOutboundHandlerAdapter {

    private final ChannelPipeline pipeline;
    private final String sessionId;

    public HttpMessageHandler(ChannelPipeline channel, String sessionId) {
      this.pipeline = channel;
      this.sessionId = sessionId;

    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
      if (msg instanceof ProtoMessage) {
        pipeline.writeAndFlush(msg).addListener(future -> System.out.println("Sent Message"));
      } else {
        ByteBuf byteBuf = (ByteBuf) msg;
        int len = byteBuf.readableBytes();
        byte[] data = new byte[len];
        byteBuf.readBytes(data);
        ProtoMessage message = new ProtoMessage();
        message.setSessionId(sessionId);
        message.setMessageType(MessageTypeEnum.HTTP_RESPONSE);
        message.setBody(new String(data));
        pipeline.writeAndFlush(message).addListener(future -> System.out.println("Sent Proto Message"));
      }
      ctx.writeAndFlush(msg);
    }
  }

  public LocalHttpResponseHandler(ChannelHandlerContext parentContext, String sessionId) {
    this.sessionId = sessionId;
    this.parentContext = parentContext;
  }

  @Override
  protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpResponse fullHttpResponse)
      throws Exception {
    EmbeddedChannel embeddedChannel =
        new EmbeddedChannel(new HttpMessageHandler(parentContext.pipeline(), sessionId),
            new HttpObjectAggregator(Integer.MAX_VALUE), new HttpResponseEncoder());
    ChannelFuture f = embeddedChannel.writeAndFlush(fullHttpResponse.retain());
    embeddedChannel.writeAndFlush(ProtoMessage.finResponseMessage(sessionId));
  }

}

