package net.kyrptonaught.quickshulker.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kyrptonaught.quickshulker.QuickShulkerMod;
import net.kyrptonaught.quickshulker.api.Util;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

public record OpenShulkerPacket(int invSlot) implements CustomPayload {
    public static final Id<OpenShulkerPacket> ID = new Id<>(new Identifier(QuickShulkerMod.MOD_ID, "open_shulker_packet"));
    public static final PacketCodec<RegistryByteBuf, OpenShulkerPacket> CODEC
            = PacketCodecs.VAR_INT.xmap(OpenShulkerPacket::new, OpenShulkerPacket::invSlot).cast();


    public static void registerReceivePacket() {
        ServerPlayNetworking.registerGlobalReceiver(ID, (payload, context) -> {
            MinecraftServer server = context.player().server;
            int invSlot = payload.invSlot();
            server.execute(() -> Util.openItem(context.player(), invSlot));
        });
    }

    @Environment(EnvType.CLIENT)
    public static void sendOpenPacket(int invSlot) {
        ClientPlayNetworking.send(new OpenShulkerPacket(invSlot));
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }


}