package com.example.projekt_aplikacje.ui.diet_plan

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.projekt_aplikacje.R
import com.example.projekt_aplikacje.database.Repository
import com.example.projekt_aplikacje.model.Day
import timber.log.Timber
import java.util.*

class WeekAdapter(
    private var context: Context,
    private var currentDays: List<Day>,
    private var daysName: List<String>,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val db = Repository

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        Timber.i("Creating...")
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.weekly_diet_item, parent, false)
        Timber.i("Created")
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentDay = currentDays[position]
        val currentDayName = daysName[position]
        val recipes = currentDay.recipesIds
        var protein = 0.0
        var fat = 0.0
        var carbs = 0.0
        var calories = 0.0
        holder.itemView.findViewById<ProgressBar>(R.id.protein).progress = 0
        holder.itemView.findViewById<ProgressBar>(R.id.fat).progress = 0
        holder.itemView.findViewById<ProgressBar>(R.id.carbs).progress = 0
        holder.itemView.findViewById<ProgressBar>(R.id.calories).progress = 0
        for (i in recipes) {
            db.getRecipe(i,
                { recipe ->
                    val nutrients = recipe?.nutrition
                    if (nutrients != null) {
                        for (j in nutrients.nutrients) {
                            when (j.title.toLowerCase(Locale.ROOT)) {
                                "calories" -> {
                                    calories += j.percentOfDailyNeeds
                                }
                                "fat" -> {
                                    fat += j.percentOfDailyNeeds
                                }
                                "protein" -> {
                                    protein += j.percentOfDailyNeeds
                                }
                                "carbohydrates" -> {
                                    carbs += j.percentOfDailyNeeds
                                }
                            }
                        }
                    }
                    holder.itemView.findViewById<ProgressBar>(R.id.protein).progress =
                        protein.toInt()
                    holder.itemView.findViewById<ProgressBar>(R.id.fat).progress = fat.toInt()
                    holder.itemView.findViewById<ProgressBar>(R.id.carbs).progress = carbs.toInt()
                    holder.itemView.findViewById<ProgressBar>(R.id.calories).progress =
                        calories.toInt()
                },
                {})
        }



        Timber.i("Binding, ${currentDay.id}")
        holder.itemView.findViewById<TextView>(R.id.name).text = currentDayName
        holder.itemView.setOnClickListener {
            val intent = Intent(context, DayDiet::class.java)
            intent.putExtra("Date", currentDay.id)
            intent.putExtra("Name", currentDayName)
            intent.putExtra("Recepies", currentDay.recipesIds as ArrayList<String>)

            startActivity(context, intent, null)
        }

    }

    override fun getItemCount(): Int {
        return currentDays.size
    }

}
