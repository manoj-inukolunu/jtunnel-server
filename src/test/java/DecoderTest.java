import codec.MessageDecoder;
import codec.MessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
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
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import proto.Message;
import test.DestHttpResponseHandler;


public class DecoderTest {

  @Test
  public void sedMessage() throws Exception {
    EventLoopGroup group = new NioEventLoopGroup();
    Bootstrap b = new Bootstrap();
    b.option(ChannelOption.SO_KEEPALIVE, true).group(group).channel(NioSocketChannel.class)
        .handler(new ChannelInitializer<SocketChannel>() {
          @Override
          protected void initChannel(SocketChannel socketChannel) throws Exception {
            socketChannel.pipeline().addLast(new MessageEncoder(), new MessageDecoder(), new EchoClientHandler());
          }
        });
    ChannelFuture f = b.connect("localhost", 1234).sync();
    // Wait until the connection is closed.
    f.channel().closeFuture().sync();
  }


  static class EchoClientHandler extends ChannelInboundHandlerAdapter {

    private List<Message> messages = Collections.synchronizedList(new ArrayList<>());

    public EchoClientHandler() {

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
      Message message = new Message();
      message.setType(1);
      message.setMessageId(UUID.randomUUID().toString());
      message.setTokenLen("bcddbbb3-55c0-4a56-bd43-ce52caf8e564".length());
      message.setToken("bcddbbb3-55c0-4a56-bd43-ce52caf8e564");
      message.setDataLen("localhost:8080".length());
      message.setData("localhost:8080".getBytes(StandardCharsets.UTF_8));
      ctx.writeAndFlush(message);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
      Message message = (Message) msg;
      //Read all the messages in the current stream
      //terminal packet
      if (message.getType() == -1) {
        List<Byte> bytes = new ArrayList<>();
        messages.forEach(mess -> {
          for (byte b : mess.getData()) {
            bytes.add(b);
          }
        });
        byte[] data = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++) {
          data[i] = bytes.get(i);
        }
        HttpRequestDecoder decoder = new HttpRequestDecoder();
        EmbeddedChannel embeddedChannel =
            new EmbeddedChannel(decoder, new HttpObjectAggregator(Integer.MAX_VALUE), new LoggingHandler(LogLevel.INFO),
                new HttpRequestInoundHandler(ctx));
        embeddedChannel.writeInbound(Unpooled.copiedBuffer(data));
        messages.clear();
      } else {
        messages.add(message);
      }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
      ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
      // Close the connection when an exception is raised.
      cause.printStackTrace();
      ctx.close();
    }

    static class HttpRequestInoundHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

      private ChannelHandlerContext parentContext;

      public HttpRequestInoundHandler(ChannelHandlerContext ctx) {
        parentContext = ctx;
      }

      @Override
      protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        localHttpRequest(msg.retain());
      }


      public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {

      }

      public void localHttpRequest(FullHttpRequest fullHttpRequest) throws Exception {
        System.out.println("Making Local HTTp ");
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(group).channel(NioSocketChannel.class).remoteAddress(new InetSocketAddress("localhost", 2020)).handler(
            new ChannelInitializer<SocketChannel>() {
              @Override
              protected void initChannel(SocketChannel socketChannel) throws Exception {
                ChannelPipeline p = socketChannel.pipeline();
                p.addLast("log", new LoggingHandler(LogLevel.DEBUG));
                p.addLast("codec", new HttpClientCodec());
                p.addLast("aggregator", new HttpObjectAggregator(Integer.MAX_VALUE));
                p.addLast("handler", new DestHttpResponseHandler(parentContext));
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


  }


}
