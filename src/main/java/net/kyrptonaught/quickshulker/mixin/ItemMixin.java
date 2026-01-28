package net.kyrptonaught.quickshulker.mixin;

import net.kyrptonaught.quickshulker.BundleHelper;
import net.kyrptonaught.quickshulker.QuickShulkerMod;
import net.kyrptonaught.quickshulker.client.ClientUtil;
import net.kyrptonaught.quickshulker.network.QuickBundlePacket;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class ItemMixin {

    @Inject(method = "overrideOtherStackedOnMe", at = @At("HEAD"), cancellable = true)
    public void QS$onClicked(ItemStack hostStack, ItemStack insertStack, Slot slot, ClickAction clickType, Player player, SlotAccess cursorStackReference, CallbackInfoReturnable<Boolean> cir) {
        if (BundleHelper.shouldAttemptBundle(player, clickType, hostStack, insertStack, QuickShulkerMod.getConfig().supportsBundlingInsert)) {
            if (!player.level().isClientSide()) {
                BundleHelper.bundleItemIntoStack(player, hostStack, insertStack, cir);
            } else if (slot.container instanceof Inventory && ClientUtil.isCreativeScreen(player)) {//stupid creative menu shiz
                QuickBundlePacket.sendPacket(ClientUtil.getPlayerInvSlot(player.containerMenu, slot), insertStack);
                BundleHelper.bundleItemIntoStack(player, hostStack, insertStack, cir);
            }
        }
    }

    @Inject(method = "overrideStackedOnOther", at = @At("HEAD"), cancellable = true)
    public void QS$onStackClicked(ItemStack hostStack, Slot slot, ClickAction clickType, Player player, CallbackInfoReturnable<Boolean> cir) {
        ItemStack insertStack = slot.getItem();
        if (BundleHelper.shouldAttemptBundle(player, clickType, hostStack, insertStack, QuickShulkerMod.getConfig().supportsBundlingPickup)) {//bundle stack into held item
            if (!player.level().isClientSide()) {
                BundleHelper.bundleItemIntoStack(player, hostStack, insertStack, cir);
            } else if (slot.container instanceof Inventory && ClientUtil.isCreativeScreen(player)) { //stupid creative menu shiz
                QuickBundlePacket.BundleIntoHeld.sendPacket(insertStack, hostStack);
                BundleHelper.bundleItemIntoStack(player, hostStack, insertStack, cir);
                QuickBundlePacket.sendCreativeSlotUpdate(insertStack, slot);
            }
        } else if (BundleHelper.shouldAttemptUnBundle(player, clickType, hostStack, insertStack, QuickShulkerMod.getConfig().supportsBundlingExtract)) {//unbundle held stack into slot
            if (!player.level().isClientSide()) {
                BundleHelper.unbundleStackIntoSlot(player, hostStack, slot, cir);
            } else if (slot.container instanceof Inventory && ClientUtil.isCreativeScreen(player)) { //stupid creative menu shiz
                QuickBundlePacket.Unbundle.sendPacket(ClientUtil.getPlayerInvSlot(player.containerMenu, slot), hostStack);
                BundleHelper.unbundleStackIntoSlot(player, hostStack, slot, cir);
            }
        }
    }
}