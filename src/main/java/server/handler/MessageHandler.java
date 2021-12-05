package server.handler;

import io.netty.channel.ChannelHandlerContext;
import server.ProtoMessage;

public interface MessageHandler {

  void handleMessage(ChannelHandlerContext ctx, ProtoMessage message);

}
