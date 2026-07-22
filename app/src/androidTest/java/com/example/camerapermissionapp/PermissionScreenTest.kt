package com.example.camerapermissionapp

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.camerapermissionapp.ui.permission.GetPermissionScreen
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PermissionScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun permisionText_Is_Displayed() {
        composeRule.setContent { GetPermissionScreen(onRequestPermission = {}) }

        val text = composeRule.activity.getString(R.string.please_grant_the_camera_permission)
        composeRule.onNodeWithText(text).assertExists()
    }

    @Test
    fun clicking_GrantPermissionButton_Invokes_Callback() {
        var clicked = false
        composeRule.setContent { GetPermissionScreen(onRequestPermission = { clicked = true }) }

        val label = composeRule.activity.getString(R.string.grant_permission)
        composeRule.onNodeWithText(label).performClick()

        assertTrue(clicked)
    }
}
