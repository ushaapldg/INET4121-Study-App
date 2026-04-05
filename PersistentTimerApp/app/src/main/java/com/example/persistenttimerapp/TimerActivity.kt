package com.example.persistenttimerapp

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import com.example.persistenttimerapp.databinding.ActivityTimerBinding
import java.util.*

class TimerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTimerBinding
    private lateinit var dataHelper: DataHelper
    private var countDownTimer: CountDownTimer? = null

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
                }
                handler.postDelayed(this, 1000)
            }
        })
    }

    private fun startStopTimer() {
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
        stopTimer()
        binding.timeTV.text = formatTime(0)
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
