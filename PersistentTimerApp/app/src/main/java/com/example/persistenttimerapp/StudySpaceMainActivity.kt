package com.example.persistenttimerapp

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
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.lifecycle.lifecycleScope
import com.example.persistenttimerapp.data.AppDatabase
import com.example.persistenttimerapp.data.StudyViewModel
import com.example.persistenttimerapp.data.StudyViewModelFactory
import com.example.persistenttimerapp.data.entities.Category
import com.example.persistenttimerapp.data.entities.Task
import com.example.persistenttimerapp.databinding.ActivityMainBinding
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.nextMonth
import com.kizitonwose.calendar.core.previousMonth
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.*

/**
 * Main Activity that manages the Calendar View and To-Do List with Room Database Persistence and ViewModel.
 */
class StudySpaceMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    
    private val viewModel: StudyViewModel by viewModels {
        StudyViewModelFactory(AppDatabase.getDatabase(this).appDao())
    }

    // Calendar Variables
    private var selectedDate: LocalDate? = LocalDate.now()
    private val today = LocalDate.now()

    // Cache for calendar dots to avoid frequent DB hits during bind
    private var allTasks = listOf<Task>()
    private var categoryColorMap = mapOf<Int, Int>()

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

        // Start observing data changes
        observeData()
    }

    private fun observeData() {
        // Observe all tasks to update calendar dots
        lifecycleScope.launch {
            viewModel.allTasks.collectLatest { tasks ->
                allTasks = tasks
                binding.calendarView.notifyCalendarChanged()
            }
        }

        // Observe categories to rebuild the To-Do list
        lifecycleScope.launch {
            viewModel.allCategories.collectLatest { categories ->
                categoryColorMap = categories.associate { it.id to it.color }
                refreshUIWithData(categories)
                binding.calendarView.notifyCalendarChanged()
            }
        }
    }

    private fun updateDateDisplay() {
        val sdf = SimpleDateFormat("EEEE M/d", Locale.getDefault())
        val date = Date.from(selectedDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant() ?: Instant.now())
        binding.dateText.text = sdf.format(date)
    }

    private fun refreshUIWithData(categories: List<Category>) {
        updateDateDisplay()
        binding.categoryContainer.removeAllViews()
        categories.forEach { category ->
            addCategoryViews(category)
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
                    viewModel.insertCategory(name, selectedColor)
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun addCategoryViews(category: Category) {
        val categoryWrapper = LinearLayout(this)
        categoryWrapper.orientation = LinearLayout.VERTICAL
        categoryWrapper.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        binding.categoryContainer.addView(categoryWrapper)

        val categoryTextView = TextView(this)
        categoryTextView.text = category.name
        categoryTextView.textSize = 24f
        categoryTextView.setTypeface(null, Typeface.BOLD)
        categoryTextView.setTextColor(category.color)
        
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.setMargins(0, 32, 0, 8)
        categoryTextView.layoutParams = params
        categoryWrapper.addView(categoryTextView)

        // DELETE CATEGORY: Long click to show deletion dialog
        categoryTextView.setOnLongClickListener {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.delete_category_title))
                .setMessage(getString(R.string.delete_category_message, category.name))
                .setPositiveButton(R.string.delete) { _, _ ->
                    viewModel.deleteCategory(category)
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
            true
        }

        // Load tasks for this category and date
        val dateLong = (selectedDate ?: today).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        lifecycleScope.launch {
            viewModel.getTasksByCategoryAndDate(category.id, dateLong).collectLatest { tasks ->
                // Refresh logic: Keep the title (index 0), remove rest
                val childCount = categoryWrapper.childCount
                if (childCount > 1) {
                    categoryWrapper.removeViews(1, childCount - 1)
                }
                
                tasks.forEach { task ->
                    addTaskRow(categoryWrapper, category.color, category.id, existingTask = task)
                }
                addAddTaskTrigger(categoryWrapper, category.color, category.id)
            }
        }
    }

    private fun addTaskRow(container: LinearLayout, color: Int, categoryId: Int, existingTask: Task? = null, index: Int = -1): EditText {
        val taskLayout = LinearLayout(this)
        taskLayout.orientation = LinearLayout.HORIZONTAL
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.setMargins(48, 4, 0, 4)
        taskLayout.layoutParams = params

        val checkBox = CheckBox(this)
        checkBox.buttonTintList = ColorStateList.valueOf(color)
        
        val taskEditText = EditText(this)
        taskEditText.textSize = 18f
        taskEditText.setTextColor(color)
        taskEditText.setPadding(16, 0, 16, 0)
        taskEditText.background = null
        taskEditText.isFocusable = false
        taskEditText.isFocusableInTouchMode = false
        taskEditText.imeOptions = EditorInfo.IME_ACTION_DONE
        taskEditText.isSingleLine = true

        val targetDate = selectedDate ?: today
        val dateLong = targetDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        taskEditText.setText(existingTask?.taskName ?: "")
        
        taskEditText.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                disableEditing(taskEditText)
                val newName = taskEditText.text.toString()
                if (newName.isNotEmpty()) {
                    if (existingTask == null) {
                        viewModel.insertTask(categoryId, newName, dateLong)
                    } else {
                        viewModel.updateTask(existingTask.copy(taskName = newName))
                    }
                }
            }
        }

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
                true
            } else {
                false
            }
        }

        taskEditText.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN && taskEditText.text.isEmpty()) {
                existingTask?.let { task ->
                    viewModel.deleteTask(task)
                }
                container.removeView(taskLayout)
                true
            } else false
        }

        taskLayout.addView(checkBox)
        taskLayout.addView(taskEditText)
        if (index == -1) container.addView(taskLayout) else container.addView(taskLayout, index)
        return taskEditText
    }

    private fun addAddTaskTrigger(container: LinearLayout, color: Int, categoryId: Int) {
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
                val et = addTaskRow(container, color, categoryId, index = index)
                enableEditing(et)
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
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun disableEditing(editText: EditText) {
        editText.isFocusable = false
        editText.isFocusableInTouchMode = false
        editText.clearFocus()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)
    }

    // --- Calendar Logic ---

    private fun setupCalendar() {
        binding.calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)

            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.textView.text = data.date.dayOfMonth.toString()

                container.view.setOnClickListener {
                    if (data.position == DayPosition.MonthDate) {
                        val currentSelection = selectedDate
                        selectedDate = if (currentSelection == data.date) null else data.date
                        binding.calendarView.notifyDateChanged(data.date)
                        currentSelection?.let { date -> binding.calendarView.notifyDateChanged(date) }
                        
                        // Force UI refresh for the new selected date
                        lifecycleScope.launch {
                           viewModel.allCategories.collectLatest { categories ->
                               refreshUIWithData(categories)
                           }
                        }
                    }
                }

                if (data.position == DayPosition.MonthDate) {
                    container.textView.visibility = View.VISIBLE
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
                            container.textView.setTypeface(null, Typeface.NORMAL)
                        }
                    }

                    container.dotContainer.removeAllViews()
                    val dayLong = data.date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    val dayTasks = allTasks.filter { it.dateCompleted == dayLong }
                    
                    if (dayTasks.isNotEmpty()) {
                        dayTasks.take(4).forEach { task ->
                            val color = categoryColorMap[task.categoryId] ?: Color.GRAY
                            container.dotContainer.addView(createDotView(color))
                        }
                        if (dayTasks.size > 4) container.dotContainer.addView(createPlusSignView())
                    }
                } else {
                    container.textView.setTextColor(Color.LTGRAY)
                    container.dotContainer.removeAllViews()
                }
            }
        }

        binding.calendarView.monthScrollListener = { month ->
            val title = "${month.yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${month.yearMonth.year}"
            binding.monthYearText.text = title
        }

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

        val currentMonth = YearMonth.now()
        binding.calendarView.setup(currentMonth.minusMonths(100), currentMonth.plusMonths(100), daysOfWeek().first())
        binding.calendarView.scrollToMonth(currentMonth)
    }

    private fun createDotView(color: Int): View {
        val dot = View(this)
        val density = resources.displayMetrics.density
        val size = (6 * density).toInt()
        val params = LinearLayout.LayoutParams(size, size)
        val margin = (2 * density).toInt()
        params.setMargins(margin, 0, margin, 0)
        dot.layoutParams = params
        val drawable = GradientDrawable()
        drawable.shape = GradientDrawable.OVAL
        drawable.setColor(color)
        dot.background = drawable
        return dot
    }

    private fun createPlusSignView(): TextView {
        val plus = TextView(this)
        plus.text = "+"
        plus.textSize = 10f
        plus.setTextColor(Color.GRAY)
        plus.setTypeface(null, Typeface.BOLD)
        plus.includeFontPadding = false
        val density = resources.displayMetrics.density
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.gravity = Gravity.CENTER_VERTICAL
        params.setMargins((2 * density).toInt(), 0, 0, 0)
        plus.layoutParams = params
        return plus
    }

    class DayViewContainer(view: View) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.calendarDayText)
        val dotContainer: LinearLayout = view.findViewById(R.id.dotContainer)
    }
}
