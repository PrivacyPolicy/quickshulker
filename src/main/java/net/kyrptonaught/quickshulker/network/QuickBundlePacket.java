package net.kyrptonaught.quickshulker.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kyrptonaught.quickshulker.BundleHelper;
import net.kyrptonaught.quickshulker.QuickShulkerMod;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public record QuickBundlePacket(int invSlotId, ItemStack stackToBundle) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<QuickBundlePacket> ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(QuickShulkerMod.MOD_ID, "quick_bundle_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, QuickBundlePacket> CODEC
            = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, QuickBundlePacket::invSlotId,
            ItemStack.STREAM_CODEC, QuickBundlePacket::stackToBundle,
            QuickBundlePacket::new);

    public static void registerReceivePacket() {
        ServerPlayNetworking.registerGlobalReceiver(ID, (payload, context) -> {
            if (context.player().isCreative()) {
                context.server().execute(() -> BundleHelper.bundleItemIntoStack(context.player(), context.player().getInventory().getItem(payload.invSlotId()), payload.stackToBundle(), null));
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
        Minecraft.getInstance().gameMode.handleCreativeModeItemAdd(output, slot.index);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public record BundleIntoHeld(ItemStack stackToBundle, ItemStack bundleStack) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<BundleIntoHeld> ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(QuickShulkerMod.MOD_ID, "quick_bundleheld_packet"));
        public static final StreamCodec<RegistryFriendlyByteBuf, BundleIntoHeld> CODEC
                = StreamCodec.composite(
                ItemStack.STREAM_CODEC, BundleIntoHeld::stackToBundle,
                ItemStack.STREAM_CODEC, BundleIntoHeld::bundleStack,
                BundleIntoHeld::new);

        public static void registerReceivePacket() {
            ServerPlayNetworking.registerGlobalReceiver(BundleIntoHeld.ID, (payload, context) -> {
                if (context.player().isCreative()) {
                    context.server().execute(() -> BundleHelper.bundleItemIntoStack(context.player(), payload.bundleStack(), payload.stackToBundle(), null));
                }
            });
        }

        @Environment(EnvType.CLIENT)
        public static void sendPacket(ItemStack stackToBundle, ItemStack bundleStack) {
            ClientPlayNetworking.send(new BundleIntoHeld(stackToBundle.copy(), bundleStack.copy()));
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return BundleIntoHeld.ID;
        }
    }

    public record Unbundle(int invSlotId, ItemStack unbundleStack) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<Unbundle> ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(QuickShulkerMod.MOD_ID, "quick_unbundle_packet"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Unbundle> CODEC
                = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Unbundle::invSlotId,
                ItemStack.STREAM_CODEC, Unbundle::unbundleStack,
                Unbundle::new);

        public static void registerReceivePacket() {
            ServerPlayNetworking.registerGlobalReceiver(Unbundle.ID, (payload, context) -> {
                if (context.player().isCreative()) {
                    context.server().execute(() -> {
                        ItemStack output = BundleHelper.unbundleItem(context.player(), payload.unbundleStack());
                        if (output != null)
                            context.player().getInventory().setItem(payload.invSlotId(), output);
                    });
                }
            });
        }

        @Environment(EnvType.CLIENT)
        public static void sendPacket(int slotID, ItemStack unbundleStack) {
            ClientPlayNetworking.send(new Unbundle(slotID, unbundleStack.copy()));
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return Unbundle.ID;
        }
    }
}
