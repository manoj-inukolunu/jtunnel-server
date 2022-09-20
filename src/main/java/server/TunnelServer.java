package server;


import com.jtunnel.proto.MessageType;
import io.cronitor.client.CronitorClient;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLException;
import server.handler.AckMessageHandler;
import server.handler.MessageHandlers;
import server.handler.PingMessageHandler;
import server.handler.RegisterMessageHandler;
import server.handler.ResponseMessageHandler;
import server.handler.TunnelServerHandler;
import server.http.HttpServer;

public class TunnelServer {

  public static void main(String[] args) {
    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    scheduler.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        try {
          CronitorClient cronitorClient = new CronitorClient("d2c7f7b9bd0b4c3b95aa666ce629aba3");
          try {
            cronitorClient.tick("JTunnel", "Jtunnel server alive");
          } catch (IOException e) {
            e.printStackTrace();
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }, 0, 1, TimeUnit.MINUTES);

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
              handlers.register(MessageType.REGISTER, new RegisterMessageHandler());
              handlers.register(MessageType.HTTP_RESPONSE, new ResponseMessageHandler());
              handlers.register(MessageType.ACK, new AckMessageHandler());
              handlers.register(MessageType.PING, new PingMessageHandler());
              addProtoClientHandlers(pipeline, ch, handlers);
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

  public static void addProtoClientHandlers(ChannelPipeline pipeline, SocketChannel ch,
      MessageHandlers handlers)
      throws CertificateException, SSLException {
    /*pipeline.addLast("length-decoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 8, 0, 8));
    pipeline.addLast("length-prepender", new LengthFieldPrepender(8));
    pipeline.addLast("proto-message-encoder", new ProtoMessageEncoder());
    pipeline.addLast("proto-message-decoder", new ProtoMessageDecoder());*/
    SelfSignedCertificate ssc = new SelfSignedCertificate();
    SslContext sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
    pipeline.addLast(sslCtx.newHandler(ch.alloc()));
    pipeline.addLast("object-encoder", new ObjectEncoder());
    pipeline.addLast("object-decoder",
        new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(TunnelServer.class.getClassLoader())));
    pipeline.addLast("tunnel-server-handler", new TunnelServerHandler(handlers));
  }

}






