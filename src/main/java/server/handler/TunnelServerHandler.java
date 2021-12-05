package server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import server.ProtoMessage;

public class TunnelServerHandler extends SimpleChannelInboundHandler<ProtoMessage> {

  private final MessageHandlers handlers;

  public TunnelServerHandler(MessageHandlers handlers) {
    this.handlers = handlers;
  }


  @Override
  protected void channelRead0(ChannelHandlerContext ctx, ProtoMessage msg) throws Exception {
    handlers.handleMessage(ctx, msg);
  }
}






