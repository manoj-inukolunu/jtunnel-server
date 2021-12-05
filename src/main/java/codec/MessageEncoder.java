package codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import java.nio.charset.Charset;
import proto.Message;

public class MessageEncoder extends MessageToByteEncoder<Message> {

  @Override
  protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
    out.writeInt(msg.getType());
    out.writeInt(msg.getMessageIdLen());
    out.writeCharSequence(msg.getMessageId(), Charset.defaultCharset());
    out.writeInt(msg.getTokenLen());
    out.writeCharSequence(msg.getToken(), Charset.defaultCharset());
    out.writeInt(msg.getDataLen());
    out.writeBytes(msg.getData());
  }
}
