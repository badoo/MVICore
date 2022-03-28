package com.bumble

import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.bumble.binder.orderfailure.R
import com.bumble.binder.orderfailure.prebindevent.PreBindEventActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PreBindEventActivityTest {

    @get:Rule
    val activityRule = ActivityTestRule(PreBindEventActivity::class.java)

    @Test
    fun `GIVEN_the_screen_launched_THEN_the_title_should_not_be_visible`() {
        Espresso.onView(ViewMatchers.withId(R.id.title))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

}