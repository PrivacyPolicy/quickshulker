package net.kyrptonaught.quickshulker.mixin;

import net.kyrptonaught.quickshulker.ItemInventoryContainer;
import net.kyrptonaught.quickshulker.api.Util;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.StonecutterMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = {CraftingMenu.class, StonecutterMenu.class})
public abstract class CraftingScreenHandlerMixin extends AbstractContainerMenu {

    protected CraftingScreenHandlerMixin(@Nullable MenuType<?> type, int syncId) {
        super(type, syncId);
    }

    @Inject(method = "stillValid", at = @At("HEAD"), cancellable = true)
    public void overrideCanUse(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (((ItemInventoryContainer) this).hasItem()) {
            ItemStack stack = player.getInventory().getItem(((ItemInventoryContainer) this).getUsedSlotInPlayerInv());
            if (Util.isOpenableItem(stack))
                cir.setReturnValue(true);
        }
    }
}
