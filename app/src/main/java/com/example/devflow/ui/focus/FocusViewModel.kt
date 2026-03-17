package com.example.devflow.ui.focus

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class TimerMode { WORK, SHORT_BREAK, LONG_BREAK }

data class FocusState(
    val workSeconds: Int = 25 * 60,
    val shortBreakSeconds: Int = 5 * 60,
    val longBreakSeconds: Int = 15 * 60,
    val totalSeconds: Int = 25 * 60,
    val remainingSeconds: Int = 25 * 60,
    val isRunning: Boolean = false,
    val mode: TimerMode = TimerMode.WORK,
    val sessionsCompleted: Int = 0,
    val todayFocusMinutes: Int = 0,
    val audioUri: Uri? = null,
    val audioFileName: String = "No audio selected"
)

class FocusViewModel : ViewModel() {

    private val _state = MutableStateFlow(FocusState())
    val state: StateFlow<FocusState> = _state

    private var timerJob: Job? = null
    private var mediaPlayer: MediaPlayer? = null

    fun startStop(context: Context) {
        if (_state.value.isRunning) pause() else start(context)
    }

    private fun start(context: Context) {
        _state.value = _state.value.copy(isRunning = true)
        startAudio(context)
        timerJob = viewModelScope.launch {
            while (_state.value.remainingSeconds > 0 && _state.value.isRunning) {
                delay(1000)
                _state.value = _state.value.copy(
                    remainingSeconds = _state.value.remainingSeconds - 1
                )
            }
            if (_state.value.remainingSeconds == 0) onTimerFinished()
        }
    }

    private fun pause() {
        timerJob?.cancel()
        pauseAudio()
        _state.value = _state.value.copy(isRunning = false)
    }

    fun reset(context: Context) {
        timerJob?.cancel()
        stopAudio()
        _state.value = _state.value.copy(
            remainingSeconds = _state.value.totalSeconds,
            isRunning = false
        )
    }

    fun setMode(mode: TimerMode, context: Context) {
        timerJob?.cancel()
        stopAudio()
        val seconds = when (mode) {
            TimerMode.WORK        -> _state.value.workSeconds
            TimerMode.SHORT_BREAK -> _state.value.shortBreakSeconds
            TimerMode.LONG_BREAK  -> _state.value.longBreakSeconds
        }
        _state.value = _state.value.copy(
            mode = mode,
            totalSeconds = seconds,
            remainingSeconds = seconds,
            isRunning = false
        )
    }

    fun setCustomDuration(minutes: Int) {
        timerJob?.cancel()
        val seconds = minutes * 60
        _state.value = _state.value.copy(
            totalSeconds = seconds,
            remainingSeconds = seconds,
            isRunning = false
        )
    }

    fun saveWorkDuration(minutes: Int) {
        val seconds = minutes * 60
        _state.value = _state.value.copy(
            workSeconds = seconds,
            totalSeconds = if (_state.value.mode == TimerMode.WORK) seconds
            else _state.value.totalSeconds,
            remainingSeconds = if (_state.value.mode == TimerMode.WORK) seconds
            else _state.value.remainingSeconds
        )
    }

    fun saveBreakDuration(isShort: Boolean, minutes: Int) {
        val seconds = minutes * 60
        _state.value = if (isShort) {
            _state.value.copy(
                shortBreakSeconds = seconds,
                totalSeconds = if (_state.value.mode == TimerMode.SHORT_BREAK) seconds
                else _state.value.totalSeconds,
                remainingSeconds = if (_state.value.mode == TimerMode.SHORT_BREAK) seconds
                else _state.value.remainingSeconds
            )
        } else {
            _state.value.copy(
                longBreakSeconds = seconds,
                totalSeconds = if (_state.value.mode == TimerMode.LONG_BREAK) seconds
                else _state.value.totalSeconds,
                remainingSeconds = if (_state.value.mode == TimerMode.LONG_BREAK) seconds
                else _state.value.remainingSeconds
            )
        }
    }

    fun setAudio(context: Context, uri: Uri, fileName: String) {
        stopAudio()
        _state.value = _state.value.copy(
            audioUri = uri,
            audioFileName = fileName
        )
    }

    fun clearAudio(context: Context) {
        stopAudio()
        _state.value = _state.value.copy(
            audioUri = null,
            audioFileName = "No audio selected"
        )
    }

    private fun startAudio(context: Context) {
        val uri = _state.value.audioUri ?: return
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, uri)
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun pauseAudio() {
        try {
            if (mediaPlayer?.isPlaying == true) mediaPlayer?.pause()
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun stopAudio() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun onTimerFinished() {
        stopAudio()
        val current = _state.value
        val newSessions = if (current.mode == TimerMode.WORK)
            current.sessionsCompleted + 1 else current.sessionsCompleted
        val newFocusMinutes = if (current.mode == TimerMode.WORK)
            current.todayFocusMinutes + (current.totalSeconds / 60)
        else current.todayFocusMinutes
        _state.value = current.copy(
            isRunning = false,
            sessionsCompleted = newSessions,
            todayFocusMinutes = newFocusMinutes
        )
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        stopAudio()
    }
}