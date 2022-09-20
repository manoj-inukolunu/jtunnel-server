package com.jtunnel.proto;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;

@Data
public class ProtoMessage implements Serializable {


  private String sessionId;
  private String body;
  private String subDomain;
  private MessageType messageType;
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

  public static ProtoMessage finMessage(String sessionId, String subDomain) {
    ProtoMessage message = new ProtoMessage();
    message.setMessageType(MessageType.FIN);
    message.setSessionId(sessionId);
    message.setSubDomain(subDomain);
    return message;
  }

}






