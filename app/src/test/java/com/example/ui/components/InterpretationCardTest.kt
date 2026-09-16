package com.example.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.engine.RuneRole
import com.example.engine.StaveInterpretationData
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class InterpretationCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun interpretationCard_rendersAndTogglesAccordion() {
        val sampleData = StaveInterpretationData(
            title = "Тестовый Став Защиты",
            summary = "Общее толкование тестового става для проверки интерфейса.",
            runeRoles = listOf(
                RuneRole("Fehu", "Исток", "Привлечение энергии и ресурсов."),
                RuneRole("Uruz", "Врата", "Наделение физической силой.")
            ),
            strokeOrderAdvice = "Наносите сначала центральные оси, затем боковые ветви.",
            activationSteps = listOf("Оговорите намерение.", "Активируйте дыханием."),
            carryingAdvice = "Носить при себе как амулет.",
            deactivationAdvice = "С благодарностью сжечь.",
            disclaimer = "Символический арт-инструмент."
        )

        composeTestRule.setContent {
            InterpretationCard(data = sampleData)
        }

        // Verify summary and title rendered
        composeTestRule.onNodeWithText("Тестовый Став Защиты").assertIsDisplayed()
        composeTestRule.onNodeWithText("Общее толкование тестового става для проверки интерфейса.").assertIsDisplayed()

        // Accordion initially collapsed
        composeTestRule.onNodeWithText("Инструкция: нанесение и активация").assertIsDisplayed()

        // Click accordion header
        composeTestRule.onNodeWithText("Инструкция: нанесение и активация").performClick()
        composeTestRule.waitForIdle()

        // Verify expanded content exists in hierarchy
        composeTestRule.onNodeWithText("Наносите сначала центральные оси, затем боковые ветви.").assertExists()
        composeTestRule.onNodeWithText("1. Оговорите намерение.").assertExists()
    }
}
