package server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import com.jtunnel.proto.ProtoMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TunnelServerHandler extends ChannelInboundHandlerAdapter {

  private final MessageHandlers handlers;

  public TunnelServerHandler(MessageHandlers handlers) {
    this.handlers = handlers;
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    log.info("Channel Active");
    super.channelActive(ctx);
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) {
    // Echo back the received object to the client.
    ProtoMessage message = (ProtoMessage) msg;
    handlers.handleMessage(ctx, message);
  }

  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) {
    ctx.flush();
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    cause.printStackTrace();
    ctx.close();
  }


}






