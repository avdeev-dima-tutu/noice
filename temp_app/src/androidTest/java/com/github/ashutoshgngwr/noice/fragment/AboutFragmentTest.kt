package com.github.ashutoshgngwr.noice.fragment

import android.app.Activity
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.withChild
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.ashutoshgngwr.noice.R
import com.test.ui_test_core.snapshot.*
import com.test.ui_test_core.utils.findActivity
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AboutFragmentTest {

//    @Rule
//    @JvmField
//    val retryTestRule = RetryTestRule(5)

    @Test
    fun testAboutItemClick() {
        launchFragmentInContainer<AboutFragment>()
        Intents.init()
        // can't test everything. Picking one item at random
        onView(withChild(withText(R.string.app_name))).perform(click())
        assertSnapshot2(
            "temp_app",
            expectSnapshot("dir/expect_snap.png"),
            com.test.ui_test_core.utils.wait {
                findActivity<Activity>()
            }.snapshot()
        )
    }
}
