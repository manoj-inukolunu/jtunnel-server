package server;

import codec.ProtoMessageDecoder;
import codec.ProtoMessageEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import java.util.UUID;
import server.handler.AckMessageHandler;
import server.handler.MessageHandlers;
import server.handler.RegisterMessageHandler;
import server.handler.ResponseMessageHandler;
import server.handler.TunnelServerHandler;
import server.http.HttpServer;

public class TunnelServer {

  public static void main(String[] args) {
    EventLoopGroup bossGroup = new NioEventLoopGroup();
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    try {
      ServerBootstrap bootstrap = new ServerBootstrap();
      bootstrap.group(bossGroup, workerGroup)
          .channel(NioServerSocketChannel.class)
          .option(ChannelOption.SO_BACKLOG, 1024)
          .handler(new LoggingHandler(LogLevel.INFO))
          .childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
              ChannelPipeline pipeline = ch.pipeline();
              MessageHandlers handlers = new MessageHandlers();
              handlers.register(MessageTypeEnum.REGISTER, new RegisterMessageHandler());
              handlers.register(MessageTypeEnum.HTTP_RESPONSE, new ResponseMessageHandler());
              handlers.register(MessageTypeEnum.ACK, new AckMessageHandler());
              addProtoClientHandlers(pipeline, handlers);
            }
          });

      ChannelFuture future = bootstrap.bind(8585).sync();
      HttpServer server = new HttpServer();
      server.run();
//      future.channel().closeFuture().sync();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      bossGroup.shutdownGracefully();
      workerGroup.shutdownGracefully();
    }
  }

  public static void addProtoClientHandlers(ChannelPipeline pipeline, MessageHandlers handlers) {
    pipeline.addLast("length-decoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
    pipeline.addLast("length-prepender", new LengthFieldPrepender(4));
    pipeline.addLast("proto-message-encoder", new ProtoMessageEncoder());
    pipeline.addLast("proto-message-decoder", new ProtoMessageDecoder());
    pipeline.addLast("tunnel-server-handler", new TunnelServerHandler(handlers));
  }

}






