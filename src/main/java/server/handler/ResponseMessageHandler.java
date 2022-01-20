package server.handler;


import com.google.common.primitives.Bytes;
import com.jtunnel.server.AppData;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpResponseDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.NoArgsConstructor;
import com.jtunnel.proto.ProtoMessage;

@NoArgsConstructor
public class ResponseMessageHandler implements MessageHandler {


  Map<String, List<ProtoMessage>> map = new ConcurrentHashMap<>();

  static class HttpResponseTransformer extends SimpleChannelInboundHandler<FullHttpResponse> {

    private final ChannelHandlerContext parentContext;

    public HttpResponseTransformer(ChannelHandlerContext channel) {
      this.parentContext = channel;
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) throws Exception {
      ChannelFuture f = parentContext.writeAndFlush(msg.retain());
      f.addListener(future -> {
        if (future.isSuccess()) {
          parentContext.close();
        } else {
          future.cause().printStackTrace();
        }
      });
    }
  }

  @Override
  public void handleMessage(ChannelHandlerContext ctx, ProtoMessage message) {
    ChannelHandlerContext httpServerChannelCtx = AppData.httpChannelMap.get(message.getSessionId());
    if (httpServerChannelCtx != null) {
      if (message.getBody().isEmpty()) {
        ByteBuf data = getBytes(map.get(message.getSessionId()));
        EmbeddedChannel embeddedChannel =
            new EmbeddedChannel(new HttpResponseDecoder(), new HttpObjectAggregator(Integer.MAX_VALUE),
                new HttpResponseTransformer(httpServerChannelCtx));
        embeddedChannel.writeInbound(data);
      } else {
        List<ProtoMessage> messages = map.getOrDefault(message.getSessionId(), new ArrayList<>());
        messages.add(message);
        map.put(message.getSessionId(), messages);
      }
    }
  }

  private ByteBuf getBytes(List<ProtoMessage> protoMessages) {
    byte[] data = protoMessages.get(0).getBody().getBytes(StandardCharsets.UTF_8);
    for (int i = 1; i < protoMessages.size(); i++) {
      data = Bytes.concat(data, protoMessages.get(i).getBody().getBytes());
    }
    return Unpooled.copiedBuffer(data);
  }
}






