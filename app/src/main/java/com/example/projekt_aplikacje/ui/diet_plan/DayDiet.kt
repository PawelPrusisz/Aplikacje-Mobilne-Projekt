package com.example.projekt_aplikacje.ui.diet_plan

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.setMargins
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projekt_aplikacje.R
import com.example.projekt_aplikacje.database.Repository
import com.example.projekt_aplikacje.databinding.ActivityDayDietViewerBinding
import com.example.projekt_aplikacje.model.Day
import com.example.projekt_aplikacje.model.Recipe
import com.example.projekt_aplikacje.ui.browse.BrowseActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.skydoves.progressview.ProgressView
import com.skydoves.transformationlayout.TransformationLayout
import timber.log.Timber

class DayDiet : AppCompatActivity() {
    private lateinit var dayAdapter: DayAdapter
    private lateinit var binding: ActivityDayDietViewerBinding

    private val db = Repository
    private var dayId = ""
    private var recipeList: ArrayList<Recipe> = ArrayList()
    private var sthChanged = false
    private val progressBars = HashMap<String, ProgressView>()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDayDietViewerBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val date = intent.getStringExtra("Date")
        val dayName = intent.getStringExtra("Name")
        if (date != null) {
            dayId = date
        }
        val recipeId = intent.getStringArrayListExtra("Recepies")
        dayAdapter = DayAdapter(recipeList) { update() }
        findViewById<TextView>(R.id.item_day).text = "$dayName $date"
        val recipeRecycler = findViewById<RecyclerView>(R.id.recyclerView)

        recipeRecycler.layoutManager = LinearLayoutManager(this)
        recipeRecycler.adapter = dayAdapter
        setNutrients()


        if (recipeId != null) {
            if (recipeId.isNotEmpty()) {
                db.getManyRecipes(recipeId, { recipeList ->
                    this.recipeList = recipeList as ArrayList<Recipe>
                    setIngredients()
                    updateNutrients()

                    dayAdapter.setRecipes(this.recipeList)

                }, {})
            }
        }

        findViewById<FloatingActionButton>(R.id.addRecipeToDayButton).setOnClickListener {
            addFromFavorite()
        }

        findViewById<FloatingActionButton>(R.id.floatingActionButtonIngredients).setOnClickListener {
            findViewById<TransformationLayout>(R.id.transformationLayoutIngredientsDetails).startTransform()
        }

        findViewById<View>(R.id.ingredientsCardLayout).setOnClickListener {
            findViewById<TransformationLayout>(R.id.transformationLayoutIngredientsDetails).finishTransform()
        }

        findViewById<View>(R.id.ingredientsCardLayout2).setOnClickListener {
            findViewById<TransformationLayout>(R.id.transformationLayoutIngredientsDetails).finishTransform()
        }

        findViewById<TextView>(R.id.ingredientsCardTitle).text =
            resources.getText(R.string.ingredients)

        binding.ingredientsLayoutDayDesc.root.text =
            resources.getText(R.string.percent_of_daily_needs)

