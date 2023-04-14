package com.github.tatercertified.sharematica.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;

import static com.github.tatercertified.sharematica.Sharematica.SHAREMATICA_SYNC_PACKET_ID;

public class SharematicaClient implements ClientModInitializer {

    public SharematicaClient() {
    }

    @Override
    public void onInitializeClient() {
        System.out.println("Client Init");
        registerPacket();
    }
    public void registerPacket() {
        System.out.println("Init Client");
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            String connect_msg = "Sharematica Synced!";
            PacketByteBuf passedData = PacketByteBufs.create();
            passedData.writeString(connect_msg);
            ClientPlayNetworking.send(SHAREMATICA_SYNC_PACKET_ID, passedData);
        });
    }

}
