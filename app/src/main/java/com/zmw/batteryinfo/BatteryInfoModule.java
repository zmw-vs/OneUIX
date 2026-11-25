package com.zmw.batteryinfo;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import java.lang.reflect.Method;

import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface;

/**
 * Headless LSPosed module, extracted from SoClear/OneUIX (AGPL-3.0).
 *
 * Single feature: reveal the hidden battery regulatory information entry
 * under "Settings -> About phone -> Battery information".
 *
 * Pure Java on purpose: a Kotlin source drags the whole kotlin-stdlib into
 * the APK (>2 MB) for a hook that needs no runtime library at all.
 */
@SuppressLint("DiscouragedPrivateApi")
public class BatteryInfoModule extends XposedModule {

    private static final String SETTINGS_PACKAGE = "com.android.settings";
    private static final String CONTROLLER_CLASS =
            "com.samsung.android.settings.deviceinfo.batteryinfo.BatteryRegulatoryPreferenceController";

    private static final Method ATTACH_METHOD = findAttachMethod();

    private static Method findAttachMethod() {
        try {
            return Application.class.getDeclaredMethod("attach", Context.class);
        } catch (NoSuchMethodException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @Override
    public void onPackageReady(XposedModuleInterface.PackageReadyParam param) {
        if (!param.isFirstPackage() || !SETTINGS_PACKAGE.equals(param.getPackageName())) {
            return;
        }

        // Wait for the host Application context, then locate the controller class.
        hook(ATTACH_METHOD).intercept(chain -> {
            Object result = chain.proceed();
            try {
                Class<?> controller = param.getClassLoader().loadClass(CONTROLLER_CLASS);
                hook(controller.getDeclaredMethod("getAvailabilityStatus")).intercept(c -> 0);
            } catch (Throwable t) {
                // One UI updates may rename this class. Fail quietly, keep the host alive.
                logFailure(t);
            }
            return result;
        });
    }

    private void logFailure(Throwable t) {
        log(Log.ERROR, null,
                "[BatteryInfoLSP] showMoreBatteryInfo hook failed: "
                        + t.getClass().getSimpleName() + ": " + t.getMessage(), t);
    }
}
