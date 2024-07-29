package de.ente.inventorysortermod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InventorySorterMod implements ModInitializer {

    private static KeyBinding sortKeyBinding;

    @Override
    public void onInitialize() {
        // Register the keybinding
        sortKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.inventorysortermod.sort", // The translation key of the keybinding's name
                InputUtil.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard keys
                GLFW.GLFW_KEY_O, // The keycode of the key
                "category.inventorysortermod.general" // The translation key of the keybinding's category
        ));

        // Register a client tick event to listen for key presses
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (sortKeyBinding.wasPressed()) {
                if (client.player != null) {
                    sortInventory(client.player);
                }
            }
        });
    }

    private void sortInventory(PlayerEntity player) {
        // Extract the part of the inventory excluding the hotbar
        List<ItemStack> inventoryList = new ArrayList<>(player.getInventory().main.subList(9, player.getInventory().main.size()));

        // Stack items
        stackItems(inventoryList);

        // Convert back to array for sorting
        ItemStack[] inventory = inventoryList.toArray(new ItemStack[0]);
        Arrays.sort(inventory, (a, b) -> {
            if (a.isEmpty() && b.isEmpty()) return 0;
            if (a.isEmpty()) return 1;
            if (b.isEmpty()) return -1;
            return a.getItem().getName().getString().compareTo(b.getItem().getName().getString());
        });

        // Update the player's inventory with the sorted items
        for (int i = 0; i < inventory.length; i++) {
            player.getInventory().main.set(9 + i, inventory[i]);
        }
    }

    private void stackItems(List<ItemStack> inventory) {
        // Iterate over the inventory and stack items
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack1 = inventory.get(i);
            if (stack1.isEmpty() || !stack1.isStackable()) continue;

            for (int j = i + 1; j < inventory.size(); j++) {
                ItemStack stack2 = inventory.get(j);
                if (stack2.isEmpty() || !stack1.equals(stack2)) continue;

                int transferableAmount = Math.min(stack2.getCount(), stack1.getMaxCount() - stack1.getCount());
                if (transferableAmount > 0) {
                    stack1.increment(transferableAmount);
                    stack2.decrement(transferableAmount);

                    if (stack2.isEmpty()) {
                        inventory.set(j, ItemStack.EMPTY);
                    }

                    if (stack1.getCount() >= stack1.getMaxCount()) {
                        break;
                    }
                }
            }
        }

        // Remove empty stacks
        inventory.removeIf(ItemStack::isEmpty);
    }

}
