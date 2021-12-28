package server.handler;

import com.google.common.base.Throwables;
import com.jtunnel.server.AppData;
import io.netty.channel.ChannelHandlerContext;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import server.MessageTypeEnum;
import proto.ProtoMessage;


@NoArgsConstructor
@Slf4j
public class RegisterMessageHandler implements MessageHandler {

  private MessageHandlers handlers;

  public RegisterMessageHandler(MessageHandlers handlers) {
    handlers.register(MessageTypeEnum.REGISTER, this);
  }

  @Override
  public void handleMessage(ChannelHandlerContext ctx, ProtoMessage message) {
    try {
      log.info("Register Message Received for subdomain={} sessionId={}", message.getAttachments().get("subdomain"),
          message.getSessionId());
      AppData.channelMap.put(message.getAttachments().get("subdomain"), ctx.pipeline());
      message.setBody("Successfully Registered");
      ctx.writeAndFlush(message);
    } catch (Exception e) {
      log.error("Exception while registering message = {} ", message, e);
      message.setBody("Failed to register " + Throwables.getStackTraceAsString(e));
      ctx.writeAndFlush(message);
    }
  }
}






