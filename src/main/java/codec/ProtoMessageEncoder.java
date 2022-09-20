/*
package codec;


import com.google.common.base.Strings;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.Charset;
import java.util.UUID;
import server.MessageTypeEnum;
import com.jtunnel.proto.ProtoMessage;

public class ProtoMessageEncoder extends MessageToByteEncoder<ProtoMessage> {

  @Override
  protected void encode(ChannelHandlerContext ctx, ProtoMessage message, ByteBuf out) {
    if (message.getMessageType() != MessageTypeEnum.EMPTY) {
      if (Strings.isNullOrEmpty(message.getSessionId())) {
        String sessionId = UUID.randomUUID().toString();
        message.setSessionId(sessionId);
        out.writeCharSequence(sessionId, Charset.defaultCharset());
      } else {
        out.writeCharSequence(message.getSessionId(), Charset.defaultCharset());
      }

      out.writeByte(message.getMessageType().getType());
      out.writeShort(message.getAttachments().size());
      message.getAttachments().forEach((key, value) -> {
        Charset charset = Charset.defaultCharset();
        out.writeInt(key.length());
        out.writeCharSequence(key, charset);
        out.writeInt(value.length());
        out.writeCharSequence(value, charset);
      });

      if (null == message.getBody()) {
        out.writeInt(0);
      } else {
        out.writeInt(message.getBody().length());
        out.writeCharSequence(message.getBody(), Charset.defaultCharset());
      }
    }
  }
}





*/
