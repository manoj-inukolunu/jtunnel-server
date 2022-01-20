/*
package codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.UUID;
import server.MessageTypeEnum;
import com.jtunnel.proto.ProtoMessage;

public class ProtoMessageDecoder extends ByteToMessageDecoder {

  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) throws Exception {
    ByteBuf copied = byteBuf.copy();
    int len = copied.readableBytes();
    byte[] data = new byte[len];
    copied.readBytes(data);
    System.out.println(new String(data));
    ProtoMessage message = new ProtoMessage();
    CharSequence sessionId =
        byteBuf.readCharSequence(UUID.randomUUID().toString().length(), Charset.defaultCharset());//  Read sessionId
    message.setSessionId((String) sessionId);

    message.setMessageType(MessageTypeEnum.get(byteBuf.readByte()));
    short attachmentSize = byteBuf.readShort();
    for (short i = 0; i < attachmentSize; i++) {
      int keyLength = byteBuf.readInt();
      CharSequence key = byteBuf.readCharSequence(keyLength, Charset.defaultCharset());
      int valueLength = byteBuf.readInt();
      CharSequence value = byteBuf.readCharSequence(valueLength, Charset.defaultCharset());
      message.addAttachment(key.toString(), value.toString());
    }

    int bodyLength = byteBuf.readInt();
    CharSequence body = byteBuf.readCharSequence(bodyLength, Charset.defaultCharset());
    message.setBody(body.toString());
    out.add(message);
  }
}






*/