        val itemTouchHelper = ItemTouchHelper(DayDelete(dayAdapter))
        itemTouchHelper.attachToRecyclerView(findViewById(R.id.recyclerView))
    }

    private fun update() {
        sthChanged = true
        updateNutrients()
        setIngredients()
    }

    private fun calcNutrients(): HashMap<String, Double> {
        val map = HashMap<String, Double>()

        for (recipe in recipeList) {
            if (recipe.nutrition != null) {
                for (nutrient in recipe.nutrition.nutrients) {
                    if (nutrient.title in resources.getStringArray(R.array.nutrients).toList()) {
                        if (map.containsKey(nutrient.title)) {
                            map[nutrient.title] =
                                map[nutrient.title]!!.plus(nutrient.percentOfDailyNeeds)
                        } else {
                            map[nutrient.title] = nutrient.percentOfDailyNeeds
                        }
                    }
                }
            }
        }

        return map
    }

    private fun updateNutrients() {
        val map = calcNutrients()

        for (p in progressBars) {
            val v = if (map.containsKey(p.key)) {
                map[p.key]
            } else {
                0.0
            }

            progressBars[p.key]!!.labelText = "${p.key} ${v!!.toInt().toFloat()}%"
            progressBars[p.key]!!.progress = v.toInt().toFloat()
        }
    }

    private fun setNutrients() {

        val map = resources.getStringArray(R.array.nutrients).toList()

        for (n in map) {
            addProgressBar(n, 0.0)
        }
    }

    private fun setIngredients() {
        val layout = findViewById<LinearLayout>(R.id.ingredientsCardLayout)

        if (layout.childCount > 1) {
            Timber.d("Usuwam")
            layout.removeViews(1, layout.childCount - 1)
        }

        val map = HashMap<Pair<String, String>, Double>()

        for (recipe in recipeList) {
            if (recipe.nutrition != null) {
                for (n in recipe.nutrition.ingredients) {
                    val pair = Pair(n.name, n.unit)
                    if (map.containsKey(pair)) {
                        map[Pair(n.name, n.unit)] = map[Pair(n.name, n.unit)]!!.plus(n.amount)
                    } else {
                        map[Pair(n.name, n.unit)] = n.amount
                    }
                }
            }
        }

        for (p in map) {
            addIngredientEntry(layout, p.key.first, p.value.toString(), p.key.second)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun addIngredientEntry(
        layout: LinearLayout,
        name: String,
        amount: String,
        unit: String,
    ) {
        val entry = layoutInflater.inflate(R.layout.ingredient_entry, layout, false)

        entry.findViewById<TextView>(R.id.textViewAmountAndUnit).text = "$amount $unit"
        entry.findViewById<TextView>(R.id.textViewIngredientName).text = name
        layout.addView(entry)
    }

    private fun addFromFavorite() {
        val intent = Intent(
            this,
            BrowseActivity::class.java
        )
        intent.putExtra("browseMode", BrowseActivity.BrowseMode.FirebaseFavourites)
        intent.putExtra("actionMode", BrowseActivity.ActionMode.ReturnIntent)

        startActivityForResult(intent, 999)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 999 && resultCode == RESULT_OK && data != null) {
            val recipe = data.getSerializableExtra("recipe")
            if (recipe != null) {
                Timber.d(recipe.toString())

                if (recipe !in recipeList) {
                    dayAdapter.addDay(recipe as Recipe)
                    updateNutrients()
                }
            }
        }
    }


    override fun onPause() {
        super.onPause()

        if (sthChanged) {
            val recipeListId: ArrayList<String> = ArrayList()

            Timber.d(recipeList.toString())
            for (i in (recipeList)) {
                recipeListId.add(i.documentId)
            }

            val day = Day(dayId, recipeListId)
            db.addNewDayPlan(day, {}, {})
        }
    }


    private fun addProgressBar(
        name: String,
        value: Double,
        minValue: Int = 0,
        maxValue: Int = 100,
    ) {

        val progressBar = ProgressView(this).apply {
            progress = value.toFloat()
            labelText = "$name ${value}%"
            labelColorInner = ContextCompat.getColor(this@DayDiet, R.color.blue1)
            labelColorOuter = ContextCompat.getColor(this@DayDiet, R.color.white)
            min = minValue.toFloat()
            max = maxValue.toFloat()
            progressFromPrevious = true
            autoAnimate = true
            radius = 12 * resources.displayMetrics.density
            borderColor = ContextCompat.getColor(this@DayDiet, R.color.white)
            borderWidth = 2
            colorBackground = ContextCompat.getColor(this@DayDiet, R.color.blue4)
            highlightView.color = getColorProgressBar(name)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (25 * resources.displayMetrics.density).toInt()
            )
            params.setMargins((5 * resources.displayMetrics.density).toInt())
            params.gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(params)
        }

        findViewById<LinearLayout>(R.id.ingredientsLayoutDay).addView(progressBar)
        progressBars[name] = progressBar
    }

    private fun getColorProgressBar(name: String): Int {
        val coloName =
            name[0].toLowerCase() + name.subSequence(1, name.length).toString() + "_color"
        return ContextCompat.getColor(
            this, resources.getIdentifier(
                coloName,
                "color",
                packageName
            )
        )
    }

}