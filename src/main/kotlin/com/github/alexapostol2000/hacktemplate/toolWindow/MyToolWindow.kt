package com.github.alexapostol2000.hacktemplate.toolWindow

import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import javax.swing.JPanel

class MyToolWindow(project: Project) {

    private val failedBuildLabel = JBLabel("Failed builds (last 1 min): 0")
    private val tracker = BuildFailureTracker(project, failedBuildLabel)

    fun getContent(): JPanel {
        return JBPanel<JBPanel<*>>().apply {
            add(failedBuildLabel)
        }
    }
}
