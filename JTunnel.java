

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;

public class JTunnel {

    private static void readData(SocketChannel channel) throws Exception {
        ByteBuffer buffer = ByteBuffer.allocateDirect(10000);
        int count;
        while ((count = channel.read(buffer)) > 0) {
            buffer.flip();
            System.out.println(StandardCharsets.UTF_8.decode(buffer));
            buffer.clear();
        }
    }
    

    public static void runServer() throws Exception {
        ServerSocketChannel remoteHttpServer = ServerSocketChannel.open();
        remoteHttpServer.configureBlocking(false);
        remoteHttpServer.bind(new InetSocketAddress(8080));
        Selector httpSelector = Selector.open();
        remoteHttpServer.register(httpSelector, SelectionKey.OP_ACCEPT);

        ServerSocketChannel clientServer = ServerSocketChannel.open();
        clientServer.configureBlocking(false);
        clientServer.bind(new InetSocketAddress(1234));
        Selector clientSelector = Selector.open();
        clientServer.register(clientSelector, SelectionKey.OP_ACCEPT);
        HashMap<String, SocketChannel> clientChannelMap = new HashMap<>();
        HashMap<String, SocketChannel> sourceChannelMap = new HashMap<>();
        while (true) {
            if (httpSelector.selectNow() > 0) {
                Iterator<SelectionKey> keys = httpSelector.selectedKeys().iterator();
                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();
                    if (key.isValid() && key.isAcceptable()) {
                        SocketChannel remoteHttpChannel = remoteHttpServer.accept();
                        remoteHttpChannel.configureBlocking(false);
                        remoteHttpChannel.register(httpSelector, SelectionKey.OP_READ);
                        System.out.println("Http Connection received");
                    } else if (key.isValid() && key.isReadable()) {
                        SocketChannel remoteHttpChannel = (SocketChannel) key.channel();
                        transferFromServerToClient(remoteHttpChannel, clientChannelMap, sourceChannelMap);
                    }
                }
            }
            if (clientSelector.selectNow() > 0) {
                Iterator<SelectionKey> keys = clientSelector.selectedKeys().iterator();
                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();
                    if (key.isValid() && key.isAcceptable()) {
                        SocketChannel clientChannel = clientServer.accept();
                        String hostName = ((InetSocketAddress) clientChannel.getRemoteAddress()).getHostName();
                        clientChannel.configureBlocking(false);
                        clientChannel.register(clientSelector, SelectionKey.OP_READ, hostName);
                        clientChannelMap.put(hostName, clientChannel);
                        System.out.println("Remote Client Connection received from = " + hostName);
                    } else if (key.isValid() && key.isReadable()) {
                        SocketChannel clientChannel = (SocketChannel) key.channel();
                        Object attachment = key.attachment();
                        if (attachment != null) {
                            String clientName = (String) attachment;
                            SocketChannel remoteChannel = sourceChannelMap.get(clientName);
                            if (remoteChannel != null) {
                                transferDataFromClientToServer(clientChannel, remoteChannel);
                            }
                        } else {
                            System.out.println("No Attachment Found");
                        }
                    }
                }
            }
        }

    }

    private static void transferDataFromClientToServer(SocketChannel source, SocketChannel sink) throws Exception {
        ByteBuffer buffer = ByteBuffer.allocateDirect(10000);
        int count;
        while ((count = source.read(buffer)) > 0) {
            buffer.flip();
            while (buffer.hasRemaining()) {
                sink.write(buffer);
            }
            buffer.clear();
        }

        if (count < 0) {
            source.close();
        }
    }


    private static void transferFromServerToClient(SocketChannel source, HashMap<String, SocketChannel> destMap,
                                                   HashMap<String, SocketChannel> sourceMap) throws Exception {
        ByteBuffer buffer = ByteBuffer.allocateDirect(10000);
        int count;
        String clientName = null;
        SocketChannel sink = null;
        while ((count = source.read(buffer)) > 0) {
            buffer.flip();
            if (clientName == null) {
                clientName = getClientName(buffer);
                sourceMap.put(clientName, source);
                System.out.println("Found clientName = " + clientName);
                buffer.rewind();
                sink = destMap.get(clientName);
                System.out.println("Found Sink Channel for clientName = " + clientName);
                while (buffer.hasRemaining()) {
                    sink.write(buffer);
                }
                buffer.clear();
            } else if (sink != null) {
                while (buffer.hasRemaining()) {
                    sink.write(buffer);
                }
                buffer.clear();
            }
        }
        if (count < 0) {
            source.close();
        }
    }

    private static String getClientName(ByteBuffer buff) {
        StringBuilder buffer = new StringBuilder();
        int count = 0;
        while (buff.hasRemaining()) {
            char data = (char) buff.get();
            if (data == '\r') {
                count++;
                if (count == 2) {
                    break;
                }
            }
            buffer.append(data);
        }
        String line = buffer.toString().split("\r\n")[1];
        return line.split(" ")[1];
    }

    public static void main(String[] args) throws Exception {
        runServer();
    }
}
