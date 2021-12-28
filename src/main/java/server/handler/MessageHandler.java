package server.handler;

import io.netty.channel.ChannelHandlerContext;
import proto.ProtoMessage;

public interface MessageHandler {

  void handleMessage(ChannelHandlerContext ctx, ProtoMessage message);

}
