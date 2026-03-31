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

    public int getInventorySize() {
        return inventory.length;
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