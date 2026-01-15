package com.digitar.gamorax

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.digitar.gamorax.core.ads.AdManager
import com.digitar.gamorax.core.ads.AppOpenAdManager

class GameApplication : Application(), Application.ActivityLifecycleCallbacks {

    private var currentActivity: Activity? = null
    private lateinit var appLifecycleObserver: DefaultLifecycleObserver
    private val appOpenAdManager = AppOpenAdManager()

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)
        AdManager.initialize(this)

        appLifecycleObserver = object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                currentActivity?.let {
                    appOpenAdManager.showAdIfAvailable(it) {}
                }
            }
        }

        ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {
        currentActivity = null
    }

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity == activity) {
            currentActivity = null
        }
    }
}
