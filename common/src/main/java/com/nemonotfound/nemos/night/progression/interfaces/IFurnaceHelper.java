package com.nemonotfound.nemos.night.progression.interfaces;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.FuelValues;

public interface IFurnaceHelper {

    int nemosNightProgression$getLitTimeRemaining();
    void nemosNightProgression$setLitTimeRemaining(int litTimeRemaining);
    int nemosNightProgression$getCookingTotalTime();
    NonNullList<ItemStack> nemosNightProgression$getItems();
    int nemosNightProgression$getBurnDuration(FuelValues fuelValues, ItemStack itemStack);
}
