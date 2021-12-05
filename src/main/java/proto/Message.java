package proto;


import lombok.Data;

@Data
public class Message {

  public int type;
  public int messageIdLen;
  public String messageId;
  public int tokenLen;
  public String token;
  public int dataLen;
  public byte[] data;


  public void setMessageId(String messageId) {
    this.messageId = messageId;
    this.messageIdLen = messageId.length();
  }

  public void setToken(String token) {
    this.token = token;
    this.tokenLen = token.length();
  }

  public void setData(byte[] data) {
    this.data = data;
    this.dataLen = data.length;
  }

  public static Message finMessage(String id, String token) {
    Message message = new Message();
    message.setMessageId(id);
    message.setToken(token);
    message.setData(new byte[] {});
    return message;
  }

}
