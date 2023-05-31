package com.hfad.synapflow

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hfad.synapflow.databinding.CalendarDayLayoutBinding
import com.hfad.synapflow.databinding.CalendarEventItemViewBinding
import com.hfad.synapflow.databinding.FragmentCalendarBinding
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.*


data class Event(
        val id: String,
        val time: String,
        val text: String,
        val desc: String,
        val date: LocalDate,
        var completed: Boolean
    )
class CalendarEventsAdapter(val onClick: (Event) -> Unit) :
    RecyclerView.Adapter<CalendarEventsAdapter.CalendarEventsViewHolder>() {

    // Variable for storing mutable list of events
    val events = mutableListOf<Event>()

    // Creates and returns a 'CalendarEventsViewHolder' instance that is
    // called by 'RecyclerView' when a new ViewHolder instance needs to be created.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarEventsViewHolder {
        return CalendarEventsViewHolder(
            CalendarEventItemViewBinding.inflate(parent.context.layoutInflater, parent, false),
        )
    }

    // Binds data corresponding to the 'viewHolder' position selected
    override fun onBindViewHolder(viewHolder: CalendarEventsViewHolder, position: Int) {
        viewHolder.bind(events[position])
    }

    override fun getItemCount(): Int = events.size

    // Binds data from an 'Event' object to the 'ViewHolder' to be managed and displayed
    // and managed through 'RecyclerView' which includes a onClick handling
    inner class CalendarEventsViewHolder(private val binding: CalendarEventItemViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                onClick(events[bindingAdapterPosition])
            }
        }

        fun bind(event: Event) {
            // Format from FB event.
            binding.cardTitle.text = event.text
            binding.description.text = event.desc
            binding.time.text = event.time
            if (event.completed)
                binding.completed.text = "Complete"
            else
                binding.completed.text = "Not Complete"
        }
    }
}

class CalendarFragment : Fragment(R.layout.fragment_calendar) {

    //private val fsdb = FirestoreData()
    // Declaration of a mutable map, 'taskMap', which stores our 'Task' objects.
    // Keys are of type 'String' and values are of type 'Task'.
    private var taskMap = mutableMapOf<String, Task>()

    // Declaration of an eventsAdapter instance of the 'CalendarEventsAdapter' class.
    // Creates an alert dialog box event that can mark a task/event as complete,
    // close the alert dialog box, or delete the task/event depending on users
    // decision.
    private val eventsAdapter = CalendarEventsAdapter {
        AlertDialog.Builder(requireContext())
            .setMessage(R.string.calender_dialog_complete_task_confirmation)
            .setPositiveButton(R.string.complete) { _, _ ->
                // ISend an update to FB to complete the task.
                completeEvent(it)
            }
            .setNeutralButton(R.string.delete) { _, _ ->
                fsdb.deleteTask(it.id)
                deleteEvent(it)
            }
            .setNegativeButton(R.string.close, null)
            .show()
    }

