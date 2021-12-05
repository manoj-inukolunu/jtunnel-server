package client;

import codec.ProtoMessageDecoder;
import codec.ProtoMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;

public class TunnelClient {

  public static void main(String[] args) {
    NioEventLoopGroup group = new NioEventLoopGroup();
    Bootstrap bootstrap = new Bootstrap();
    try {
      bootstrap.group(group)
          .channel(NioSocketChannel.class)
          .option(ChannelOption.TCP_NODELAY, Boolean.TRUE)
          .handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
              ChannelPipeline pipeline = ch.pipeline();
              pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
              pipeline.addLast(new LengthFieldPrepender(4));
              pipeline.addLast(new IdleStateHandler(1, 2, 0));
              pipeline.addLast(new ProtoMessageEncoder());
              pipeline.addLast(new ProtoMessageDecoder());
              pipeline.addLast(new TunnelClientMessageHandler());
            }
          });

      ChannelFuture future = bootstrap.connect("manoj.jtunnel.net", 8585).sync();
      future.channel().closeFuture().sync();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      group.shutdownGracefully();
    }
  }
}






