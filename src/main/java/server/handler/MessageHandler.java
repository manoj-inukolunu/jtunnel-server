package server.handler;

import io.netty.channel.ChannelHandlerContext;
import com.jtunnel.proto.ProtoMessage;

public interface MessageHandler {

  void handleMessage(ChannelHandlerContext ctx, ProtoMessage message);

}
