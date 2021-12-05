package server.handler;

import com.jtunnel.server.AppData;
import io.netty.channel.ChannelHandlerContext;
import lombok.NoArgsConstructor;
import server.MessageTypeEnum;
import server.ProtoMessage;


@NoArgsConstructor
public class RegisterMessageHandler implements MessageHandler {

  private MessageHandlers handlers;

  public RegisterMessageHandler(MessageHandlers handlers) {
    handlers.register(MessageTypeEnum.REGISTER, this);
  }

  @Override
  public void handleMessage(ChannelHandlerContext ctx, ProtoMessage message) {
    System.out.println("Register Message Received");
    AppData.channelMap.put(message.getAttachments().get("subdomain"), ctx.pipeline());
    message.setBody("Successfully Registered");
    ctx.writeAndFlush(message);
  }
}






