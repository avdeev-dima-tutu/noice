package com.github.ashutoshgngwr.noice.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.filterEquals
import androidx.test.espresso.matcher.ViewMatchers.withChild
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.ashutoshgngwr.noice.R
import com.github.ashutoshgngwr.noice.RetryTestRule
import com.test.ui_test_core.snapshot.*
import com.test.ui_test_core.utils.findActivity
import com.test.ui_test_core.utils.wait
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import ru.tutu.snapshot.upload.sendMultipart

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
            "app",
            expectSnapshot("dir/expect_snap.png"),
            com.test.ui_test_core.utils.wait {
                findActivity<Activity>()
            }.snapshot()
        )
    }
}
