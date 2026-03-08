package com.example.ranchers_delight.entities;

import com.example.ranchers_delight.components.Item;

public class Player {
    public Item[] inventory; // Changed to Item[] to match your class
    public String name;
    public int level;

    public Player(String name, int level) {
        this.name = name;
        this.level = level;
        // Correct syntax: new Item[size]
        // Let's give the player 10 slots to start with
        this.inventory = new Item[10];
    }

    /**
     * Loops through the array and puts the item in the first null (empty) slot.
     */
    public boolean addItemToInventory(Item newItem) {
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
}