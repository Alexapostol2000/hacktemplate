package com.github.alexapostol2000.hacktemplate.toolWindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.ui.components.JBLabel
import java.util.*
import java.util.Timer
import java.util.TimerTask
import javax.swing.JComponent
import javax.swing.SwingUtilities

class BreakReminderManager(
    private val project: Project,
    private val timeSinceStartLabel: JBLabel,
    private val sessionStatsLabel: JBLabel,
    private val labelsToHideDuringBreak: List<JComponent>
) {
    private val breakIntervalMinutes = 1            // Test rapid
    private val breakDurationSeconds = 30           // Test rapid

    private var secondsSinceStart = 0
    private var codingSecondsTotal = 0
    private var breakSecondsTotal = 0
    private var breakInProgress = false
    private var lastBreakPromptMinute = -1

    private val timer = Timer()

    init {
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                if (breakInProgress) return

                secondsSinceStart++
                codingSecondsTotal++

                SwingUtilities.invokeLater {
                    val min = secondsSinceStart / 60
                    val sec = secondsSinceStart % 60
                    timeSinceStartLabel.text = "Time since start: ${min} min ${sec} sec"

                    sessionStatsLabel.text = "Coding: ${codingSecondsTotal / 60} min | Breaks: ${breakSecondsTotal / 60} min"
                }

                val currentMinute = secondsSinceStart / 60
                if (currentMinute != 0 && currentMinute % breakIntervalMinutes == 0 && currentMinute != lastBreakPromptMinute) {
                    lastBreakPromptMinute = currentMinute

                    SwingUtilities.invokeLater {
                        val response = Messages.showYesNoDialog(
                            project,
                            "Au trecut $currentMinute minute. Vrei să iei o pauză de ${breakDurationSeconds / 60} minute?",
                            "Pauză recomandată",
                            "Da",
                            "Nu",
                            null
                        )
                        if (response == Messages.YES) {
                            startBreakTimer()
                        }
                    }
                }
            }
        }, 0, 1000)
    }

    private fun startBreakTimer() {
        breakInProgress = true

        SwingUtilities.invokeLater {
            labelsToHideDuringBreak.forEach { it.isVisible = false }

            BreakPopupWindow(breakDurationSeconds) {
                breakInProgress = false
                breakSecondsTotal += breakDurationSeconds

                SwingUtilities.invokeLater {
                    labelsToHideDuringBreak.forEach { it.isVisible = true }

                    Messages.showInfoMessage(
                        project,
                        "Pauza s-a terminat. Welcome back!",
                        "Gata cu pauza!"
                    )

                    // update UI imediat după pauză
                    sessionStatsLabel.text = "Coding: ${codingSecondsTotal / 60} min | Breaks: ${breakSecondsTotal / 60} min"
                }
            }
        }
    }
}
