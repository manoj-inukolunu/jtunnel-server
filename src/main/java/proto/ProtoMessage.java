package proto;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import server.MessageTypeEnum;

@Data
public class ProtoMessage {


  private String sessionId;
  private String body;
  private MessageTypeEnum messageType;
  private Map<String, String> attachments = new HashMap<>();


  public Map<String, String> getAttachments() {
    return Collections.unmodifiableMap(attachments);
  }

  public void setAttachments(Map<String, String> attachments) {
    this.attachments.clear();
    if (null != attachments) {
      this.attachments.putAll(attachments);
    }
  }

  public void addAttachment(String key, String value) {
    attachments.put(key, value);
  }

  public static ProtoMessage finMessage(String sessionId) {
    ProtoMessage message = new ProtoMessage();
    message.setMessageType(MessageTypeEnum.FIN);
    message.setSessionId(sessionId);
    return message;
  }

  public static ProtoMessage finResponseMessage(String sessionId) {
    ProtoMessage message = new ProtoMessage();
    message.setMessageType(MessageTypeEnum.HTTP_RESPONSE);
    message.setSessionId(sessionId);
    message.setBody("");
    return message;
  }
}






