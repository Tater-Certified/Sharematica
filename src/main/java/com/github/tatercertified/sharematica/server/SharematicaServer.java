package com.github.tatercertified.sharematica.server;

import com.github.tatercertified.sharematica.shared.Config;
import com.github.tatercertified.sharematica.shared.Utils;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;

import java.io.File;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.github.tatercertified.sharematica.Sharematica.*;
import static com.github.tatercertified.sharematica.shared.Utils.generateSharematicaFolder;
import static com.github.tatercertified.sharematica.shared.Utils.path;

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

        //Packets
        registerGlobalReceivers();
    }

    public void registerGlobalReceivers() {
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

        });
    }

    private String[] getSchematicList() {
        File folder = Utils.path.toFile();
        return folder.list();
    }

    private File getSchematic() {
        String name = (String) packet_enderchest.poll();
        return new File(path + "/" + name);
    }

}
