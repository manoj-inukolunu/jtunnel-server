package server.handler;

import com.google.common.base.Throwables;
import com.jtunnel.proto.MessageType;
import com.jtunnel.server.AppData;
import io.netty.channel.ChannelHandlerContext;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.jtunnel.proto.ProtoMessage;


@NoArgsConstructor
@Slf4j
public class RegisterMessageHandler implements MessageHandler {

  public RegisterMessageHandler(MessageHandlers handlers) {
    handlers.register(MessageType.REGISTER, this);
  }

  @Override
  public void handleMessage(ChannelHandlerContext ctx, ProtoMessage message) {
    try {
      log.info("Register Message Received for subdomain={} sessionId={}", message.getSubDomain(),
          message.getSessionId());
      AppData.channelMap.put(message.getSubDomain(), ctx.pipeline());
      message.setBody("Successfully Registered");
      ctx.writeAndFlush(message);
    } catch (Exception e) {
      log.error("Exception while registering message = {} ", message, e);
      message.setBody("Failed to register " + Throwables.getStackTraceAsString(e));
      ctx.writeAndFlush(message);
    }
  }
}