    // *CURRENTLY NOT BEING USED*
    // Purpose: Create an alert dialog box which allows user to enter information
    // for an event which can then be saved, or the alert box closed.
    private val inputDialog by lazy {
        val editText = AppCompatEditText(requireContext())
        val layout = FrameLayout(requireContext()).apply {
            // Setting the padding on the EditText only pads the input area
            // not the entire EditText so we wrap it in a FrameLayout.
            val padding = dpToPx(20, requireContext())
            setPadding(padding, padding, padding, padding)
            addView(editText, FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ))
        }
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.calendar_input_dialog_title))
            .setView(layout)
            .setPositiveButton(R.string.save) { _, _ ->
                saveEvent(editText.text.toString())
                // Prepare EditText for reuse.
                editText.setText("")
            }
            .setNegativeButton(R.string.close, null)
            .create()
            .apply {
                setOnShowListener {
                    // Show the keyboard
                    editText.requestFocus()
                    context.inputMethodManager
                        .toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
                }
                setOnDismissListener {
                    // Hide the keyboard
                    context.inputMethodManager
                        .toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
                }
            }
    }

    // Variables for storing selected date, date of today, and for formatting date output
    // of events.
    private var selectedDate: LocalDate? = null
    private val today = LocalDate.now()

    private val selectionFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")
    private val events = mutableMapOf<LocalDate, List<Event>>()

    private lateinit var binding: FragmentCalendarBinding


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.title = "Calendar | Synapflow"

        // Sets status bar color, binds layout using data binding, configures LinearLayoutManager
        // for RecyclerView, sets adapter for RecyclerView, and DividerItemDecoration is added to
        // segment/divide between each vertical list of tasks.
        addStatusBarColorUpdate(R.color.calendar_statusbar_color)
        binding = FragmentCalendarBinding.bind(view)
        binding.calendarRv.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = eventsAdapter
            addItemDecoration(DividerItemDecoration(requireContext(), RecyclerView.VERTICAL))
        }

        // Declaration of values to store variables related to the functionality of
        // the calendar.
        val daysOfWeek = daysOfWeek()
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(50)
        val endMonth = currentMonth.plusMonths(50)

        // Configure/bind calendarView to have the proper start month, end month
        // and first days of the week. Immediately scroll to the current month.
        configureBinders(daysOfWeek)
        binding.calendarView.apply {
            setup(startMonth, endMonth, daysOfWeek.first())
            scrollToMonth(currentMonth)
        }


        if (savedInstanceState == null) {
            // Show today's events initially.
            binding.calendarView.post { selectDate(today) }
        }
        //binding.eventAddButton.setOnClickListener { inputDialog.show() }

        getTaskList()
        getTasksForCalender()
    }

    // Updates selected date and notifies calendarView to visually update
    //previous and current selected dates.
    private fun selectDate(date: LocalDate) {
        if (selectedDate != date) {
            val oldDate = selectedDate
            selectedDate = date
            oldDate?.let { binding.calendarView.notifyDateChanged(it) }
            binding.calendarView.notifyDateChanged(date)
            updateAdapterForDate(date)
        }
    }

    private fun getTaskList() {
        taskMap = fsdb.getTaskMap()
    }

    public fun getTasksForCalender() {
        /*
            Updates the task in Firestore to complete the task.
        */
        // Helper function to convert date.
        fun asLocalDate(date: Date): LocalDate {
            return Instant.ofEpochMilli(date.time).atZone(ZoneId.systemDefault()).toLocalDate()
        }
        clearEvents()
        // Create time parameter
        for ((key, task) in taskMap) {
            var date = asLocalDate(task.startTimeStamp.toDate())
            var intHrs = task.startTimeStamp.toDate().hours.toInt()
            var intMins = task.startTimeStamp.toDate().minutes.toInt()
            var strMins = task.startTimeStamp.toDate().minutes.toString()
            var strTime = ""

            //Check timestamps minutes to account for example: 1:9 PM
            //which should be 1:09 PM
            if (strMins.length < 2) {
                strMins = "0" + strMins
            }
            else {
                strMins
            }

            //Checks for converting military time format to
            //standard time format
            if (intHrs in 13..23) {
                strTime = (intHrs - 12).toString() + ":" + strMins + " PM"
            }
            else if (intHrs in 1..11) {
                strTime = intHrs.toString() + ":" + strMins + " AM"
            }
            else if (intHrs.equals(12) && intMins >= 0) {
                strTime = intHrs.toString() + ":" + strMins + " PM"
            }
            else if (intHrs.equals(0) && intMins >= 0) {
                strTime = (intHrs + 12).toString() + ":" + strMins + " AM"
            }

            events[date] =
                events[date].orEmpty().plus(Event(
                    key,
                    strTime,
                    task.name,
                    task.description,
                    date,
                    task.completed
                ))
            updateAdapterForDate(date)
        }
    }

    // DOESNT WORK FOR NOW
    private fun saveEvent(text: String) {
        // DOESNT WORK FOR NOW
        /*
        DOESNT WORK FOR NOW

         */
        if (text.isBlank()) {
            Toast.makeText(requireContext(), R.string.calendar_empty_input_text, Toast.LENGTH_LONG)
                .show()
        } else {
            selectedDate?.let {
                //events[it] =
                    //events[it].orEmpty().plus(Event(UUID.randomUUID().toString(), text, it))
                //updateAdapterForDate(it)
            }
        }
    }

    private fun clearEvents() {
        /*
        Clears events to redraw
         */
        for (eventList in events.values) {
            for (event in eventList){
                deleteEvent(event)
            }
        }
    }

    private fun completeEvent(event: Event) {
        // Tells Firestore to update the event.
        fsdb.markTaskCompleted(event.id)
        event.completed = true
        // Request the taskListUpdate.
        getTaskList()
    }
    private fun deleteEvent(event: Event) {
        val date = event.date
        events[date] = events[date].orEmpty().minus(event)
        updateAdapterForDate(date)
    }

    private fun updateAdapterForDate(date: LocalDate) {
        eventsAdapter.apply {
            events.clear()
            events.addAll(this@CalendarFragment.events[date].orEmpty())
            notifyDataSetChanged()
        }
        binding.currentDateText.text = selectionFormatter.format(date)
    }

    // sets up binding and appearance of days views in calendar.
    private fun configureBinders(daysOfWeek: List<DayOfWeek>) {
        // DayViewContainer class holds views and manages click event to update
        // selected date.
        class DayViewContainer(view: View) : ViewContainer(view) {
            lateinit var day: CalendarDay // Will be set when this container is bound.
            val binding = CalendarDayLayoutBinding.bind(view)

            init {
                view.setOnClickListener {
                    if (day.position == DayPosition.MonthDate) {
                        selectDate(day.date)
                    }
                }
            }
        }
        // MonthDayBinder binds data to the day views and manages appearance based on position
        // and whether it is today's date, selected date, else
        // Inbound and outbound dates not related to the current month in view are greyed out
        // in text coloration compared to being solid black for dates of current month.
        binding.calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            @SuppressLint("ResourceAsColor")
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.day = data
                val textView = container.binding.calendarDayText
                val dotView = container.binding.calendarDotView

                textView.text = data.date.dayOfMonth.toString()

                if (data.position == DayPosition.MonthDate) {
                    textView.makeVisible()
                    when (data.date) {
                        today -> {
                            textView.setTextColorRes(R.color.calendar_white)
                            textView.setBackgroundResource(R.drawable.calendar_today_bg)
                            dotView.makeInVisible()
                        }
                        selectedDate -> {
                            textView.setTextColorRes(R.color.calendar_blue)
                            textView.setBackgroundResource(R.drawable.calendar_selected_bg)
                            dotView.makeInVisible()
                        }
                        else -> {
                            textView.setTextColorRes(R.color.calendar_black)
                            textView.background = null
                            dotView.isVisible = events[data.date].orEmpty().isNotEmpty()
                        }
                    }
                } else {
                    //textView.makeInVisible()
                    textView.setTextColor(R.color.calendar_grey_light)
                    dotView.makeInVisible()
                }
            }
        }

        // Declaration of variables as named necessary for binding to our calendar view.
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(50)
        val endMonth = currentMonth.plusMonths(50)
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek

        // Defined variables are bound to calendar view as assigned.
        binding.calendarView.setup(startMonth, endMonth, firstDayOfWeek)
        binding.calendarView.scrollToMonth(currentMonth)

        // Declaration of titlesContainer views to display abbreviated names corresponding
        // to days of week.
        val titlesContainer = view?.findViewById<ViewGroup>(R.id.titlesContainer)
        if (titlesContainer != null) {
            titlesContainer.children
                .map { it as TextView }
                .forEachIndexed { index, textView ->
                    val dayOfWeek = daysOfWeek[index]
                    val title = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                    textView.text = title
                }
        }

        // Sets up month head binder for calendar view.
        class MonthViewContainer(view: View) : ViewContainer(view) {
            val titlesContainer = view as ViewGroup
        }
        binding.calendarView.monthHeaderBinder =
            object : MonthHeaderFooterBinder<MonthViewContainer> {
                override fun create(view: View) = MonthViewContainer(view)
                override fun bind(container: MonthViewContainer, data: CalendarMonth) {
                    // Setup each header day text if we have not done that already.
                    if (container.titlesContainer.tag == null) {
                        container.titlesContainer.tag = data.yearMonth
                        container.titlesContainer.children.map { it as TextView }
                            .forEachIndexed { index, tv ->
                                tv.text = daysOfWeek[index].name.first().toString()
                                tv.setTextColorRes(R.color.calendar_black)
                            }
                    }
                }
            }
    }
}