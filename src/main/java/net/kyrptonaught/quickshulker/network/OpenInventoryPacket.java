package net.kyrptonaught.quickshulker.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kyrptonaught.quickshulker.QuickShulkerMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class OpenInventoryPacket implements CustomPayload {
    public static final CustomPayload.Id<OpenInventoryPacket> ID = new CustomPayload.Id<>(Identifier.of(QuickShulkerMod.MOD_ID, "openinv"));
    public static final PacketCodec<RegistryByteBuf, OpenInventoryPacket> CODEC
            = PacketCodec.ofStatic((buf, value) -> {}, buf -> new OpenInventoryPacket());

    public static void send(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, new OpenInventoryPacket());
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
