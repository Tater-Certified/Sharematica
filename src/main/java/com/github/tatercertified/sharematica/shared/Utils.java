package com.github.tatercertified.sharematica.shared;

import com.github.tatercertified.sharematica.Sharematica;
import com.simtechdata.waifupnp.UPnP;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import net.fabricmc.loader.api.FabricLoader;
import network.ycc.raknet.RakNet;
import network.ycc.raknet.client.RakNetClient;
import network.ycc.raknet.pipeline.UserDataCodec;
import network.ycc.raknet.server.RakNetServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class Utils {
    public static Path path = Path.of(FabricLoader.getInstance().getGameDir() + "/schematics/sharematica");
    public static void generateSharematicaFolder() {
        if (Files.notExists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    static final EventLoopGroup ioGroup = new NioEventLoopGroup();
    static final EventLoopGroup childGroup = new DefaultEventLoopGroup();
    static final InetSocketAddress localhost = new InetSocketAddress("localhost", Config.port);
    public static Channel serverChannel;
    public static Channel clientChannel;
    public static void openRaknetty() throws InterruptedException {
        if (Config.upnp) {
            System.out.println("UPnP: " + UPnP.openPortUDP(Config.port));
        }
        if (Config.raknet && Objects.equals(Sharematica.ENVIRONMENT, "server")) {
            System.out.println("Starting Raknetty Server!");
            serverChannel = new ServerBootstrap()
                    .group(ioGroup, childGroup)
                    .channel(RakNetServer.CHANNEL)
                    .option(RakNet.SERVER_ID,
                            1234567L) //will be set randomly if not specified (optional)
                    .childHandler(new ChannelInitializer<Channel>() {
                        protected void initChannel(Channel ch) {
                            ch.pipeline().addLast(UserDataCodec.NAME, new UserDataCodec(0xFE));
                        }
                    }).bind(localhost).sync().channel();
            System.out.println("Raknetty Server Started!");
        }
        if (Objects.equals(Sharematica.ENVIRONMENT, "client")) {
            System.out.println("Starting Raknetty Client!");
            clientChannel = new Bootstrap()
                    .group(ioGroup)
                    .channel(RakNetClient.THREADED_CHANNEL)
                    .option(RakNet.MTU, 150) //can configure an initial MTU if desired (optional)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,
                            5000) //supports most normal netty ChannelOptions (optional)
                    .option(ChannelOption.SO_REUSEADDR, true) //can also set socket options (optional)
                    .handler(new ChannelInitializer<Channel>() {
                        protected void initChannel(Channel ch) {
                            ch.pipeline().addLast(UserDataCodec.NAME, new UserDataCodec(0xFE));
                            ch.pipeline().addLast(new SimpleChannelInboundHandler<ByteBuf>() {
                                protected void channelRead0(ChannelHandlerContext ctx, ByteBuf buf) throws IOException {
                                    byte[] data = buf.array();
                                    System.out.println("Client received Schematic from Server!");
                                    Files.write(path, data);
                                }
                            });
                        }
                    }).connect(localhost).sync().channel();
            System.out.println("Raknetty Client Started!");
        }
    }

    public static void stopRaknetty() throws InterruptedException {
        serverChannel.close().sync();
        clientChannel.close().sync();
        UPnP.closePortUDP(Config.port);
    }
}
