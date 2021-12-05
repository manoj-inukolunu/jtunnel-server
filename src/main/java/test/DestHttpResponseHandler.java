package test;


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
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpResponseEncoder;
import java.util.UUID;
import lombok.extern.java.Log;
import proto.Message;

@Sharable
@Log
public class DestHttpResponseHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

  public static final String TOKEN = "bcddbbb3-55c0-4a56-bd43-ce52caf8e564";
  private final ChannelHandlerContext parentContext;

  static class HttpMessageHandler extends ChannelOutboundHandlerAdapter {

    private final ChannelPipeline pipeline;
    private final String messageId;

    public HttpMessageHandler(ChannelPipeline channel, String messageId) {
      this.pipeline = channel;
      this.messageId = messageId;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
      byte[] data = ByteBufUtil.getBytes((ByteBuf) msg);
      Message message = new Message();
      message.setType(2);
      message.setMessageId(messageId);
      message.setTokenLen(TOKEN.length());
      message.setToken(TOKEN);
      message.setDataLen(data.length);
      message.setData(data);
      pipeline.writeAndFlush(message);
    }
  }

  public DestHttpResponseHandler(ChannelHandlerContext parentContext) {
    this.parentContext = parentContext;
  }

  @Override
  protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpResponse fullHttpResponse)
      throws Exception {
    if (parentContext != null) {
      String messageId = UUID.randomUUID().toString();
      EmbeddedChannel embeddedChannel =
          new EmbeddedChannel(new HttpMessageHandler(parentContext.pipeline(), messageId),
              new HttpObjectAggregator(Integer.MAX_VALUE), new HttpResponseEncoder());
      ChannelFuture f = embeddedChannel.writeAndFlush(fullHttpResponse.retain());
      parentContext.writeAndFlush(Message.finMessage(messageId, TOKEN));
      f.addListener(future -> {
        if (!future.isSuccess()) {
          future.cause().printStackTrace();
        }
      });
    }

  }
}

