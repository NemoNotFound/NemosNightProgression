package com.nemonotfound.nemos.night.progression.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.nemonotfound.nemos.night.progression.interfaces.IServerLevelHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.clock.ClockTimeMarker;
import net.minecraft.world.clock.ServerClockManager;
import net.minecraft.world.clock.WorldClock;
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
    @Unique
    private int nemosNightProgression$previousRandomTickSpeed;

    protected ServerLevelMixin(WritableLevelData levelData, ResourceKey<Level> dimension, RegistryAccess registryAccess, Holder<DimensionType> dimensionTypeRegistration, boolean isClientSide, boolean isDebug, long biomeZoomSeed, int maxChainedNeighborUpdates) {
        super(levelData, dimension, registryAccess, dimensionTypeRegistration, isClientSide, isDebug, biomeZoomSeed, maxChainedNeighborUpdates);
    }

    @WrapOperation(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/clock/ServerClockManager;moveToTimeMarker(Lnet/minecraft/core/Holder;Lnet/minecraft/resources/ResourceKey;)Lnet/minecraft/world/clock/ServerClockManager$MoveResult;"
            )
    )
    private ServerClockManager.MoveResult nemosNightProgression$captureSkippedTime(
            ServerClockManager clockManager,
            Holder<WorldClock> clock,
            ResourceKey<ClockTimeMarker> marker,
            Operation<ServerClockManager.MoveResult> original
    ) {
        var beforeSleepTime = clockManager.getInstance(clock).totalTicks();
        var moveResult = original.call(clockManager, clock, marker);
        var afterSleepTime = clockManager.getInstance(clock).totalTicks();
        var skippedTicks = afterSleepTime - beforeSleepTime;

        if (moveResult != ServerClockManager.MoveResult.MOVED || skippedTicks <= 0) {
            return moveResult;
        }

        nemosNightProgression$setBeforeSleepTime(beforeSleepTime);
        nemosNightProgression$setAfterSleepTime(afterSleepTime);
        nemosNightProgression$setShouldHandleNightProgression(true);

        var gameRules = getLevel().getGameRules();
        nemosNightProgression$previousRandomTickSpeed = gameRules.get(GameRules.RANDOM_TICK_SPEED);
        var acceleratedRandomTickSpeed = Math.clamp(
                (long) nemosNightProgression$previousRandomTickSpeed * skippedTicks,
                0L,
                Integer.MAX_VALUE
        );
        gameRules.set(GameRules.RANDOM_TICK_SPEED, (int) acceleratedRandomTickSpeed, this.server);

        return moveResult;
    }

    @Inject(method = "tick", at = @At(value = "TAIL"))
    private void tickAtEnd(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        if (!nemosNightProgression$shouldHandleNightProgression()) {
            return;
        }

        getLevel().getGameRules().set(
                GameRules.RANDOM_TICK_SPEED,
                nemosNightProgression$previousRandomTickSpeed,
                this.server
        );
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
