package test;


import com.google.common.primitives.Bytes;
import com.jtunnel.server.AppData;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import proto.Message;

@Sharable
public class MsgHandler extends ChannelInboundHandlerAdapter {

  Map<String, LinkedList<Message>> messageMap = new ConcurrentHashMap<>();


  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    super.channelActive(ctx);
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    Message message = (Message) msg;
    //Register message
    if (message.getType() == 1) {
      String domain = new String(message.getData());
      AppData.channelMap.put(domain, ctx.pipeline());
      AppData.hostToUUIDMap.put(domain, message.getToken());
    } else if (message.getType() == 2) {
      LinkedList<Message> linkedList = messageMap.getOrDefault(message.getMessageId(), new LinkedList<>());
      linkedList.add(message);
      messageMap.put(message.getMessageId(), linkedList);
    } else if (message.getType() == -1) {
      LinkedList<Message> list = messageMap.get(message.getMessageId());
      byte[] data = new byte[] {};
      for (Message m : list) {
        data = Bytes.concat(data, m.getData());
      }
      ChannelHandlerContext channelHandlerContext = AppData.httpChannelMap.get(message.getToken());
      HttpClientCodec codec = new HttpClientCodec();
      EmbeddedChannel embeddedChannel =
          new EmbeddedChannel(codec, new HttpObjectAggregator(Integer.MAX_VALUE), new LoggingHandler(LogLevel.INFO),
              new HttpResponseInboundHandler(channelHandlerContext.pipeline()));
      embeddedChannel.writeInbound(Unpooled.copiedBuffer(data));
      messageMap.remove(message.getMessageId());
    }
    super.channelRead(ctx, msg);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    cause.printStackTrace();
    super.exceptionCaught(ctx, cause);
  }

  static class HttpResponseInboundHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    private ChannelPipeline parentPipeline;

    public HttpResponseInboundHandler(ChannelPipeline ctx) {
      parentPipeline = ctx;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) throws Exception {
      ChannelFuture future = parentPipeline.writeAndFlush(msg.retain());
      future.addListener((ChannelFutureListener) future1 -> {
        if (!future1.isSuccess()) {
          future1.cause().printStackTrace();
        }
      });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
      cause.printStackTrace();
      super.exceptionCaught(ctx, cause);
    }
  }
}
