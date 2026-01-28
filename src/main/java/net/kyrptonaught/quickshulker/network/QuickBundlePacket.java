package net.kyrptonaught.quickshulker.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kyrptonaught.quickshulker.BundleHelper;
import net.kyrptonaught.quickshulker.QuickShulkerMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;

public record QuickBundlePacket(int invSlotId, ItemStack stackToBundle) implements CustomPayload {
    public static final CustomPayload.Id<QuickBundlePacket> ID = new CustomPayload.Id<>(Identifier.of(QuickShulkerMod.MOD_ID, "quick_bundle_packet"));
    public static final PacketCodec<RegistryByteBuf, QuickBundlePacket> CODEC
            = PacketCodec.tuple(
            PacketCodecs.VAR_INT, QuickBundlePacket::invSlotId,
            ItemStack.PACKET_CODEC, QuickBundlePacket::stackToBundle,
            QuickBundlePacket::new);

    public static void registerReceivePacket() {
        ServerPlayNetworking.registerGlobalReceiver(ID, (payload, context) -> {
            if (context.player().isCreative()) {
                context.player().getServer().execute(() -> BundleHelper.bundleItemIntoStack(context.player(), context.player().getInventory().getStack(payload.invSlotId()), payload.stackToBundle(), null));
            }
        });
        Unbundle.registerReceivePacket();
        BundleIntoHeld.registerReceivePacket();
    }

    @Environment(EnvType.CLIENT)
    public static void sendPacket(int slotID, ItemStack stackToBundle) {
        ClientPlayNetworking.send(new QuickBundlePacket(slotID, stackToBundle.copy()));
    }

    public static void sendCreativeSlotUpdate(ItemStack output, Slot slot) {
        MinecraftClient.getInstance().interactionManager.clickCreativeStack(output, slot.id);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public record BundleIntoHeld(ItemStack stackToBundle, ItemStack bundleStack) implements CustomPayload {
        public static final CustomPayload.Id<BundleIntoHeld> ID = new CustomPayload.Id<>(Identifier.of(QuickShulkerMod.MOD_ID, "quick_bundleheld_packet"));
        public static final PacketCodec<RegistryByteBuf, BundleIntoHeld> CODEC
                = PacketCodec.tuple(
                ItemStack.PACKET_CODEC, BundleIntoHeld::stackToBundle,
                ItemStack.PACKET_CODEC, BundleIntoHeld::bundleStack,
                BundleIntoHeld::new);

        public static void registerReceivePacket() {
            ServerPlayNetworking.registerGlobalReceiver(BundleIntoHeld.ID, (payload, context) -> {
                if (context.player().isCreative()) {
                    context.player().getServer().execute(() -> BundleHelper.bundleItemIntoStack(context.player(), payload.bundleStack(), payload.stackToBundle(), null));
                }
            });
        }

        @Environment(EnvType.CLIENT)
        public static void sendPacket(ItemStack stackToBundle, ItemStack bundleStack) {
            ClientPlayNetworking.send(new BundleIntoHeld(stackToBundle.copy(), bundleStack.copy()));
        }

        @Override
        public Id<? extends CustomPayload> getId() {
            return BundleIntoHeld.ID;
        }
    }

    public record Unbundle(int invSlotId, ItemStack unbundleStack) implements CustomPayload {
        public static final CustomPayload.Id<Unbundle> ID = new CustomPayload.Id<>(Identifier.of(QuickShulkerMod.MOD_ID, "quick_unbundle_packet"));
        public static final PacketCodec<RegistryByteBuf, Unbundle> CODEC
                = PacketCodec.tuple(
                PacketCodecs.VAR_INT, Unbundle::invSlotId,
                ItemStack.PACKET_CODEC, Unbundle::unbundleStack,
                Unbundle::new);

        public static void registerReceivePacket() {
            ServerPlayNetworking.registerGlobalReceiver(Unbundle.ID, (payload, context) -> {
                if (context.player().isCreative()) {
                    context.player().getServer().execute(() -> {
                        ItemStack output = BundleHelper.unbundleItem(context.player(), payload.unbundleStack());
                        if (output != null)
                            context.player().getInventory().setStack(payload.invSlotId(), output);
                    });
                }
            });
        }

        @Environment(EnvType.CLIENT)
        public static void sendPacket(int slotID, ItemStack unbundleStack) {
            ClientPlayNetworking.send(new Unbundle(slotID, unbundleStack.copy()));
        }

        @Override
        public Id<? extends CustomPayload> getId() {
            return Unbundle.ID;
        }
    }
}
