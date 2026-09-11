package com.nemonotfound.nemos.night.progression.mixin;

import com.nemonotfound.nemos.night.progression.interfaces.IServerLevelHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AgeableMob.class)
public abstract class AgeableMobMixin {

    @Shadow public abstract int getAge();

    @Shadow public abstract void setAge(int age);

    @Shadow public abstract boolean canAgeUp();

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void nemosNightProgression$simulateSkippedGrowth(CallbackInfo callbackInfo) {
        var mob = (AgeableMob) (Object) this;

        if (!(mob.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        var serverLevelHelper = (IServerLevelHelper) serverLevel;

        if (!serverLevelHelper.nemosNightProgression$shouldHandleNightProgression()) {
            return;
        }

        var skippedTicks = serverLevelHelper.nemosNightProgression$getAfterSleepTime()
                - serverLevelHelper.nemosNightProgression$getBeforeSleepTime() - 1;
        var age = getAge();

        if (skippedTicks <= 0 || age == 0) {
            return;
        }

        if (age < 0 && canAgeUp()) {
            setAge((int) Math.min(0L, age + skippedTicks));
        } else if (age > 0) {
            setAge((int) Math.max(0L, age - skippedTicks));
        }
    }
}
