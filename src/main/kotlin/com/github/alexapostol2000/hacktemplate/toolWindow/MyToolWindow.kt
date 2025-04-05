package com.github.alexapostol2000.hacktemplate.toolWindow

import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import javax.swing.JPanel

class MyToolWindow(project: Project) {

    private val timeSinceStartLabel = JBLabel("Time since start: 0 min 0 sec")
    private val failedBuildLabel = JBLabel("Failed builds (last 1 min): 0")
    private val sessionStatsLabel = JBLabel("Coding: 0 min | Breaks: 0 min")

    private val panel = JBPanel<JBPanel<*>>().apply {
        layout = javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS)
        add(timeSinceStartLabel)
        add(failedBuildLabel)
        add(sessionStatsLabel)
    }

    private val tracker = BuildFailureTracker(project, failedBuildLabel)
    private val breakReminder = BreakReminderManager(
        project,
        timeSinceStartLabel,
        sessionStatsLabel,
        listOf(failedBuildLabel, sessionStatsLabel) // 👈 le ascundem în pauză
    )

    fun getContent(): JPanel = panel
}

