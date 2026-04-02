package com.example.todo_list

import android.content.Context
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
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var categoryContainer: LinearLayout
    private lateinit var dateText: TextView
    private val categories = mutableListOf<Category>()
    private var calendar = Calendar.getInstance()

    data class Category(val name: String, val color: Int)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        
        val mainView = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dateText = findViewById(R.id.date_text)
        categoryContainer = findViewById(R.id.category_container)
        
        updateDateDisplay()

        val fabAdd: FloatingActionButton = findViewById(R.id.fab_add)
        fabAdd.setOnClickListener {
            showAddCategoryPopup()
        }

        setupSwipeDetection(mainView)
    }

    private fun updateDateDisplay() {
        val sdf = SimpleDateFormat("EEEE M/d", Locale.getDefault())
        dateText.text = sdf.format(calendar.time)
    }

    private fun setupSwipeDetection(view: View) {
        val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            private val SWIPE_THRESHOLD = 100
            private val SWIPE_VELOCITY_THRESHOLD = 100

            override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                if (e1 == null) return false
                val diffX = e2.x - e1.x
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        // Swiped Right -> Next Day
                        nextDay()
                    } else {
                        // Swiped Left -> Previous Day
                        previousDay()
                    }
                    return true
                }
                return false
            }
        })

        view.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
    }

    private fun nextDay() {
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        refreshUIForNewDay()
    }

    private fun previousDay() {
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        refreshUIForNewDay()
    }

    private fun refreshUIForNewDay() {
        updateDateDisplay()
        categoryContainer.removeAllViews()
        categories.forEach { category ->
            addCategoryViews(category.name, category.color)
        }
    }

    private fun showAddCategoryPopup() {
        val input = EditText(this)
        input.hint = "Category Name"
        
        val container = LinearLayout(this)
        container.orientation = LinearLayout.VERTICAL
        container.setPadding(48, 16, 48, 16)
        container.addView(input)

        val colorTitle = TextView(this)
        colorTitle.text = "Pick a color:"
        colorTitle.setPadding(0, 32, 0, 8)
        container.addView(colorTitle)

        val colorLayout = LinearLayout(this)
        colorLayout.orientation = LinearLayout.HORIZONTAL
        colorLayout.gravity = Gravity.CENTER
        
        val colors = listOf("#E57373", "#81C784", "#64B5F6", "#FFF176", "#FFB74D", "#BA68C8", "#4DB6AC")
        var selectedColor = Color.parseColor(colors[0])
        val colorViews = mutableListOf<View>()

        fun updateColorSelection(selectedView: View, colorInt: Int) {
            selectedColor = colorInt
            colorViews.forEachIndexed { index, view ->
                val drawable = GradientDrawable()
                drawable.setColor(Color.parseColor(colors[index]))
                if (view == selectedView) {
                    drawable.setStroke(6, Color.BLACK)
                }
                view.background = drawable
            }
        }

        colors.forEach { colorHex ->
            val colorView = View(this)
            val colorInt = Color.parseColor(colorHex)
            val p = LinearLayout.LayoutParams(80, 80)
            p.setMargins(8, 8, 8, 8)
            colorView.layoutParams = p
            colorView.setOnClickListener { updateColorSelection(colorView, colorInt) }
            colorViews.add(colorView)
            colorLayout.addView(colorView)
        }
        updateColorSelection(colorViews[0], Color.parseColor(colors[0]))
        container.addView(colorLayout)

        AlertDialog.Builder(this)
            .setTitle("Add New Category")
            .setView(container)
            .setPositiveButton("Add") { _, _ ->
                val name = input.text.toString()
                if (name.isNotEmpty()) {
                    val category = Category(name, selectedColor)
                    categories.add(category)
                    addCategoryViews(name, selectedColor)
                }
            }
            .setNegativeButton("Cancel", null)
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
        categoryContainer.addView(categoryTextView)

        addAddTaskTrigger(categoryContainer, color)
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
        taskEditText.setImeOptions(EditorInfo.IME_ACTION_DONE)
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
        taskEditText.setOnTouchListener { _, event -> gd.onTouchEvent(event); !taskEditText.isFocusable }
        
        taskEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                disableEditing(taskEditText)
                // Automatically add a new task row below
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

        taskEditText.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus) disableEditing(taskEditText) }

        taskLayout.addView(checkBox)
        taskLayout.addView(taskEditText)
        if (index == -1) container.addView(taskLayout) else container.addView(taskLayout, index)
        return taskEditText
    }

    private fun addAddTaskTrigger(container: LinearLayout, color: Int) {
        val trigger = TextView(this)
        trigger.text = "Double tap to add task..."
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
        trigger.setOnTouchListener { _, event -> gd.onTouchEvent(event); true }
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
}
