package com.hfad.synapflow

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.preference.PreferenceManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.hfad.synapflow.PrefUtil.Companion.getPrevTimerLength
import com.hfad.synapflow.PrefUtil.Companion.getTimeRemaining
import me.zhanghai.android.materialprogressbar.MaterialProgressBar


class TimerFragment : Fragment() {
    enum class TimerState {
        // Used to store the status of the states
        Stop, Pause, Play
    }

    //setting up viewModel
    private val viewModel : TimerViewModel by activityViewModels()
    private lateinit var timer: CountDownTimer
    private lateinit var myContext: Context

    private  lateinit var play : FloatingActionButton
    private  lateinit var pause : FloatingActionButton
    private  lateinit var stop : FloatingActionButton
    private lateinit var progress_countdown: MaterialProgressBar
    private  lateinit var  countdown: TextView
    private  lateinit var  title: TextView

    val fsdb = FirestoreData()
    //--------------------------------------------------------------------//

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.title = "Timer | Synapflow"
        }
    //--------------------------------------------------------------------//
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_timer, container, false)
    }
    //--------------------------------------------------------------------//
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        myContext = view.context
        play = view.findViewById<FloatingActionButton>(R.id.fab_play)
        pause = view.findViewById<FloatingActionButton>(R.id.fab_pause)
        stop = view.findViewById<FloatingActionButton>(R.id.fab_stop)
        progress_countdown = view.findViewById<MaterialProgressBar>(R.id.progress_countdown)
        countdown = view.findViewById<TextView>(R.id.countdown)
        title = view.findViewById<TextView>(R.id.timerTitle)

        // Update UI
        updateButtons()
        updateCountdownUI()
        // If we have timer data, update it with this if the timer is still going.
        if ((viewModel.secondsRemaining.value!! > 0) and (viewModel.timerState.value!! == TimerFragment.TimerState.Play)) {
            val elapsedTime = (System.currentTimeMillis() - viewModel.lastCurrentTime.value!!) / 1000
            val newSecondsRemaining = viewModel.secondsRemaining.value!! - elapsedTime

            // If the timer finished while off the screen
            if (newSecondsRemaining <= 0) {
                viewModel.secondsRemaining.setValue(0)
                updateCountdownUI()
                onTimerFinished()
            } else { // Restart the timer with the new time remaining
                viewModel.secondsRemaining.setValue(newSecondsRemaining)
                viewModel.timer.setValue(object : android.os.CountDownTimer((viewModel.secondsRemaining.value?: 0) * 1000, 1000) {
                    override fun onFinish() = onTimerFinished()
                    override fun onTick(millisUntilFinished: kotlin.Long) {
                        viewModel.lastCurrentTime.setValue(System.currentTimeMillis())
                        viewModel.secondsRemaining.setValue(millisUntilFinished / 1000)
                        updateCountdownUI()
                    }
                })
                viewModel.timer.value!!.start()
                progress_countdown.max = if (viewModel.onStudySession())  viewModel.studyTimerMax.value!!.toInt() else viewModel.breakTimerMax.value!!.toInt()
            }

        } else { // Otherwise reset the wheel to make sure it draws.
            progress_countdown.max = 1500
            progress_countdown.progress = 1499
        }

        // Initial setup
        play.setOnClickListener {
            viewModel.timerState.setValue(TimerFragment.TimerState.Play)
            startTimer()
        }
        pause.setOnClickListener {
            viewModel.timerState.setValue(TimerFragment.TimerState.Pause)
            updateButtons()
            viewModel.timer.value!!.cancel()
        }
        stop.setOnClickListener {
            viewModel.timerState.setValue(TimerFragment.TimerState.Stop)
            viewModel.timer.value!!.cancel()
            onTimerFinished()
        }
    }
    //--------------------------------------------------------------------//
    override fun onResume() {
        // Used on resuming the timer
        super.onResume()
        initTimer()
    }

    override fun onPause() {
        // Information for pausing the timer.
        super.onPause()
        if (viewModel.timerState.value ==  TimerFragment.TimerState.Play){ // If timer is playing cancel it
            viewModel.timer.value!!.cancel()
        } else if (viewModel.timerState.value == TimerFragment.TimerState.Pause) {  // If paused, reset the length
            PrefUtil.setPrevTimerLength((viewModel.timerLengthSeconds.value?: 0), myContext)
            PrefUtil.setTimeRemaining((viewModel.secondsRemaining.value?: 0), myContext)
        }
    }
    private fun initTimer() {
        // Initialized the timer
        if (viewModel.getTimerState() == TimerFragment.TimerState.Stop) { // If  the timers state is stopped do a study session
            setNewTimerLength()
        } else { // Otherwise set it with the previous timer length
            setPreviousTimerLength()
        }// Update the secondsremaining data.
        if (viewModel.getTimerState() == TimerFragment.TimerState.Play || viewModel.getTimerState() == TimerFragment.TimerState.Pause) {
            viewModel.secondsRemaining.setValue(getTimeRemaining(myContext))
        } else {
            viewModel.secondsRemaining = viewModel.timerLengthSeconds
        }
    }
    private fun onTimerFinished() {
        // Logic for when the timer finishes
        //check if timer has ended
        if(viewModel.getTimerState() == TimerFragment.TimerState.Play && viewModel.secondsRemaining.value!! <= 0L){
            // Output a message
            Toast.makeText(context, "Time is up!", Toast.LENGTH_SHORT).show()

            // Update the study session count, only if its a finished study session
            if (viewModel.onStudySession())
                fsdb.onTimerCompletion()
            // Change between study/break session
            viewModel.updateSession()
        }
        viewModel.timerState.setValue(TimerState.Stop) // Turn off the timer
        setNewTimerLength() // Change the timer length
        progress_countdown.progress = 0 // Update UI wheel
        PrefUtil.setTimeRemaining((viewModel.timerLengthSeconds.value?: 0), myContext)
        viewModel.secondsRemaining = viewModel.timerLengthSeconds
        // Update UI
        updateButtons()
        updateCountdownUI()
    }


    private fun startTimer() {
        // Creates our overriden timer
        viewModel.timerState.setValue( TimerFragment.TimerState.Play)
        viewModel.timer.setValue(object : CountDownTimer((viewModel.secondsRemaining.value?: 0) * 1000, 1000) {
            override fun onFinish() = onTimerFinished() // Calls func on finish
            override fun onTick(millisUntilFinished: Long) { // Updates LiveModel Data
                viewModel.lastCurrentTime.setValue(System.currentTimeMillis())
                viewModel.secondsRemaining.setValue(millisUntilFinished / 1000)
                updateCountdownUI()
            }
        })
        viewModel.timer.value!!.start() // Starts the timer
        updateButtons() // Updates the UI
    }


    private fun setNewTimerLength() {
        // Sets the logic for the new timer
        var lengthInMinutes : Long // Start a study session Timer
        if (viewModel.onStudySession())
            lengthInMinutes = PrefUtil.getTimerLength()
        else // Starts a break timer
            lengthInMinutes = PrefUtil.getBreakTimerLength()
        viewModel.timerLengthSeconds.setValue(lengthInMinutes * 60L)  // Updates the timer logic
        progress_countdown.max = (viewModel.timerLengthSeconds.value!!).toInt() // updates the timer wheel
    }

    private fun setPreviousTimerLength() {
        // Updates prev timer
        viewModel.timerLengthSeconds.setValue(getPrevTimerLength(myContext))
    }

    private fun updateCountdownUI() {
        // Updates the countdown UI data
        title.text = if (viewModel.onStudySession())  "Study Time!" else "Break Time!" // Updates the text on top
        countdown.text = viewModel.tSecondsRemainingStr() // updates timer
        progress_countdown.progress = viewModel.secondsRemaining.value!!.toInt() // updates countdown wheel data
    }

    private fun updateButtons() {
        // Changes what buttons are active based on the state.
        when (viewModel.timerState.value) {
            TimerFragment.TimerState.Play -> {
                play.isEnabled  = false
                pause.isEnabled = true
                stop.isEnabled  = true
            }
            TimerFragment.TimerState.Pause -> {
                play.isEnabled  = true
                pause.isEnabled = false
                stop.isEnabled  = true
            }
            TimerFragment.TimerState.Stop -> {
                play.isEnabled  = true
                pause.isEnabled = false
                stop.isEnabled  = false
            }
            null -> {
                //Null parameter
            }
        }

    }


}
//--------------------------------------------------------------------//
class PrefUtil {
    /*
    Helper function for timer.
     */
    companion object {
        // Id data
        private const val PREV_TIMER_ID = "com.hfad.synapflow.timer.prev_timer_id"
        private const val TIMER_STATE_ID = "com.hfad.synapflow.timer.state_id"
        private const val TIME_REMAINING_ID = "com.hfad.synapflow.timer.remaining_id"
        fun getTimerLength(): Long {
            // Timer length for study
            return 25
        }
        fun getBreakTimerLength(): Long {
            // Timer length for break
            return 5
        }

        fun getPrevTimerLength(context: Context): Long {
            // gets prev timer length
            val pref = PreferenceManager.getDefaultSharedPreferences(context)
            return pref.getLong(PREV_TIMER_ID, 0)
        }
        fun setPrevTimerLength(time: Long, context: Context) {
            // Sets prev timer lenght
            val pref = PreferenceManager.getDefaultSharedPreferences(context).edit()
            pref.putLong(PREV_TIMER_ID, time).apply()
        }
        fun getTimeRemaining(context: Context): Long {
            // Gets the  timer length
            val pref = PreferenceManager.getDefaultSharedPreferences(context)
            return pref.getLong(TIME_REMAINING_ID, 0)
        }
        fun setTimeRemaining(time: Long, context: Context) {
            // Sets the timer length
            val pref = PreferenceManager.getDefaultSharedPreferences(context).edit()
            pref.putLong(TIME_REMAINING_ID, time).apply()
        }
    }
}
