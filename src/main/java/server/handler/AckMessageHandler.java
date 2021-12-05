package server.handler;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import server.ProtoMessage;

@Slf4j
public class AckMessageHandler implements MessageHandler {

  @Override
  public void handleMessage(ChannelHandlerContext ctx, ProtoMessage message) {
    log.info("Acknowledged message with sessionId={}", message.getSessionId());
  }
}






