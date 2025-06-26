package com.devnemo.nemos.night.progression.platform.services;

public interface IModLoaderHelper {

    String getModLoaderName();

    boolean isModLoaded(String modId);

    boolean isDevelopmentEnvironment();
}