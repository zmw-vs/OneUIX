package com.zmw.batteryinfo

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface
import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * Headless LSPosed module, extracted from SoClear/OneUIX (AGPL-3.0).
 *
 * Two independent features:
 *  1. Settings app  -> show the hidden battery regulatory information entry.
 *  2. system_server -> lift the China/HK firmware restriction on GMS/FCM push network.
 */
@SuppressLint("DiscouragedPrivateApi")
class BatteryInfoModule : XposedModule() {

    override fun onPackageReady(param: XposedModuleInterface.PackageReadyParam) {
        if (!param.isFirstPackage || param.packageName != SETTINGS_PACKAGE) return

        // Wait for the host Application context, then locate the controller class.
        hook(ATTACH_METHOD).intercept { chain ->
            val result = chain.proceed()
            try {
                val controllerClass = param.classLoader.loadClass(
                    "com.samsung.android.settings.deviceinfo.batteryinfo.BatteryRegulatoryPreferenceController"
                )
                hook(controllerClass.getDeclaredMethod("getAvailabilityStatus")).intercept { 0 }
            } catch (t: Throwable) {
                // One UI updates may rename this class. Fail quietly, keep the host alive.
                logFailure("showMoreBatteryInfo", t)
            }
            result
        }
    }

    override fun onSystemServerStarting(param: XposedModuleInterface.SystemServerStartingParam) {
        try {
            param.classLoader
                .loadClass("com.android.server.alarm.GmsAlarmManager")
                .constructors
                .forEach { constructor ->
                    hook(constructor).intercept { chain ->
                        val result = chain.proceed()
                        try {
                            setBooleanField(chain.thisObject, "isChinaMode", false)
                            setBooleanField(chain.thisObject, "isHongKongMode", false)
                        } catch (t: Throwable) {
                            logFailure("liftFcmNetworkLimit", t)
                        }
                        result
                    }
                }
        } catch (t: Throwable) {
            logFailure("liftFcmNetworkLimit", t)
        }
    }

    /** Lightweight replacement for the upstream reflection facade: walk the superclass chain. */
    private fun setBooleanField(target: Any, name: String, value: Boolean) {
        var clazz: Class<*>? = target.javaClass
        while (clazz != null) {
            try {
                val field: Field = clazz.getDeclaredField(name)
                field.isAccessible = true
                field.setBoolean(target, value)
                return
            } catch (_: NoSuchFieldException) {
                clazz = clazz.superclass
            }
        }
        throw NoSuchFieldException(name)
    }

    private fun logFailure(feature: String, t: Throwable) {
        log(
            Log.ERROR,
            null,
            "[BatteryInfoLSP] $feature hook failed: ${t.javaClass.simpleName}: ${t.message}",
            t
        )
    }

    companion object {
        const val SETTINGS_PACKAGE = "com.android.settings"

        val ATTACH_METHOD: Method = Application::class.java
            .getDeclaredMethod("attach", Context::class.java)
    }
}
