package com.nemonotfound.nemos.night.progression.mixin;

import com.nemonotfound.nemos.night.progression.interfaces.IServerLevelHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin {

    @Unique
    private static boolean nemosNightProgression$simulatingSkippedTicks;

    @Inject(method = "serverTick", at = @At("HEAD"), cancellable = true)
    private static void nemosNightProgression$simulateSkippedTicks(
            ServerLevel serverLevel,
            BlockPos blockPos,
            BlockState blockState,
            AbstractFurnaceBlockEntity furnace,
            CallbackInfo callbackInfo
    ) {
        var serverLevelHelper = (IServerLevelHelper) serverLevel;

        if (nemosNightProgression$simulatingSkippedTicks
                || !serverLevelHelper.nemosNightProgression$shouldHandleNightProgression()) {
            return;
        }

        var skippedTicks = serverLevelHelper.nemosNightProgression$getAfterSleepTime()
                - serverLevelHelper.nemosNightProgression$getBeforeSleepTime();

        if (skippedTicks <= 1) {
            return;
        }

        nemosNightProgression$simulatingSkippedTicks = true;

        try {
            for (long tick = 0; tick < skippedTicks; tick++) {
                AbstractFurnaceBlockEntity.serverTick(
                        serverLevel,
                        blockPos,
                        serverLevel.getBlockState(blockPos),
                        furnace
                );
            }
        } finally {
            nemosNightProgression$simulatingSkippedTicks = false;
        }

        callbackInfo.cancel();
    }
}
