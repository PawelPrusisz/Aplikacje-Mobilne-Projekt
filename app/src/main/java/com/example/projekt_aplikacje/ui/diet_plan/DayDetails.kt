package com.example.projekt_aplikacje.ui.diet_plan

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.setMargins
import androidx.fragment.app.FragmentContainerView
import com.example.projekt_aplikacje.R
import com.example.projekt_aplikacje.model.Recipe
import com.example.projekt_aplikacje.ui.browse.RecipeInfo
import com.skydoves.progressview.ProgressView
import com.squareup.picasso.Picasso

class DayDetails : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diet_details)

        val recipe = intent.getSerializableExtra("Recipe") as Recipe

        findViewById<TextView>(R.id.textRecipeTitleDetail).text = recipe.title
        Picasso.get().load(recipe.image).resize(636, 393).placeholder(R.drawable.dish_placeholder)
            .into(findViewById<ImageView>(R.id.imageViewRecipe))

        if (recipe.nutrition != null) {
            findViewById<TextView>(R.id.progressBarDescDetail).text =
                resources.getText(R.string.percent_of_daily_needs)
            for (nutrient in recipe.nutrition.nutrients) {
                if (nutrient.title in resources.getStringArray(R.array.nutrients).toList())
                    addProgressBar(
                        nutrient.title,
                        nutrient.percentOfDailyNeeds
                    )
            }
        } else {
            findViewById<TextView>(R.id.progressBarDescDetail).visibility = View.GONE
        }

        addRatingBar(recipe)
        val fragment = supportFragmentManager.findFragmentById(R.id.recipeInfoFragment) as RecipeInfo
        fragment.setRecipe(recipe)
        fragment.hideTitle()

        addAttributes(recipe)
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
            labelColorInner = ContextCompat.getColor(this@DayDetails, R.color.blue1)
            labelColorOuter = ContextCompat.getColor(this@DayDetails, R.color.white)
            min = minValue.toFloat()
            max = maxValue.toFloat()
            autoAnimate = true
            radius = 12 * resources.displayMetrics.density
            borderColor = ContextCompat.getColor(this@DayDetails, R.color.white)
            borderWidth = 2
            colorBackground = ContextCompat.getColor(this@DayDetails, R.color.blue4)
            highlightView.color = getColorProgressBar(name)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (25 * resources.displayMetrics.density).toInt()
            )
            params.setMargins((5 * resources.displayMetrics.density).toInt())
            params.gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(params)

        }

        findViewById<LinearLayout>(R.id.progressBarsDetailLayout).addView(progressBar)
    }

    private fun addRatingBar(recipe: Recipe) {
        val ratingBarDesc = findViewById<TextView>(R.id.ratingBarDescDetails)
        val ratingBar = findViewById<RatingBar>(R.id.ratingBarDetails)

        ratingBarDesc.text = resources.getText(R.string.health_score)

        if (recipe.healthScore != null) {
            ratingBar.rating = (recipe.healthScore / 20.0).toFloat()
            ratingBar.visibility = View.VISIBLE
            ratingBarDesc.visibility = View.VISIBLE
        } else {
            ratingBar.visibility = View.GONE
            ratingBarDesc.visibility = View.GONE
        }
    }

    private fun addAttributes(recipe: Recipe) {
        if (recipe.cheap) addAttribute(R.drawable.cheap_icon)
        if (recipe.glutenFree) addAttribute(R.drawable.gluten_icon)
        if (recipe.ketogenic) addAttribute(R.drawable.keto_icon)
        if (recipe.vegan) addAttribute(R.drawable.vegan_icon)
        if (recipe.vegetarian && !recipe.vegan) addAttribute(R.drawable.vegetarian_icon)
        if (recipe.veryPopular) addAttribute(R.drawable.popular_icon)
        if (recipe.veryHealthy) addAttribute(R.drawable.healthy_icon)
    }

    private fun addAttribute(pic: Int) {
        val imageView = ImageView(this)
        imageView.setImageResource(pic)
        imageView.adjustViewBounds = true

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        params.gravity = Gravity.END
        params.setMargins((3 * resources.displayMetrics.density).toInt(), 0, 0, 0)
        imageView.layoutParams = params
        findViewById<LinearLayout>(R.id.attributesLayoutDetails).addView(imageView)
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
