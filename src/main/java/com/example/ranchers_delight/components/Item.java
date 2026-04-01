package com.example.ranchers_delight.components;

import com.example.ranchers_delight.utils.Rarity;

public class Item
{
    public String name;
    public Rarity rarity;
    private int quantity;

    public Item(String name, Rarity rarity) {
        this(name, rarity, 1);
    }

    public Item(String name, Rarity rarity, int quantity) {
        this.name = name;
        this.rarity = rarity;
        this.quantity = Math.max(1, quantity);
    }

    public String getName() {
        return name;
    }

    public Rarity getRarity() {
        return rarity;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = Math.max(1, quantity);
    }

    public void addQuantity(int amount) {
        if (amount > 0) {
            this.quantity += amount;
        }
    }

    public boolean consumeOne() {
        if (quantity <= 0) {
            return false;
        }

        quantity--;
        return true;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRarity(Rarity rarity) {
        this.rarity = rarity;
    }
}
