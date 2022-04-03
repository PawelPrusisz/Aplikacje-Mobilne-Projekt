package com.example.projekt_aplikacje.ui.diet_plan

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projekt_aplikacje.R
import com.example.projekt_aplikacje.database.Repository
import com.example.projekt_aplikacje.databinding.ActivityWeeklyDietViewerBinding
import com.example.projekt_aplikacje.model.Day
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class WeeklyDietViewer : AppCompatActivity() {

    private lateinit var binding: ActivityWeeklyDietViewerBinding
    private var offset: Int = 0
    private var today: Calendar = Calendar.getInstance()
    private val db = Repository
    private val dayNames =
        listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeeklyDietViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val tmpList = ArrayList<Day>()
        val dayList = ArrayList<String>()
        for (i in 0..6) {
            val theDay: Int = today.get(Calendar.DAY_OF_WEEK) - 1
            dayList.add(dayNames[theDay])
            val viewFormatter = SimpleDateFormat("dd-MM-y")
            val viewFormattedDate: String = viewFormatter.format(today.time)
            val tmp = Day(viewFormattedDate, ArrayList())
            tmpList.add(tmp)
            today.add(Calendar.DAY_OF_MONTH, 1)
        }
        val dayRecycler = binding.dayRecycler
        dayRecycler.layoutManager = LinearLayoutManager(this)
        dayRecycler.adapter = WeekAdapter(this, tmpList, dayList)


        binding.nextWeek.setOnClickListener { rightButton() }
        binding.lastWeek.setOnClickListener { leftButton() }

        refreshDate()
    }

    /*
        refreshing date display and the RecyclerView
     */
    @SuppressLint("SimpleDateFormat")
    private fun refreshDate() {
        val curWeek = binding.currentWeek
        val nextWeek = binding.nextWeek
        val lastWeek = binding.lastWeek

        today = Calendar.getInstance()

        today.add(Calendar.DAY_OF_MONTH, 7 * offset)
        val viewFormatter = SimpleDateFormat("dd-MM-y")
        var viewFormattedDate: String = viewFormatter.format(today.time)
        val spannableString = SpannableString(viewFormattedDate)
        spannableString.setSpan(UnderlineSpan(), 0, spannableString.length, 0)
        curWeek.text = spannableString

        val dayToGet: ArrayList<String> = ArrayList()
        val dayList: ArrayList<String> = ArrayList()

        for (i in 0..6) {
            val theDay: Int = today.get(Calendar.DAY_OF_WEEK) - 1
            dayList.add(dayNames[theDay])
            dayToGet.add(viewFormattedDate)
            today.add(Calendar.DAY_OF_MONTH, 1)
            viewFormattedDate = viewFormatter.format(today.time)
        }

        viewFormattedDate = viewFormatter.format(today.time)
        nextWeek.text = viewFormattedDate

        today.add(Calendar.DAY_OF_MONTH, -14)
        viewFormattedDate = viewFormatter.format(today.time)
        lastWeek.text = viewFormattedDate


        updateRecycler(dayToGet, dayList)
    }

    /**
    last week
     */
    fun leftButton() {
        offset -= 1
        refreshDate()

    }

    /**
    next week
     */
    fun rightButton() {
        offset += 1
        refreshDate()
    }

    override fun onResume() {
        super.onResume()
        refreshDate()
    }

    private fun updateRecycler(days: ArrayList<String>, dayNames: List<String>) {
        Timber.i("Started loading the RecyclerView item")
        Timber.i(days.toString())
        db.getManyPlansOnDay(days,
            { currentDays ->
                var dayList = ArrayList<Day>()

                if (currentDays.size != days.size) {
                    for (i in days) {
                        var ok = 0
                        for (j in currentDays) {
                            if (i == j.id) {
                                ok = 1
                                dayList.add(j)
                            }
                        }
                        if (ok == 0) {
                            val newDay = Day(
                                i,
                                ArrayList()
                            )
                            dayList.add(newDay)
                        }
                    }
                } else {
                    dayList = currentDays as ArrayList<Day>
                }
                val dayRecycler = findViewById<RecyclerView>(R.id.day_recycler)
                dayRecycler?.adapter = WeekAdapter(this, dayList, dayNames)
            },

            { exception ->
                Timber.i("Failed to load data from Database, $exception")
            }
        )
    }
}

