package server.handler;

import com.jtunnel.proto.MessageType;
import io.netty.channel.ChannelHandlerContext;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import com.jtunnel.proto.ProtoMessage;

@Slf4j
public class MessageHandlers implements MessageHandler {


  Map<MessageType, MessageHandler> handlers = new ConcurrentHashMap<>();

  public void register(MessageType type, MessageHandler handler) {
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






