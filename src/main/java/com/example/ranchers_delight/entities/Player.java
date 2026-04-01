package com.example.ranchers_delight.entities;

import com.example.ranchers_delight.components.Item;

public class Player {
    private final Item[] inventory;
    private final String name;
    private int level;

    public Player(String name, int level) {
        this.name = name;
        this.level = level;
        this.inventory = new Item[10];
    }

    public boolean addItemToInventory(Item newItem) {
        if (newItem == null) {
            return false;
        }

        if (isSeedItem(newItem)) {
            for (Item item : inventory) {
                if (item != null && isSameSeedStack(item, newItem)) {
                    item.addQuantity(newItem.getQuantity());
                    System.out.println(newItem.getName() + " stacked to " + item.getQuantity());
                    return true;
                }
            }
        }

        for (int i = 0; i < inventory.length; i++) {
            if (inventory[i] == null) {
                inventory[i] = newItem;
                System.out.println(newItem.getName() + " added to slot " + i);
                return true; // Item added successfully
            }
        }
        System.out.println("Inventory is full!");
        return false; // No room left
    }

    public Item getItemInSlot(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= inventory.length) {
            return null;
        }
        return inventory[slotIndex];
    }

    public boolean consumeItemInSlot(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= inventory.length || inventory[slotIndex] == null) {
            return false;
        }

        Item item = inventory[slotIndex];
        if (!item.consumeOne()) {
            return false;
        }

        if (item.getQuantity() <= 0) {
            inventory[slotIndex] = null;
        }
        return true;
    }

    private boolean isSeedItem(Item item) {
        String name = item.getName();
        return name != null && ("seed".equalsIgnoreCase(name) || "seeds".equalsIgnoreCase(name));
    }

    private boolean isSameSeedStack(Item a, Item b) {
        return a.getRarity() == b.getRarity() && a.getName().equalsIgnoreCase(b.getName());
    }

    public int getInventorySize() {
        return inventory.length;
    }

    public void clearInventory() {
        for (int i = 0; i < inventory.length; i++) {
            inventory[i] = null;
        }
    }

    public boolean setItemInSlot(int slotIndex, Item item) {
        if (slotIndex < 0 || slotIndex >= inventory.length) {
            return false;
        }

        inventory[slotIndex] = item;
        return true;
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}