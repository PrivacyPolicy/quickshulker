package net.kyrptonaught.quickshulker.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kyrptonaught.quickshulker.QuickShulkerMod;
import net.kyrptonaught.quickshulker.api.Util;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;

public record OpenShulkerPacket(int invSlot) implements CustomPacketPayload {
    public static final Type<OpenShulkerPacket> ID = new Type<>(Identifier.fromNamespaceAndPath(QuickShulkerMod.MOD_ID, "open_shulker_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenShulkerPacket> CODEC
            = ByteBufCodecs.VAR_INT.map(OpenShulkerPacket::new, OpenShulkerPacket::invSlot).cast();


    public static void registerReceivePacket() {
        ServerPlayNetworking.registerGlobalReceiver(ID, (payload, context) -> {
            MinecraftServer server = context.server();
            int invSlot = payload.invSlot();
            server.execute(() -> Util.openItem(context.player(), invSlot));
        });
    }

    @Environment(EnvType.CLIENT)
    public static void sendOpenPacket(int invSlot) {
        ClientPlayNetworking.send(new OpenShulkerPacket(invSlot));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }


}