package com.github.tatercertified.sharematica.server;

import com.github.tatercertified.sharematica.shared.Config;
import com.github.tatercertified.sharematica.shared.Utils;
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
import static com.github.tatercertified.sharematica.shared.Raknet.serverChannel;
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
        //try {
        //    Utils.openRaknetty();
        //} catch (InterruptedException e) {
        //    throw new RuntimeException(e);
        //}

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
            System.out.println("sendLitematicaRequest() Acquired by Server!");
            String name = buf.readString();
            File schematic = getSchematic(name);
            final PacketByteBuf buf1 = PacketByteBufs.create();
            if (Config.raknet) {
                try {
                    buf1.writeBytes(Files.readAllBytes(schematic.toPath()));
                    serverChannel.writeAndFlush(buf1).sync();
                    System.out.println("Server sent Schematic to Client!");
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else {
                try {
                    buf1.writeBytes(Utils.compress(schematic));
                    sender.sendPacket(SHAREMATICA_REQUEST_SCHEMATIC, buf1);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void registerEvents() {
        ServerLifecycleEvents.SERVER_STOPPING.register((server -> {
            //try {
            //    Utils.stopRaknetty();
            //} catch (InterruptedException e) {
            //    throw new RuntimeException(e);
            //}
        }));
    }



    private String[] getSchematicList() {
        File folder = server_path.toFile();
        return folder.list();
    }

    private File getSchematic(String name) {
        return new File(server_path + "/" + name + ".litematic");
    }
}

