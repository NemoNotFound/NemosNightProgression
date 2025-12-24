package com.nemonotfound.nemos.night.progression.mixin;

import com.nemonotfound.nemos.night.progression.interfaces.IFurnaceHelper;
import com.nemonotfound.nemos.night.progression.interfaces.IServerLevelHelper;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.FuelValues;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

//TODO: Refactor
@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class ForgeAbstractFurnaceBlockEntityMixin implements IFurnaceHelper {

    @Shadow
    int litTimeRemaining;
    @Shadow
    int cookingTotalTime;

    @Shadow protected NonNullList<ItemStack> items;

    @Shadow protected abstract int getBurnDuration(FuelValues fuelValues, ItemStack stack);

    @Definition(id = "furnace", local = @Local(argsOnly = true, type = AbstractFurnaceBlockEntity.class))
    @Definition(id = "cookingTimer", field = "Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;cookingTimer:I")
    @Expression("furnace.cookingTimer")
    @ModifyExpressionValue(method = "serverTick", at = @At(value = "MIXINEXTRAS:EXPRESSION", ordinal = 0))
    private static int updateCookingTimer(
            int original,
            @Local(argsOnly = true) ServerLevel serverLevel,
            @Local(argsOnly = true) AbstractFurnaceBlockEntity furnace,
            @Local RecipeHolder<? extends AbstractCookingRecipe> recipeHolder,
            @Local SingleRecipeInput singleRecipeInput,
            @Local(ordinal = 1) boolean flag
    ) {
        var furnaceHelper = ((IFurnaceHelper) furnace);
        var serverLevelHelper = (IServerLevelHelper) serverLevel;
        var progressingTicks = serverLevelHelper.nemosNightProgression$getAfterSleepTime() - serverLevelHelper.nemosNightProgression$getBeforeSleepTime();
        var itemStack = furnaceHelper.nemosNightProgression$getItems().get(1);
        var burnDuration = furnaceHelper.nemosNightProgression$getBurnDuration(serverLevel.fuelValues(), itemStack);
        var updatedLitTimeRemaining = furnaceHelper.nemosNightProgression$getLitTimeRemaining() + ((itemStack.getCount()) * burnDuration);
        var updatedCookingTimer = original + progressingTicks - 1;
        var lowest = Math.min(updatedLitTimeRemaining, updatedCookingTimer);
        var shouldHandleNightProgression = serverLevelHelper.nemosNightProgression$shouldHandleNightProgression();
        var progressedTicks = 0;

        if (!shouldHandleNightProgression) {
            return original;
        }

        while (lowest > furnaceHelper.nemosNightProgression$getCookingTotalTime()) {
            lowest -= furnaceHelper.nemosNightProgression$getCookingTotalTime();
            updatedLitTimeRemaining -= furnaceHelper.nemosNightProgression$getCookingTotalTime();
            updatedCookingTimer -= furnaceHelper.nemosNightProgression$getCookingTotalTime();
            progressedTicks += furnaceHelper.nemosNightProgression$getCookingTotalTime();

            if (furnace.burn(serverLevel.registryAccess(), recipeHolder, singleRecipeInput, furnaceHelper.nemosNightProgression$getItems(), furnace.getMaxStackSize())) {
                furnace.setRecipeUsed(recipeHolder);
            }

            flag = true;
        }

        if (burnDuration != 0) {
            var usedItems = progressedTicks / burnDuration;

            if (usedItems >= 1) {
                itemStack.shrink(usedItems);
            }

            furnaceHelper.nemosNightProgression$setLitTimeRemaining(updatedLitTimeRemaining - (itemStack.getCount() * burnDuration));
        } else {
            furnaceHelper.nemosNightProgression$setLitTimeRemaining(updatedLitTimeRemaining);
        }

        return (int) updatedCookingTimer;
    }

    @Override
    public int nemosNightProgression$getCookingTotalTime() {
        return cookingTotalTime;
    }

    @Override
    public NonNullList<ItemStack> nemosNightProgression$getItems() {
        return items;
    }

    @Override
    public int nemosNightProgression$getBurnDuration(FuelValues fuelValues, ItemStack itemStack) {
        return getBurnDuration(fuelValues, itemStack);
    }

    @Override
    public int nemosNightProgression$getLitTimeRemaining() {
        return litTimeRemaining;
    }

    @Override
    public void nemosNightProgression$setLitTimeRemaining(int litTimeRemaining) {
        this.litTimeRemaining = litTimeRemaining;
    }
}
