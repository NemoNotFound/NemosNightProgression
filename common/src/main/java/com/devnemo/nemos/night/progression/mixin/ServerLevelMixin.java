package com.devnemo.nemos.night.progression.mixin;

import com.devnemo.nemos.night.progression.interfaces.IServerLevelHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin extends Level implements IServerLevelHelper {

    @Shadow public abstract ServerLevel getLevel();

    @Shadow @Final private MinecraftServer server;
    @Unique
    private boolean nemosNightProgression$shouldHandleNightProgression = false;
    @Unique
    private long nemosNightProgression$beforeSleepTime = 0;
    @Unique
    private long nemosNightProgression$afterSleepTime = 0;

    protected ServerLevelMixin(WritableLevelData levelData, ResourceKey<Level> dimension, RegistryAccess registryAccess, Holder<DimensionType> dimensionTypeRegistration, boolean isClientSide, boolean isDebug, long biomeZoomSeed, int maxChainedNeighborUpdates) {
        super(levelData, dimension, registryAccess, dimensionTypeRegistration, isClientSide, isDebug, biomeZoomSeed, maxChainedNeighborUpdates);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setDayTime(J)V"))
    private void tick(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        var j = this.getDayTime() + 24000L;

        nemosNightProgression$setShouldHandleNightProgression(true);
        nemosNightProgression$setBeforeSleepTime(getDayTime());
        nemosNightProgression$setAfterSleepTime(j - j % 24000L);

        var ticksSlept = nemosNightProgression$afterSleepTime - nemosNightProgression$beforeSleepTime;

        this.getLevel().getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(3 * (int) ticksSlept, this.server);
    }

    @Inject(method = "tick", at = @At(value = "TAIL"))
    private void tickAtEnd(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        this.getLevel().getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(3, this.server);
        nemosNightProgression$setShouldHandleNightProgression(false);
    }

    @Override
    public boolean nemosNightProgression$shouldHandleNightProgression() {
        return nemosNightProgression$shouldHandleNightProgression;
    }

    @Override
    public void nemosNightProgression$setShouldHandleNightProgression(boolean shouldHandleNightProgression) {
        this.nemosNightProgression$shouldHandleNightProgression = shouldHandleNightProgression;
    }

    @Override
    public long nemosNightProgression$getBeforeSleepTime() {
        return nemosNightProgression$beforeSleepTime;
    }

    @Override
    public void nemosNightProgression$setBeforeSleepTime(long beforeSleepTime) {
        this.nemosNightProgression$beforeSleepTime = beforeSleepTime;
    }

    @Override
    public long nemosNightProgression$getAfterSleepTime() {
        return nemosNightProgression$afterSleepTime;
    }

    @Override
    public void nemosNightProgression$setAfterSleepTime(long afterSleepTime) {
        this.nemosNightProgression$afterSleepTime = afterSleepTime;
    }
}
