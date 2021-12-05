package codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.ReplayingDecoder;
import java.nio.charset.Charset;
import java.util.List;
import proto.Message;

public class MessageDecoder extends ReplayingDecoder {


  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    Message message = new Message();
    message.setType(in.readInt());
    message.setMessageIdLen(in.readInt());
    message.setMessageId(in.readCharSequence(message.getMessageIdLen(), Charset.defaultCharset()).toString());
    message.setTokenLen(in.readInt());
    message.setToken(in.readCharSequence(message.getTokenLen(), Charset.defaultCharset()).toString());
    int lenToRead = in.readInt();
    message.setDataLen(lenToRead);
    byte[] bytes = new byte[message.getDataLen()];
    for (int i = 0; i < bytes.length; i++) {
      bytes[i] = in.readByte();
    }
    message.setData(bytes);
    out.add(message);
  }
}
