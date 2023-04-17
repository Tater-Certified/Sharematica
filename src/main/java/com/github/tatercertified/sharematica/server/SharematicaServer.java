package com.github.tatercertified.sharematica.server;

import com.github.tatercertified.sharematica.shared.Config;
import com.github.tatercertified.sharematica.shared.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.github.tatercertified.sharematica.Sharematica.*;
import static com.github.tatercertified.sharematica.shared.Utils.*;

public class SharematicaServer implements DedicatedServerModInitializer {

    public static Queue<Object> packet_enderchest = new ConcurrentLinkedQueue<>();
    public SharematicaServer() {
    }

    @Override
    public void onInitializeServer() {
        ENVIRONMENT = "server";
        Config.createConfig();
        System.out.println("Server Init");
        generateSharematicaFolder();

        // Open Raknetty
        try {
            Utils.openRaknetty();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //Packets
        registerGlobalReceivers();

        //Events
        registerEvents();
    }

    private void registerGlobalReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(SHAREMATICA_SYNC_PACKET_ID, (server, player, handler, buf, sender) -> {
            packet_enderchest.add(buf.readIdentifier());
            System.out.println("Sync Packet Received!");
        });
        ServerPlayNetworking.registerGlobalReceiver(SHAREMATICA_SEND_SCHEMATIC_LIST, (server, player, handler, buf, sender) -> {
            PacketByteBuf schematics = PacketByteBufs.create();
            schematics.writeCollection(Arrays.asList(getSchematicList()), PacketByteBuf::writeString);
            sender.sendPacket(SHAREMATICA_SEND_SCHEMATIC_LIST, schematics);
        });
        ServerPlayNetworking.registerGlobalReceiver(SHAREMATICA_REQUEST_SCHEMATIC, (server, player, handler, buf, sender) -> {
            packet_enderchest.add(buf.readString());
            File schematic = getSchematic();
            final ByteBuf buf1 = Unpooled.buffer();
            try {
                buf1.writeBytes(Files.readAllBytes(schematic.toPath()));
                serverChannel.writeAndFlush(buf1).sync();
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void registerEvents() {
        ServerLifecycleEvents.SERVER_STOPPING.register((server -> {
            try {
                Utils.stopRaknetty();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }));
    }



    private String[] getSchematicList() {
        File folder = Utils.path.toFile();
        return folder.list();
    }

    private File getSchematic() {
        String name = (String) packet_enderchest.poll();
        return new File(path + "/" + name + ".litematic");
    }

}
