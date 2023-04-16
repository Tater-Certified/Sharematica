package com.github.tatercertified.sharematica.client;

import com.github.tatercertified.sharematica.shared.Config;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;

import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.github.tatercertified.sharematica.Sharematica.*;

public class SharematicaClient implements ClientModInitializer {

    public static Queue<Object> packet_enderchest = new ConcurrentLinkedQueue<>();

    public SharematicaClient() {
    }

    @Override
    public void onInitializeClient() {
        ENVIRONMENT = "client";
        Config.createConfig();
        System.out.println("Client Init");
        registerGlobalReceivers();
        registerEvents();
    }
    public void registerGlobalReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(SHAREMATICA_SEND_SCHEMATIC_LIST, ((client, handler, buf, responseSender) -> {
            List<String> schematics = buf.readList(PacketByteBuf::readString);
            packet_enderchest.add(schematics);
            serverSchematics();
        }));
    }

    public void registerEvents() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            // Example join packet
            PacketByteBuf passedData = PacketByteBufs.create();
            passedData.writeIdentifier(SHAREMATICA_SYNC_PACKET_ID);
            ClientPlayNetworking.send(SHAREMATICA_SYNC_PACKET_ID, passedData);
            System.out.println("Sent Sync Packet");

            // Example server schematics grab
            PacketByteBuf get_schematics = PacketByteBufs.empty();
            ClientPlayNetworking.send(SHAREMATICA_SEND_SCHEMATIC_LIST, get_schematics);

            // Example request schematic
            PacketByteBuf request = PacketByteBufs.create();
            String schematic_name = "example"; //TODO Hook this into the click function of Litematica
            request.writeString(schematic_name);
            ClientPlayNetworking.send(SHAREMATICA_REQUEST_SCHEMATIC, request);

        });
    }

    private void serverSchematics() {
        //TODO Actually add these schematics to the schematics list
        System.out.println(packet_enderchest.poll());
    }

}
