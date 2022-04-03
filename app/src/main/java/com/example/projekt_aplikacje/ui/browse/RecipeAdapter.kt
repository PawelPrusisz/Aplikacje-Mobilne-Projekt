package com.example.projekt_aplikacje.ui.browse

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.setMargins
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.projekt_aplikacje.R
import com.example.projekt_aplikacje.model.Recipe
import com.skydoves.progressview.ProgressView
import com.skydoves.transformationlayout.TransformationLayout
import com.squareup.picasso.Picasso
import timber.log.Timber


class RecipeAdapter(private val nutrientsToShow: List<String>, private val onClick : (Recipe, TransformationLayout) -> Unit) :
    ListAdapter<Recipe, RecipeAdapter.RecipeViewHolder>(RecipeComparator()) {


    class RecipeViewHolder(private val view: View, private val nutrientsToShow: List<String>, private val onClick : (Recipe, TransformationLayout) -> Unit) :
        RecyclerView.ViewHolder(view) {

        private val textView: TextView = view.findViewById(R.id.textRecipeTitle)
        private val imageView: ImageView = view.findViewById(R.id.imageViewRecipe)
        private val attributesDesk: LinearLayout = view.findViewById(R.id.attributesLayout)
        private var ratingBarDesc: TextView = view.findViewById(R.id.ratingBarDesc)
        private var ratingBar: RatingBar = view.findViewById(R.id.ratingBar)
        private var progressBarsDesc: TextView = view.findViewById(R.id.progressBarDesc)
        private val progressBars = HashMap<String, ProgressView>() // nazwa progressbara
        private val attributes = HashMap<Int, ImageView>() // id obrazka


        // do tłumaczenia piksli na dp
        private val factor: Float = view.context.resources.displayMetrics.density

        init {
            progressBarsDesc.text = view.resources.getText(R.string.percent_of_daily_needs)
            ratingBarDesc.text = view.resources.getText(R.string.health_score)
        }

        fun bind(recipe: Recipe) {
            Timber.d("Binding: %s", recipe.toString())

            view.findViewById<Button>(R.id.buttonRecipe).setOnClickListener {
                onClick(recipe, view.findViewById(R.id.transformationLayout))}
            Timber.d("TU BYŁ : ${textView.text} teraz będize ${recipe.title}")
            textView.text = recipe.title
            Picasso.get().load(recipe.image).resize(636, 393).placeholder(R.drawable.dish_placeholder).into(imageView)

            if (recipe.servings > 1) {
                view.findViewById<TextView>(R.id.textViewServings).text = view.resources.getString(
                    R.string.servings,
                    recipe.servings.toString()
                )
            } else {
                view.findViewById<TextView>(R.id.textViewServings).text = view.resources.getString(
                    R.string.serving,
                    recipe.servings.toString()
                )
            }
            view.findViewById<TextView>(R.id.textViewTime).text = view.resources.getString(
                R.string.readyIn,
                recipe.readyInMinutes.toString()
            )

            if (recipe.healthScore != null) {
                addRatingBar(recipe.healthScore)
            } else {
                hideRatingBar()
            }

            for (i in progressBars.keys) {
                hideProgressBar(i)
            }

            if (recipe.nutrition != null) {
                progressBarsDesc.visibility = View.VISIBLE

                for (nutrient in recipe.nutrition.nutrients) {
                    if (nutrient.title in nutrientsToShow)
                        addProgressBar(
                            nutrient.title,
                            nutrient.percentOfDailyNeeds
                        )
                }
            } else {
                progressBarsDesc.visibility = View.GONE
            }

            addAttributes(recipe)
        }

        private fun addAttributes(recipe: Recipe) {
            if (recipe.cheap) addAttribute(R.drawable.cheap_icon) else hideAttribute(R.drawable.cheap_icon)
            if (recipe.glutenFree) addAttribute(R.drawable.gluten_icon) else hideAttribute(R.drawable.gluten_icon)
            if (recipe.ketogenic) addAttribute(R.drawable.keto_icon) else hideAttribute(R.drawable.keto_icon)
            if (recipe.vegan) addAttribute(R.drawable.vegan_icon) else hideAttribute(R.drawable.vegan_icon)
            if (recipe.vegetarian && !recipe.vegan) addAttribute(R.drawable.vegetarian_icon) else hideAttribute(
                R.drawable.vegetarian_icon
            )
            if (recipe.veryPopular) addAttribute(R.drawable.popular_icon) else hideAttribute(R.drawable.popular_icon)
            if (recipe.veryHealthy) addAttribute(R.drawable.healthy_icon) else hideAttribute(R.drawable.healthy_icon)
        }

        private fun addAttribute(pic: Int) {

            if(attributes[pic] != null) {
                attributes[pic]?.visibility = View.VISIBLE
                return
            }

            val imageView = ImageView(view.context)
            imageView.setImageResource(pic)
            imageView.adjustViewBounds = true

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            params.gravity = Gravity.END
            params.setMargins((3 * factor).toInt(), 0, 0, 0)
            imageView.layoutParams = params

            attributes[pic] = imageView
            attributesDesk.addView(imageView)
        }

        private fun hideAttribute(pic: Int) {
            attributes[pic]?.visibility = View.GONE
        }

        private fun addRatingBar(rating: Double) {
            ratingBar.rating = (rating / 20.0).toFloat()
            ratingBar.visibility = View.VISIBLE
            ratingBarDesc.visibility = View.VISIBLE
        }

        private fun hideRatingBar() {
            ratingBarDesc.visibility = View.GONE
            ratingBar.visibility = View.GONE
        }

        private fun addProgressBar(
            name: String,
            value: Double,
            minValue: Int = 0,
            maxValue: Int = 100
        ) {
            if(progressBars[name] != null) {
                (progressBars[name])!!.apply {
                    min = minValue.toFloat()
                    max = maxValue.toFloat()
                    labelText = "$name ${value}%"
                    progress = value.toFloat()
                    visibility = View.VISIBLE
                    this.progressFromPrevious = false
                }
                return
            }

            progressBars[name] = ProgressView(view.context).apply {
                progress = value.toFloat()
                labelText = "$name ${value}%"
                labelColorInner = ContextCompat.getColor(view.context, R.color.blue1)
                labelColorOuter = ContextCompat.getColor(view.context, R.color.white)
                min = minValue.toFloat()
                max = maxValue.toFloat()
                autoAnimate = true
                radius = 12 * factor
                borderColor = ContextCompat.getColor(view.context, R.color.white)
                borderWidth = 2
                colorBackground = ContextCompat.getColor(view.context, R.color.blue4)
                highlightView.color = getColorProgressBar(name)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (25 * factor).toInt()
                )
                params.setMargins((5 * factor).toInt())
                params.gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(params)

            }

            view.findViewById<LinearLayout>(R.id.progressBarsLayout).addView(progressBars[name])
        }

        private fun hideProgressBar(name: String) {
            progressBars[name]?.visibility = View.GONE
        }

        private fun getColorProgressBar(name: String) : Int {
            val coloName = name[0].toLowerCase() + name.subSequence(1, name.length).toString() + "_color"
            return ContextCompat.getColor(
                view.context, view.resources.getIdentifier(
                    coloName,
                    "color",
                    view.context.packageName
                )
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recipe_item, parent, false)
        return RecipeViewHolder(view, nutrientsToShow, onClick)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }


    class RecipeComparator : DiffUtil.ItemCallback<Recipe>() {
        override fun areItemsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
            return oldItem.documentId == newItem.documentId && oldItem.title == newItem.title && oldItem.owner == newItem.owner
        }

        override fun areContentsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
            return oldItem.documentId == newItem.documentId && oldItem.title == newItem.title && oldItem.owner == newItem.owner
        }

    }
}