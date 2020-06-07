package com.test.ui_test_core.utils

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

inline fun <reified T : Fragment> findFragment(): T? = fragmentSequence().mapNotNull { it as? T }.firstOrNull()
inline fun <reified T : Activity> findActivity(): T? = activitySequence().mapNotNull { it as? T }.firstOrNull()

inline fun <reified T : View> Fragment.findView(): T? = view?.findView()
inline fun <reified T : View> Activity.findView(): T? = window.decorView.findView<T>()

inline fun <reified T : View> View.findView(): T? = viewSequence(this).mapNotNull { it as? T }.firstOrNull()

/**
 * Ждём чтобы lambda() != null
 */
fun <T : Any> wait(lambda: suspend () -> T?): T = runBlocking {
    waitNotNullSuspend(lambda)
}

/**
 * Ждём чтобы lambda() != null
 */
tailrec suspend fun <T : Any> waitNotNullSuspend(lambda: suspend () -> T?): T =
        lambda() ?: waitNotNullSuspend {
            delay(1)//todo try yield
            lambda()
        }

/**
 * Ждём чтобы lambda() != null
 */
fun <This, T : Any> This.wait(lambda: suspend This.() -> T?): T {
    val wrapper: suspend () -> T? = { lambda(this) }
    return wait(wrapper)
}

fun activitySequence(): Sequence<Activity> = runBlocking {
    withContext(Dispatchers.Main) {
        val result = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED)
        sequence {
            yieldAll(result)
        }
    }
}

fun fragmentSequence(): Sequence<Fragment> = sequence {
    activitySequence().forEach {
        yieldAll(fragmentSequence(it))
    }
}

fun fragmentSequence(fragmentManager: FragmentManager): Sequence<Fragment> = sequence {
    yieldAll(
            fragmentManager.fragments.filter { it.isVisible }
    )
    fragmentManager.fragments.forEach {
        yieldAll(fragmentSequence(it.childFragmentManager))
    }
}

fun fragmentSequence(activity: Activity): Sequence<Fragment> = sequence {
    if (activity is AppCompatActivity) {
        yieldAll(fragmentSequence(activity.supportFragmentManager))
    }
    if (activity is FragmentActivity) {
        yieldAll(fragmentSequence(activity.supportFragmentManager))
    }
}

fun viewSequence(view: View): Sequence<View> = sequence {
    yield(view)
    if (view is ViewGroup) {
        for (i in 0 until view.childCount) {
            yieldAll(
                    viewSequence(view.getChildAt(i))
            )
        }
    }
}
