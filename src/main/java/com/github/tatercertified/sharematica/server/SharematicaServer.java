package com.github.tatercertified.sharematica.server;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.text.Text;

import static com.github.tatercertified.sharematica.Sharematica.SHAREMATICA_SYNC_PACKET_ID;

public class SharematicaServer implements ModInitializer {

    public SharematicaServer() {
    }

    @Override
    public void onInitialize() {
        System.out.println("Server Init");
        registerPacket();
    }

    public void registerPacket() {
        ServerPlayNetworking.registerGlobalReceiver(SHAREMATICA_SYNC_PACKET_ID, (client, handler, buf, responseSender, player) -> {
            client.execute(() -> {
                client.sendMessage(Text.of("Packet Received!"));
                System.out.println("Packet Received!");
            });
        });
    }
}
