package server;

public enum MessageTypeEnum {
  HTTP_REQUEST((byte) 1), HTTP_RESPONSE((byte) 2), PING((byte) 3), PONG((byte) 4), EMPTY((byte) 5), REGISTER((byte) 6),
  FIN((byte) 7), ACK((byte) 8);

  private byte type;

  MessageTypeEnum(byte type) {
    this.type = type;
  }

  public int getType() {
    return type;
  }

  public static MessageTypeEnum get(byte type) {
    for (MessageTypeEnum value : values()) {
      if (value.type == type) {
        return value;
      }
    }

    throw new RuntimeException("unsupported type: " + type);
  }
}






