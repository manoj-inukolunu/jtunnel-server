package com.jtunnel.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppData {

  public static Map<String, ChannelPipeline> channelMap = new ConcurrentHashMap<>();
  public static Map<String, ChannelHandlerContext> httpChannelMap = new ConcurrentHashMap<>();
  public static Map<String, String> hostToUUIDMap = new HashMap<>();

  public static ExecutorService service = Executors.newFixedThreadPool(2);

}
