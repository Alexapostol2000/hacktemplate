package com.github.alexapostol2000.hacktemplate.toolWindow

import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.SwingUtilities
import javax.swing.JOptionPane

class MyToolWindow(project: Project) {

    private val timeSinceStartLabel = JBLabel("Time since start: 0 min 0 sec")
    private val failedBuildLabel = JBLabel("Failed builds (last 1 min): 0")
    private val sessionStatsLabel = JBLabel("Coding: 0 min | Breaks: 0 min")
    private val breakToggleButton = JButton("Start Break")

    private val panel = JBPanel<JBPanel<*>>().apply {
        layout = javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS)
        add(timeSinceStartLabel)
        add(failedBuildLabel)
        add(sessionStatsLabel)
        add(breakToggleButton)
    }

    private val breakReminder = BreakReminderManager(
        project = project,
        timeSinceStartLabel = timeSinceStartLabel,
        sessionStatsLabel = sessionStatsLabel,
        labelsToHideDuringBreak = listOf(failedBuildLabel, sessionStatsLabel, breakToggleButton),
        breakToggleButton = breakToggleButton
    )

    private val tracker = BuildFailureTracker(project, failedBuildLabel) { recentFails ->
        if (recentFails >= 10) {
            SwingUtilities.invokeLater {
                val response = JOptionPane.showConfirmDialog(
                    null,
                    "Ai avut $recentFails builduri eșuate în ultima minută. Vrei să iei o pauză?",
                    "Recomandare pauză",
                    JOptionPane.YES_NO_OPTION
                )
                if (response == JOptionPane.YES_OPTION) {
                    breakReminder.startManualBreak()
                }
            }
        }
    }

    fun getContent(): JPanel = panel
}
