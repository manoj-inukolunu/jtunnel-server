package server.handler;

import io.netty.channel.ChannelHandlerContext;
import server.ProtoMessage;

public class AckMessageHandler implements MessageHandler {

  @Override
  public void handleMessage(ChannelHandlerContext ctx, ProtoMessage message) {
    System.out.println("Acknowledged ");
  }
}






