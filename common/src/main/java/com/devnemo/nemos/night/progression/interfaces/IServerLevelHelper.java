package com.devnemo.nemos.night.progression.interfaces;

public interface IServerLevelHelper {

    boolean nemosNightProgression$shouldHandleNightProgression();
    void nemosNightProgression$setShouldHandleNightProgression(boolean shouldHandleNightProgression);
    long nemosNightProgression$getBeforeSleepTime();
    void nemosNightProgression$setBeforeSleepTime(long beforeSleepTime);
    long nemosNightProgression$getAfterSleepTime();
    void nemosNightProgression$setAfterSleepTime(long afterSleepTime);
}
