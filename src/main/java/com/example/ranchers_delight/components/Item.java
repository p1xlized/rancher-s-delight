package com.example.ranchers_delight.components;

import com.example.ranchers_delight.utils.Rarity;

public class Item
{
    public String name;
    public Rarity rarity;

    public Item(String name, Rarity rarity) {
        this.name = name;
        this.rarity = rarity;
    }

    public String getName() {
        return name;
    }

    public Rarity getRarity() {
        return rarity;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRarity(Rarity rarity) {
        this.rarity = rarity;
    }
}
