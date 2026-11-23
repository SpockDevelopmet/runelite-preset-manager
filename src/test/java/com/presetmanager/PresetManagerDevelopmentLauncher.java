package com.presetmanager;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

/**
 * Development entry point only. Not included in the published plugin JAR.
 */
public final class PresetManagerDevelopmentLauncher {
    public static void main(String[] args) throws Exception {
        ExternalPluginManager.loadBuiltin(PresetManagerPlugin.class);
        RuneLite.main(args);
    }

    private PresetManagerDevelopmentLauncher() {}
}
