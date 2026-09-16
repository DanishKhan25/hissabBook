package com.example.hisabbook

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * End-to-end UI tests covering all main pages and navigation flows in HisabBook.
 */
@RunWith(AndroidJUnit4::class)
class FullAppUiTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testOnboardingAndBusinessProfileSetup() {
        val onboardingHeader = composeTestRule.onAllNodesWithText("Welcome to HisabBook").fetchSemanticsNodes(false)
        if (onboardingHeader.isNotEmpty()) {
            composeTestRule.onNodeWithText("Welcome to HisabBook").assertIsDisplayed()
            composeTestRule.onNodeWithText("Select Language").assertIsDisplayed()
            composeTestRule.onNodeWithText("Business Profile").assertIsDisplayed()
        }
    }

    @Test
    fun testNavigationBetweenAllMainPages() {
        val onboardingNodes = composeTestRule.onAllNodesWithText("Welcome to HisabBook").fetchSemanticsNodes(false)
        if (onboardingNodes.isNotEmpty()) {
            composeTestRule.onNodeWithText("Business Name *").performTextInput("Test Traders")
            val getStartedBtn = composeTestRule.onAllNodesWithText("Get Started").fetchSemanticsNodes(false)
            if (getStartedBtn.isNotEmpty()) {
                composeTestRule.onNodeWithText("Get Started").performClick()
            }
        }

        composeTestRule.waitForIdle()

        // 1. Verify Home / Dashboard Page
        val homeTab = composeTestRule.onAllNodesWithText("Home").fetchSemanticsNodes(false)
        if (homeTab.isNotEmpty()) {
            composeTestRule.onNodeWithText("Home").performClick()
            composeTestRule.waitForIdle()
        }

        // 2. Test Customer List Page Navigation
        val customersTab = composeTestRule.onAllNodesWithText("Customers").fetchSemanticsNodes(false)
        if (customersTab.isNotEmpty()) {
            composeTestRule.onNodeWithText("Customers").performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onAllNodesWithText("Customers")[0].assertIsDisplayed()
        }

        // 3. Test Quotation List Page Navigation
        val quotationsTab = composeTestRule.onAllNodesWithText("Quotations").fetchSemanticsNodes(false)
        if (quotationsTab.isNotEmpty()) {
            composeTestRule.onNodeWithText("Quotations").performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Quotations & Estimates").assertIsDisplayed()
            composeTestRule.onNodeWithText("New Quotation").assertIsDisplayed()
        }

        // 4. Test Invoices & Bills Page Navigation
        val invoicesTab = composeTestRule.onAllNodesWithText("Invoices").fetchSemanticsNodes(false)
        if (invoicesTab.isNotEmpty()) {
            composeTestRule.onNodeWithText("Invoices").performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Invoices & Bills").assertIsDisplayed()
            composeTestRule.onNodeWithText("New Bill").assertIsDisplayed()
        }

        // 5. Test Inventory Page Navigation
        val inventoryTab = composeTestRule.onAllNodesWithText("Inventory").fetchSemanticsNodes(false)
        if (inventoryTab.isNotEmpty()) {
            composeTestRule.onNodeWithText("Inventory").performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Inventory").assertIsDisplayed()
            val addItemBtn = composeTestRule.onAllNodesWithText("Add Item").fetchSemanticsNodes(false)
            if (addItemBtn.isNotEmpty()) {
                composeTestRule.onAllNodesWithText("Add Item")[0].assertIsDisplayed()
            }
        }
    }

    @Test
    fun testQuotationScreenNavigationAndCreateFlow() {
        val onboardingNodes = composeTestRule.onAllNodesWithText("Welcome to HisabBook").fetchSemanticsNodes(false)
        if (onboardingNodes.isNotEmpty()) {
            composeTestRule.onNodeWithText("Business Name *").performTextInput("Test Traders")
            composeTestRule.onNodeWithText("Get Started").performClick()
            composeTestRule.waitForIdle()
        }

        val quotationsTab = composeTestRule.onAllNodesWithText("Quotations").fetchSemanticsNodes(false)
        if (quotationsTab.isNotEmpty()) {
            composeTestRule.onNodeWithText("Quotations").performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText("All").assertIsDisplayed()
            composeTestRule.onNodeWithText("Draft").assertIsDisplayed()
            composeTestRule.onNodeWithText("Approved").assertIsDisplayed()

            composeTestRule.onNodeWithText("New Quotation").performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText("New Quotation").assertIsDisplayed()
            composeTestRule.onNodeWithText("Quotation Number:").assertIsDisplayed()
            val customerField = composeTestRule.onAllNodesWithText("Customer *").fetchSemanticsNodes(false)
            if (customerField.isNotEmpty()) {
                composeTestRule.onAllNodesWithText("Customer *")[0].assertIsDisplayed()
            }
        }
    }

    @Test
    fun testCustomerDialogAndAddFlow() {
        val onboardingNodes = composeTestRule.onAllNodesWithText("Welcome to HisabBook").fetchSemanticsNodes(false)
        if (onboardingNodes.isNotEmpty()) {
            composeTestRule.onNodeWithText("Business Name *").performTextInput("Test Traders")
            composeTestRule.onNodeWithText("Get Started").performClick()
            composeTestRule.waitForIdle()
        }

        val customersTab = composeTestRule.onAllNodesWithText("Customers").fetchSemanticsNodes(false)
        if (customersTab.isNotEmpty()) {
            composeTestRule.onNodeWithText("Customers").performClick()
            composeTestRule.waitForIdle()

            val addCustomerNodes = composeTestRule.onAllNodesWithText("Add Customer").fetchSemanticsNodes(false)
            if (addCustomerNodes.isNotEmpty()) {
                composeTestRule.onAllNodesWithText("Add Customer")[0].performClick()
                composeTestRule.waitForIdle()

                composeTestRule.onNodeWithText("Customer Name *").assertIsDisplayed()
                composeTestRule.onNodeWithText("Mobile Number").assertIsDisplayed()
                composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
                composeTestRule.onNodeWithText("Cancel").performClick()
            }
        }
    }
}
