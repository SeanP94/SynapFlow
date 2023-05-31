package com.hfad.synapflow
import android.os.CountDownTimer
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


class TimerViewModel :ViewModel() {
    /*
        Live data model for saving data in the timer while accessing other elements.
     */
    var studyTimerMax = MutableLiveData<Long>(PrefUtil.getTimerLength() * 60)
    var breakTimerMax = MutableLiveData<Long>(PrefUtil.getBreakTimerLength() * 60)
    var timerLengthSeconds = MutableLiveData<Long>(0)
    var timerState = MutableLiveData<TimerFragment.TimerState>(TimerFragment.TimerState.Stop)
    var secondsRemaining = MutableLiveData<Long>(studyTimerMax.value!!)
    var secondsRemainingStr = MutableLiveData<String>()
    var studySessionState = MutableLiveData<Boolean>(true)
    var lastCurrentTime = MutableLiveData<Long>()
    var timer = MutableLiveData<CountDownTimer>()


    fun updateSession() {
        // Flips thethe session to true or false based on what it previously was
        if (studySessionState.value!!)
            studySessionState.setValue(false)
        else
            studySessionState.setValue(true)
    }

    fun onStudySession() : Boolean {
        // Returns if the study session is on or if its break
        if (studySessionState.value?: true)
            return true
        return false
    }

    fun tSecondsRemainingStr() : String? {
        // Returns the formatted time string. To be a timer like 24:59
        val minutesUntilFinished = (secondsRemaining.value?: 0) / 60
        val secondsInMinutesUntilFinished = (secondsRemaining.value?: 0) - minutesUntilFinished * 60
        val secondsStr = secondsInMinutesUntilFinished.toString()
        var newStr = "$minutesUntilFinished:${ // Adds a 0 in the case otherwise 24:50 might be 24:5
            if (secondsStr.length == 2) secondsStr 
            else "0"+ secondsStr}"
        secondsRemainingStr.setValue(newStr)
        return secondsRemainingStr.value?.toString()
    }

    fun getTimerState(): TimerFragment.TimerState? {
        // Gets the state of the timer.
        return timerState.value
    }
}