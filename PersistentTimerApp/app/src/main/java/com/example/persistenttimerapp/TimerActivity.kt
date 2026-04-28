package com.example.persistenttimerapp

import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import com.example.persistenttimerapp.databinding.ActivityTimerBinding
import java.util.*

class TimerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTimerBinding
    private lateinit var dataHelper: DataHelper
    private var countDownTimer: CountDownTimer? = null

    private var alarmRingtone: Ringtone? = null
    private var alarmFired: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dataHelper = DataHelper(applicationContext)

        binding.startButton.setOnClickListener { startStopTimer() }
        binding.resetButton.setOnClickListener { resetTimer() }
        binding.calendarButton.setOnClickListener {
            val intent = Intent(this, Calendar_ToDo_Activity::class.java)
            startActivity(intent)
        }
        binding.preset25Button.setOnClickListener { setPreset(25) }
        binding.preset5Button.setOnClickListener { setPreset(5) }
        binding.preset15Button.setOnClickListener { setPreset(15) }

        if (dataHelper.timerCounting()) {
            startTimer()
        } else {
            stopTimer()
            if (dataHelper.startTime() != null && dataHelper.stopTime() != null) {
                val time = calculateTime()
                binding.timeTV.text = formatTime(time)
            }
        }

        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                if (dataHelper.timerCounting()) {
                    val time = calculateTime()
                    binding.timeTV.text = formatTime(time)

                    // Fire alarm exactly once when the countdown reaches zero
                    if (time <= 0L && !alarmFired) {
                        onTimerFinished()
                    }
                }
                handler.postDelayed(this, 1000)
            }
        })
    }

    private fun setPreset(minutes: Int) {
        // Clear any existing timer state
        dataHelper.setStopTime(null)
        dataHelper.setStartTime(null)
        dataHelper.setTimerCounting(false)
        stopAlarm()
        alarmFired = false

        // Store the preset duration without starting the timer
        val durationMs = minutes * 60 * 1000L
        dataHelper.setCountdownDuration(durationMs)

        // Populate the input fields so the user can see/adjust the preset
        binding.inputHours.setText("0")
        binding.inputMinutes.setText(minutes.toString())
        binding.inputSeconds.setText("0")

        // Update the displayed time and make sure the start button reads "Start"
        binding.timeTV.text = formatTime(durationMs)
        stopTimer()
    }

    private fun startStopTimer() {
        // If the alarm is currently ringing (timer ended), pressing the button just silences it
        // and returns the UI to a clean stopped state, without starting a new countdown.
        if (alarmFired) {
            stopAlarm()
            alarmFired = false
            stopTimer()
            return
        }

        if (dataHelper.timerCounting()) {
            dataHelper.setStopTime(Date())
            dataHelper.setTimerCounting(false)
            stopTimer()
        } else {
            if (dataHelper.stopTime() != null) {
                val delta = Date().time - dataHelper.stopTime()!!.time
                dataHelper.setStartTime(Date(dataHelper.startTime()!!.time + delta))
            } else {
                val hours = binding.inputHours.text.toString().toLongOrNull() ?: 0
                val minutes = binding.inputMinutes.text.toString().toLongOrNull() ?: 0
                val seconds = binding.inputSeconds.text.toString().toLongOrNull() ?: 0
                val totalMs = (hours * 3600 + minutes * 60 + seconds) * 1000
                dataHelper.setCountdownDuration(totalMs)
                dataHelper.setStartTime(Date())
            }
            dataHelper.setStopTime(null)
            dataHelper.setTimerCounting(true)
            alarmFired = false
            startTimer()
        }
    }

    private fun startTimer() {
        binding.startButton.text = getString(R.string.stop)
    }

    private fun stopTimer() {
        binding.startButton.text = getString(R.string.start)
    }

    private fun resetTimer() {
        dataHelper.setStopTime(null)
        dataHelper.setStartTime(null)
        dataHelper.setTimerCounting(false)
        dataHelper.setCountdownDuration(0)
        stopAlarm()
        alarmFired = false
        stopTimer()
        binding.timeTV.text = formatTime(0)
    }

    private fun onTimerFinished() {
        alarmFired = true
        // Mark the timer as no longer counting so the display freezes at 00:00:00.
        // Note: we intentionally do NOT call stopTimer() here — the button stays labeled
        // "Stop" so the user can press it to silence the alarm intuitively.
        dataHelper.setTimerCounting(false)
        binding.timeTV.text = formatTime(0)
        playAlarm()
    }

    private fun playAlarm() {
        try {
            // Prefer the user's alarm tone; fall back to notification, then ringtone
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

            alarmRingtone = RingtoneManager.getRingtone(applicationContext, uri)
            alarmRingtone?.play()
        } catch (e: Exception) {
            // If sound playback fails for any reason, fail silently rather than crashing
            e.printStackTrace()
        }
    }

    private fun stopAlarm() {
        try {
            if (alarmRingtone?.isPlaying == true) {
                alarmRingtone?.stop()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        alarmRingtone = null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlarm()
    }

    private fun calculateTime(): Long {
        val startTime = dataHelper.startTime() ?: return 0
        val duration = dataHelper.getCountdownDuration()
        val now = if (dataHelper.timerCounting()) Date().time else dataHelper.stopTime()?.time ?: Date().time
        val elapsed = now - startTime.time
        val remaining = duration - elapsed
        return if (remaining < 0) 0 else remaining
    }

    private fun formatTime(ms: Long): String {
        val seconds = (ms / 1000) % 60
        val minutes = (ms / (1000 * 60)) % 60
        val hours = (ms / (1000 * 60 * 60))
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}