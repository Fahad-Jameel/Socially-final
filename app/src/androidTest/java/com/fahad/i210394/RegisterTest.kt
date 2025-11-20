package com.fahad.i210394

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegisterTest {
    @get:Rule
    var activityRule: ActivityTestRule<Login> = ActivityTestRule(Login::class.java)

    @Before
    fun setUp() {
        Intents.init() // Initialize Espresso Intents to verify activity transitions
    }

    @After
    fun tearDown() {
        Intents.release() // Clean up Espresso Intents
    }

    @Test
    fun testRegisterTextViewLaunchesRegisterActivity() {
        // Perform click on the register TextView
        onView(withId(R.id.regt)).perform(click())

        // Verify that Register activity is launched
        Intents.intended(hasComponent(Register::class.java.name))
    }
}