package com.example.ui.components

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.engine.RuneRole
import com.example.engine.StaveInterpretationData
import com.example.ui.theme.RunicStaveTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class InterpretationCardAccessibilityTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun interpretationCard_accordionHasStateDescriptionSemantics() {
        val dummyData = StaveInterpretationData(
            title = "Тестовый Став",
            summary = "Краткое описание става",
            runeRoles = listOf(RuneRole("Феху", "Исток", "Привлечение ресурсов")),
            strokeOrderAdvice = "Сначала верт. штрихи",
            activationSteps = listOf("Произнесите оговор"),
            carryingAdvice = "Носите при себе",
            deactivationAdvice = "Сжжение с благодарностью",
            disclaimer = "Сакральный символ"
        )

        composeTestRule.setContent {
            RunicStaveTheme {
                InterpretationCard(data = dummyData)
            }
        }

        val accordionNode = composeTestRule.onNodeWithText("Инструкция: нанесение и активация")
        accordionNode.assertIsDisplayed()

        val initialSemantics = accordionNode.fetchSemanticsNode()
        val initialStatus = initialSemantics.config.getOrNull(SemanticsProperties.StateDescription)
        assertEquals("Свёрнуто", initialStatus)

        accordionNode.performClick()

        val expandedSemantics = accordionNode.fetchSemanticsNode()
        val expandedStatus = expandedSemantics.config.getOrNull(SemanticsProperties.StateDescription)
        assertEquals("Развёрнуто", expandedStatus)
    }
}
