package net.kyrptonaught.quickshulker;

import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.kyrptonaught.quickshulker.api.QuickOpenableRegistry;
import net.kyrptonaught.quickshulker.api.QuickShulkerData;
import net.kyrptonaught.quickshulker.api.Util;
import net.kyrptonaught.shulkerutils.ShulkerUtils;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class BundleHelper {
    public static boolean shouldAttemptBundle(Player player, ClickAction clickType, ItemStack hostStack, ItemStack insertStack, boolean enabledInConfig) {
        return (enabledInConfig && clickType == ClickAction.SECONDARY && Util.isOpenableItem(hostStack) && isAcceptedInsertItem(insertStack) && Util.getQuickItemInventory(player, hostStack) != null);
    }

    public static boolean shouldAttemptUnBundle(Player player, ClickAction clickType, ItemStack hostStack, ItemStack insertStack, boolean enabledInConfig) {
        return (enabledInConfig && clickType == ClickAction.SECONDARY && hostStack.getCount() == 1 && insertStack.isEmpty() && Util.getQuickItemInventory(player, hostStack) != null);
    }

    private static boolean isAcceptedInsertItem(ItemStack insertStack) {
        return !insertStack.isEmpty() && !ShulkerUtils.isShulkerItem(insertStack);
    }

    public static void bundleItemIntoStack(Player player, ItemStack hostStack, ItemStack insertStack, CallbackInfoReturnable<Boolean> cir) {
        if (bundleItem(player, hostStack, insertStack) != null && cir != null)
            cir.setReturnValue(true);
    }

    public static void unbundleStackIntoSlot(Player player, ItemStack hostStack, Slot unbundleSlot, CallbackInfoReturnable<Boolean> cir) {
        ItemStack output = unbundleItem(player, hostStack);
        if (output != null) {
            unbundleSlot.setByPlayer(output);
            cir.setReturnValue(true);
        }
    }

    public static ItemStack unbundleItem(Player player, ItemStack hostStack) {
        Container inv = Util.getQuickItemInventory(player, hostStack);
        for (int i = inv.getContainerSize() - 1; i >= 0; i--) {
            if (!inv.getItem(i).isEmpty()) {
                ItemStack output = inv.removeItemNoUpdate(i);
                inv.stopOpen(player);
                return output;
            }
        }
        return null;
    }

    private static ItemStack bundleItem(Player player, ItemStack hostStack, ItemStack insertStack) {
        Container bundlingInv = Util.getQuickItemInventory(player, hostStack);
        QuickShulkerData qsdata = QuickOpenableRegistry.getQuickie(hostStack.getItem());
        if (bundlingInv != null && qsdata.canBundleInsertItem(player, bundlingInv, hostStack, insertStack)) {
            try (Transaction transaction = Transaction.openOuter()) {
                long amount = InventoryStorage.of(bundlingInv, null).insert(ItemVariant.of(insertStack), insertStack.getCount(), transaction);
                if (amount == 0) return null;
                transaction.commit();
                insertStack.shrink((int) amount);
                bundlingInv.stopOpen(player);
                return insertStack;
            }
        }
        return null;
    }
}
