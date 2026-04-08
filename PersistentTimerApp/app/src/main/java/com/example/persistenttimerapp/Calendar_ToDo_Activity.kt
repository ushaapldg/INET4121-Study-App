package com.example.persistenttimerapp

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.GestureDetector
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import com.example.persistenttimerapp.databinding.ActivityMainBinding
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.nextMonth
import com.kizitonwose.calendar.core.previousMonth
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.*

/**
 * Main Activity that manages the Calendar View and To-Do List.
 */
class Calendar_ToDo_Activity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    // Calendar Variables
    private var selectedDate: LocalDate? = LocalDate.now()
    private val today = LocalDate.now()

    // To-Do Variables
    private val categories = mutableListOf<Category>()

    data class Category(val name: String, val color: Int)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupCalendar()
        updateDateDisplay()

        // Navigation to Timer Activity
        binding.timerButton.setOnClickListener {
            val intent = Intent(this, TimerActivity::class.java)
            startActivity(intent)
        }

        binding.fabAdd.setOnClickListener {
            showAddCategoryPopup()
        }
    }

    // --- To-Do List Logic ---

    private fun updateDateDisplay() {
        val sdf = SimpleDateFormat("EEEE M/d", Locale.getDefault())
        val date = Date.from(selectedDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant() ?: Instant.now())
        binding.dateText.text = sdf.format(date)
    }

    private fun refreshUIForNewDay() {
        updateDateDisplay()
        binding.categoryContainer.removeAllViews()
        categories.forEach { category ->
            addCategoryViews(category.name, category.color)
        }
    }

    private fun showAddCategoryPopup() {
        val input = EditText(this)
        input.hint = getString(R.string.category_name)
        
        val container = LinearLayout(this)
        container.orientation = LinearLayout.VERTICAL
        container.setPadding(48, 16, 48, 16)
        container.addView(input)

        val colorTitle = TextView(this)
        colorTitle.text = getString(R.string.pick_a_color)
        colorTitle.setPadding(0, 32, 0, 8)
        container.addView(colorTitle)

        val colorLayout = LinearLayout(this)
        colorLayout.orientation = LinearLayout.HORIZONTAL
        colorLayout.gravity = Gravity.CENTER
        
        val colors = listOf("#E57373", "#81C784", "#64B5F6", "#FFF176", "#FFB74D", "#BA68C8", "#4DB6AC")
        var selectedColor = colors[0].toColorInt()
        val colorViews = mutableListOf<View>()

        fun updateColorSelection(selectedView: View, colorInt: Int) {
            selectedColor = colorInt
            colorViews.forEachIndexed { index, view ->
                val drawable = GradientDrawable()
                drawable.setColor(colors[index].toColorInt())
                if (view == selectedView) {
                    drawable.setStroke(6, Color.BLACK)
                }
                view.background = drawable
            }
        }

        colors.forEach { colorHex ->
            val colorView = View(this)
            val colorInt = colorHex.toColorInt()
            val p = LinearLayout.LayoutParams(80, 80)
            p.setMargins(8, 8, 8, 8)
            colorView.layoutParams = p
            colorView.setOnClickListener { updateColorSelection(colorView, colorInt) }
            colorViews.add(colorView)
            colorLayout.addView(colorView)
        }
        updateColorSelection(colorViews[0], colors[0].toColorInt())
        container.addView(colorLayout)

        AlertDialog.Builder(this)
            .setTitle(R.string.add_new_category)
            .setView(container)
            .setPositiveButton(R.string.add) { _, _ ->
                val name = input.text.toString()
                if (name.isNotEmpty()) {
                    val category = Category(name, selectedColor)
                    categories.add(category)
                    addCategoryViews(name, selectedColor)
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun addCategoryViews(name: String, color: Int) {
        val categoryTextView = TextView(this)
        categoryTextView.text = name
        categoryTextView.textSize = 24f
        categoryTextView.setTypeface(null, Typeface.BOLD)
        categoryTextView.setTextColor(color)
        
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.setMargins(0, 32, 0, 8)
        categoryTextView.layoutParams = params
        binding.categoryContainer.addView(categoryTextView)

        addAddTaskTrigger(binding.categoryContainer, color)
    }

    private fun addTaskRow(container: LinearLayout, color: Int, index: Int = -1): EditText {
        val taskLayout = LinearLayout(this)
        taskLayout.orientation = LinearLayout.HORIZONTAL
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.setMargins(48, 4, 0, 4)
        taskLayout.layoutParams = params

        val checkBox = CheckBox(this)
        checkBox.buttonTintList = ColorStateList.valueOf(color)
        
        val taskEditText = EditText(this)
        taskEditText.setText("")
        taskEditText.textSize = 18f
        taskEditText.setTextColor(color)
        taskEditText.setPadding(16, 0, 16, 0)
        taskEditText.background = null
        taskEditText.isFocusable = false
        taskEditText.isFocusableInTouchMode = false
        taskEditText.imeOptions = EditorInfo.IME_ACTION_DONE
        taskEditText.setSingleLine(true)

        checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                taskEditText.paintFlags = taskEditText.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                taskLayout.alpha = 0.7f
            } else {
                taskEditText.paintFlags = taskEditText.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                taskLayout.alpha = 1.0f
            }
        }
        
        val gd = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                enableEditing(taskEditText)
                return true
            }
        })
        taskEditText.setOnTouchListener { v, event -> 
            val handled = gd.onTouchEvent(event)
            if (!handled && event.action == MotionEvent.ACTION_UP) {
                v.performClick()
            }
            !taskEditText.isFocusable 
        }
        
        taskEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                disableEditing(taskEditText)
                val currentIndex = container.indexOfChild(taskLayout)
                val nextEditText = addTaskRow(container, color, currentIndex + 1)
                enableEditing(nextEditText)
                true
            } else {
                false
            }
        }

        var backspacePressedOnce = false
        taskEditText.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                if (taskEditText.text.isEmpty()) {
                    if (backspacePressedOnce) {
                        container.removeView(taskLayout)
                        true
                    } else {
                        backspacePressedOnce = true
                        false
                    }
                } else {
                    false
                }
            } else {
                backspacePressedOnce = false
                false
            }
        }

        taskEditText.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus -> if (!hasFocus) disableEditing(taskEditText) }

        taskLayout.addView(checkBox)
        taskLayout.addView(taskEditText)
        if (index == -1) container.addView(taskLayout) else container.addView(taskLayout, index)
        return taskEditText
    }

    private fun addAddTaskTrigger(container: LinearLayout, color: Int) {
        val trigger = TextView(this)
        trigger.text = getString(R.string.double_tap_to_add_task)
        trigger.textSize = 14f
        trigger.setTextColor(color)
        trigger.alpha = 0.5f
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.setMargins(64, 8, 0, 32)
        trigger.layoutParams = params
        
        val gd = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                val index = container.indexOfChild(trigger)
                container.removeView(trigger)
                val et = addTaskRow(container, color, index)
                enableEditing(et)
                addAddTaskTrigger(container, color)
                return true
            }
        })
        trigger.setOnTouchListener { v, event -> 
            val handled = gd.onTouchEvent(event)
            if (!handled && event.action == MotionEvent.ACTION_UP) {
                v.performClick()
            }
            true 
        }
        container.addView(trigger)
    }

    private fun enableEditing(editText: EditText) {
        editText.isFocusable = true
        editText.isFocusableInTouchMode = true
        editText.requestFocus()
        editText.setSelection(editText.text.length)
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun disableEditing(editText: EditText) {
        editText.isFocusable = false
        editText.isFocusableInTouchMode = false
        editText.clearFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)
    }

    // --- Calendar Logic ---

    /**
     * Configures the CalendarView, sets up day binding (rendering and selection), and navigation listeners.
     */
    private fun setupCalendar() {
        // Binds data and logic to each day cell in the calendar
        binding.calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)

            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.textView.text = data.date.dayOfMonth.toString()

                // Selection logic when a day is clicked
                container.view.setOnClickListener {
                    if (data.position == DayPosition.MonthDate) {
                        val currentSelection = selectedDate
                        selectedDate = if (currentSelection == data.date) null else data.date
                        binding.calendarView.notifyDateChanged(data.date)
                        currentSelection?.let { binding.calendarView.notifyDateChanged(it) }
                        refreshUIForNewDay()
                    }
                }

                // Styling for month dates (Today, Selected, and Regular)
                if (data.position == DayPosition.MonthDate) {
                    container.textView.visibility = View.VISIBLE
                    container.textView.setTypeface(null, Typeface.NORMAL)
                    container.textView.background = null

                    when (data.date) {
                        selectedDate -> {
                            container.textView.setTextColor(Color.WHITE)
                            container.textView.setBackgroundColor(Color.rgb(103, 80, 164))
                        }
                        today -> {
                            container.textView.setTextColor(Color.rgb(103, 80, 164))
                            container.textView.setTypeface(null, Typeface.BOLD)
                        }
                        else -> {
                            container.textView.setTextColor(Color.BLACK)
                        }
                    }
                } else {
                    // Gray out dates from adjacent months
                    container.textView.setTextColor(Color.LTGRAY)
                }
            }
        }

        // Updates the Month/Year title when the calendar is scrolled
        binding.calendarView.monthScrollListener = { month ->
            val title = "${month.yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${month.yearMonth.year}"
            binding.monthYearText.text = title
        }

        // Navigation for Next and Previous month buttons
        binding.nextMonthButton.setOnClickListener {
            binding.calendarView.findFirstVisibleMonth()?.let {
                binding.calendarView.scrollToMonth(it.yearMonth.nextMonth)
            }
        }

        binding.previousMonthButton.setOnClickListener {
            binding.calendarView.findFirstVisibleMonth()?.let {
                binding.calendarView.scrollToMonth(it.yearMonth.previousMonth)
            }
        }

        // Initial setup of the calendar range and starting month
        val currentMonth = YearMonth.now()
        binding.calendarView.setup(currentMonth.minusMonths(100), currentMonth.plusMonths(100), daysOfWeek().first())
        binding.calendarView.scrollToMonth(currentMonth)
    }

    /**
     * View holder for a single calendar day cell.
     */
    class DayViewContainer(view: View) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.calendarDayText)
    }
}
