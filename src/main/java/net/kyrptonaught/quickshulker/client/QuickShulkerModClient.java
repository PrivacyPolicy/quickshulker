package net.kyrptonaught.quickshulker.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.kyrptonaught.kyrptconfig.keybinding.CustomKeyBinding;
import net.kyrptonaught.kyrptconfig.keybinding.DisplayOnlyKeyBind;
import net.kyrptonaught.quickshulker.QuickShulkerMod;
import net.kyrptonaught.quickshulker.api.RegisterQuickShulkerClient;
import net.kyrptonaught.quickshulker.network.OpenInventoryPacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

@Environment(EnvType.CLIENT)
public class QuickShulkerModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientTickEvents.START_WORLD_TICK.register(clientWorld -> {
            if (Minecraft.getInstance().screen == null && QuickShulkerMod.getConfig().keybind) {
                Player player = Minecraft.getInstance().player;
                if (getKeybinding().isKeybindPressed() && player != null) {
                    if (player.getMainHandItem().isEmpty() && !player.getOffhandItem().isEmpty())
                        ClientUtil.CheckAndSend(player.getOffhandItem(), 45);
                    else
                        ClientUtil.CheckAndSend(player.getMainHandItem(), 36 + player.getInventory().getSelectedSlot());
                }
            }
        });
        PayloadTypeRegistry.playS2C().register(OpenInventoryPacket.ID, OpenInventoryPacket.CODEC);
        ClientPlayNetworking.registerGlobalReceiver(OpenInventoryPacket.ID, (payload, context) -> {
            Minecraft client = Minecraft.getInstance();
            client.execute(() -> {
                client.setScreen(new InventoryScreen(context.player()));
            });

        });
        FabricLoader.getInstance().getEntrypoints(QuickShulkerMod.MOD_ID + "_client", RegisterQuickShulkerClient.class).forEach(RegisterQuickShulkerClient::registerClient);

        KeyBindingHelper.registerKeyBinding(new DisplayOnlyKeyBind(
                "key.quickshulker.config.keybinding",
                KeyMapping.Category.register(Identifier.fromNamespaceAndPath(QuickShulkerMod.MOD_ID, "key.categories.quickshulker")),
                getKeybinding(),
                setKey -> QuickShulkerMod.config.save()
        ));
    }

    public static CustomKeyBinding getKeybinding() {
        return QuickShulkerMod.getConfig().keybinding;
    }
}