package server.handler;

import io.netty.channel.ChannelHandlerContext;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import server.MessageTypeEnum;
import proto.ProtoMessage;

@Slf4j
public class MessageHandlers implements MessageHandler {


  Map<MessageTypeEnum, MessageHandler> handlers = new ConcurrentHashMap<>();

  public void register(MessageTypeEnum type, MessageHandler handler) {
    handlers.put(type, handler);
  }

  @Override
  public void handleMessage(ChannelHandlerContext ctx, ProtoMessage message) {
    try {
      handlers.get(message.getMessageType()).handleMessage(ctx, message);
    } catch (Exception e) {
      log.error("Exception in handling message ={}", message, e);
    }

  }
}






