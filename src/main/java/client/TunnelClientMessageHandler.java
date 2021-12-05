package client;


import com.google.common.primitives.Bytes;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;

import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import server.MessageTypeEnum;
import server.ProtoMessage;

public class TunnelClientMessageHandler extends SimpleChannelInboundHandler<ProtoMessage> {

  private final Map<String, List<ProtoMessage>> map = new ConcurrentHashMap<>();

  static class HttpRequestInboundHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private ChannelHandlerContext parentContext;
    private final String sessionId;

    public HttpRequestInboundHandler(ChannelHandlerContext ctx, String sessionId) {
      parentContext = ctx;
      this.sessionId = sessionId;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
      localHttpRequest(msg.retain());
    }


    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {

    }

    public void localHttpRequest(FullHttpRequest fullHttpRequest) throws Exception {
      EventLoopGroup group = new NioEventLoopGroup();
      Bootstrap b = new Bootstrap();
      b.group(group).channel(NioSocketChannel.class).remoteAddress(new InetSocketAddress("localhost", 2020)).handler(
          new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
              ChannelPipeline p = socketChannel.pipeline();
              p.addLast("codec", new HttpClientCodec());
              p.addLast("aggregator", new HttpObjectAggregator(Integer.MAX_VALUE));
              p.addLast("handler", new LocalHttpResponseHandler(parentContext, sessionId));
            }
          });
      Channel channel = b.connect().sync().channel();
      ChannelFuture f = channel.writeAndFlush(fullHttpRequest);
      f.addListener(future -> {
        if (!future.isSuccess()) {
          future.cause().printStackTrace();
        }
      });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
      cause.printStackTrace();
      super.exceptionCaught(ctx, cause);
    }
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    ProtoMessage registerMessage = new ProtoMessage();
    registerMessage.setAttachments(new HashMap<>());
    registerMessage.addAttachment("subdomain", "manoj");
    registerMessage.setMessageType(MessageTypeEnum.REGISTER);
    registerMessage.setSessionId(UUID.randomUUID().toString());
    ctx.writeAndFlush(registerMessage);
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, ProtoMessage msg) throws Exception {
    if (msg.getMessageType().equals(MessageTypeEnum.FIN)) {
      //initiate local http request
      HttpRequestDecoder decoder = new HttpRequestDecoder();
      EmbeddedChannel embeddedChannel =
          new EmbeddedChannel(decoder, new HttpObjectAggregator(Integer.MAX_VALUE),
              new HttpRequestInboundHandler(ctx, msg.getSessionId()));
      embeddedChannel.writeInbound(Unpooled.copiedBuffer(getBytes(map.get(msg.getSessionId()))));
    } else {
      List<ProtoMessage> messages = map.getOrDefault(msg.getSessionId(), new ArrayList<>());
      messages.add(msg);
      map.put(msg.getSessionId(), messages);
    }
    System.out.println("Received Message " + msg.getSessionId());
    ProtoMessage message = new ProtoMessage();
    message.setSessionId(msg.getSessionId());
    message.setMessageType(MessageTypeEnum.ACK);
    ctx.writeAndFlush(message);
  }


  private ByteBuf getBytes(List<ProtoMessage> protoMessages) {
    byte[] data = protoMessages.get(0).getBody().getBytes(StandardCharsets.UTF_8);
    for (int i = 1; i < protoMessages.size(); i++) {
      data = Bytes.concat(data, protoMessages.get(i).getBody().getBytes());
    }
    return Unpooled.copiedBuffer(data);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    super.exceptionCaught(ctx, cause);
    cause.printStackTrace();
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    super.channelInactive(ctx);
    System.out.println("Channel Inactive");
  }
}







