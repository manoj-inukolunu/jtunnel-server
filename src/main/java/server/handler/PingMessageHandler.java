package server.handler;

import com.jtunnel.proto.ProtoMessage;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PingMessageHandler implements MessageHandler {

  @Override
  public void handleMessage(ChannelHandlerContext ctx, ProtoMessage message) {
    log.info("ping received");
  }
}
