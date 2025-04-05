package com.github.alexapostol2000.hacktemplate.toolWindow

import java.awt.*
import javax.swing.*
import java.util.*
import java.util.Timer
import java.util.TimerTask
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import java.io.BufferedInputStream

class BreakPopupWindow(
    private val durationSeconds: Int,
    private val onBreakFinished: () -> Unit
) : JDialog() {

    private val timeLabel = JLabel("", SwingConstants.CENTER)
    private val skipButton = JButton("Skip Break")
    private var secondsLeft = durationSeconds
    private val timer = Timer()

    init {
        title = "Break Timer"
        isUndecorated = true
        layout = BorderLayout()
        preferredSize = Dimension(320, 180)

        timeLabel.font = Font("Arial", Font.BOLD, 36)
        skipButton.font = Font("Arial", Font.PLAIN, 16)
        skipButton.addActionListener {
            timer.cancel()
            finishBreak()
        }

        val buttonPanel = JPanel().apply {
            layout = FlowLayout()
            add(skipButton)
        }

        add(timeLabel, BorderLayout.CENTER)
        add(buttonPanel, BorderLayout.SOUTH)

        pack()
        setLocationRelativeTo(null)
        isAlwaysOnTop = true
        isModal = false

        startCountdown()
    }

    private fun startCountdown() {
        updateLabel()

        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                secondsLeft--
                SwingUtilities.invokeLater {
                    updateLabel()
                }

                if (secondsLeft <= 0) {
                    timer.cancel()
                    playSound()
                    finishBreak()
                }
            }
        }, 1000, 1000)

        isVisible = true
    }

    private fun updateLabel() {
        val min = secondsLeft / 60
        val sec = secondsLeft % 60
        timeLabel.text = String.format("%02d:%02d", min, sec)
    }

    private fun finishBreak() {
        onBreakFinished()
        dispose()
    }

    private fun playSound() {
        try {
            val audioStream = AudioSystem.getAudioInputStream(
                BufferedInputStream(javaClass.getResourceAsStream("/notification.wav"))
            )
            val clip: Clip = AudioSystem.getClip()
            clip.open(audioStream)
            clip.start()
        } catch (e: Exception) {
            println("Could not play sound: ${e.message}")
        }
    }
}
