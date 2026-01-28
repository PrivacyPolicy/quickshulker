package net.kyrptonaught.quickshulker.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kyrptonaught.quickshulker.QuickShulkerMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public class OpenInventoryPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenInventoryPacket> ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(QuickShulkerMod.MOD_ID, "openinv"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenInventoryPacket> CODEC
            = StreamCodec.of((buf, value) -> {}, buf -> new OpenInventoryPacket());

    public static void send(ServerPlayer player) {
        ServerPlayNetworking.send(player, new OpenInventoryPacket());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
