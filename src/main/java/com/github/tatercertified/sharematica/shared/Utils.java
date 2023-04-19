package com.github.tatercertified.sharematica.shared;

import com.github.tatercertified.sharematica.Sharematica;
import com.github.tatercertified.sharematica.client.SharematicaClient;
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

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Utils {
    public static Path path = Path.of(FabricLoader.getInstance().getGameDir() + "/schematics/sharematica");
    public static Path server_path = Path.of(FabricLoader.getInstance().getGameDir() + "/server_schematics");
    public static void generateSharematicaFolder() {
        if (Objects.equals(Sharematica.ENVIRONMENT, "client")) {
            if (Files.notExists(path)) {
                try {
                    Files.createDirectories(path);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            if (Files.notExists(server_path)) {
                try {
                    Files.createDirectories(server_path);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
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
                    })
                    .connect(localhost)
                    .addListener((ChannelFutureListener) future -> {
                        if (future.isSuccess()) {
                            System.out.println("Raknetty Client Connected!");
                            SharematicaClient.sendLitematicaRequest();
                        } else {
                            System.err.println("Raknetty Client Connection Failed!");
                            future.cause().printStackTrace();
                        }
                    }).sync().channel();
            System.out.println("Raknetty Client Started!");
        }
    }

    public static void stopRaknetty() throws InterruptedException {
        serverChannel.close().sync();
        clientChannel.close().sync();
        UPnP.closePortUDP(Config.port);
    }

    public static byte[] compress(File input) throws IOException {

        // Create a FileInputStream for the input file
        FileInputStream fileInputStream = new FileInputStream(input);

        // Create a ByteArrayOutputStream to hold the compressed data
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        // Create a GZIPOutputStream to compress the data and write it to the ByteArrayOutputStream
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);

        // Read the input data and write the compressed data to the GZIPOutputStream
        byte[] buffer = new byte[1024];
        int len;
        while ((len = fileInputStream.read(buffer)) > 0) {
            gzipOutputStream.write(buffer, 0, len);
        }

        // Close the streams
        gzipOutputStream.close();
        fileInputStream.close();

        // Get the compressed data as a byte array
        return byteArrayOutputStream.toByteArray();
    }

    public static void decompress(byte[] input, Path path, String name) throws IOException {

        // Create a ByteArrayInputStream for the compressed data
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(input);

        // Create a GZIPInputStream to decompress the data
        GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);

        // Create a FileOutputStream for the output file
        File outputFile = new File(path + "/" + name + ".litematic");
        FileOutputStream fileOutputStream = new FileOutputStream(outputFile);

        // Read the decompressed data and write it to the output file
        byte[] buffer = new byte[1024];
        int len;
        while ((len = gzipInputStream.read(buffer)) > 0) {
            fileOutputStream.write(buffer, 0, len);
        }

        // Close the streams
        gzipInputStream.close();
        fileOutputStream.close();
    }
}
