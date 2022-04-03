package com.example.projekt_aplikacje.ui.browse


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.projekt_aplikacje.R
import com.example.projekt_aplikacje.model.Recipe


class RecipeInfo : Fragment() {

    private var recipe: Recipe? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (activity?.intent != null) {
            recipe = requireActivity().intent.getSerializableExtra(recipeKey) as Recipe?
        }

        initUI()
    }

    fun setRecipe(recipe: Recipe) {
        this.recipe = recipe
        initUI()
    }

    fun hideTitle() {
        requireView().findViewById<TextView>(R.id.textViewRecipeTitle).visibility = View.GONE
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_recipe_info, container, false)
    }

    private fun initUI() {
        if (recipe == null || view == null) {
            return
        }

        requireView().findViewById<TextView>(R.id.textViewRecipeTitle).text = recipe!!.title

        requireView().findViewById<TextView>(R.id.textViewServingsInfo).text = requireView().resources.getString(
            R.string.servings,
            recipe!!.servings.toString()
        )

        requireView().findViewById<TextView>(R.id.textViewTimeInfo).text = requireView().resources.getString(
            R.string.readyIn,
            recipe!!.readyInMinutes.toString()
        )

        ingredients(recipe!!)
        steps(recipe!!)
    }

    @SuppressLint("SetTextI18n")
    private fun ingredients(recipe: Recipe) {
        val textViewIngredientsDesc = requireView().findViewById<TextView>(R.id.ingredientsDesc)
        val ingredientsLayout = requireView().findViewById<LinearLayout>(R.id.ingredientsList)

        ingredientsLayout.removeAllViews()

        textViewIngredientsDesc.text = resources.getString(R.string.ingredients)

        if (recipe.nutrition != null) {
            for (ingredient in recipe.nutrition.ingredients) {
                val ingredientEntry =
                    layoutInflater.inflate(R.layout.ingredient_entry, ingredientsLayout, false)
                ingredientEntry.findViewById<TextView>(R.id.textViewAmountAndUnit).text =
                    "${ingredient.amount} ${ingredient.unit}"
                ingredientEntry.findViewById<TextView>(R.id.textViewIngredientName).text =
                    ingredient.name
                ingredientsLayout.addView(ingredientEntry)
            }
        }
    }

    private fun steps(recipe: Recipe) {
        val textViewStepsDesc = requireView().findViewById<TextView>(R.id.recipeDesc)
        val stepsLayout = requireView().findViewById<LinearLayout>(R.id.stepsList)

        stepsLayout.removeAllViews()

        textViewStepsDesc.text = resources.getString(R.string.steps)

        fun createEntry(headerString: String, string: String) {
            val stepEntry = layoutInflater.inflate(R.layout.step_entry, stepsLayout, false)
            stepEntry.findViewById<TextView>(R.id.textViewStepNumber).text = headerString
            stepEntry.findViewById<TextView>(R.id.textViewStepContent).text = string
            stepsLayout.addView(stepEntry)
        }

        if (recipe.analyzedInstructions != null && recipe.analyzedInstructions.isNotEmpty()) {
            for (step in recipe.analyzedInstructions[0].steps) {
                createEntry(step.number.toString(), step.step)
            }
        } else {
            createEntry(":(", resources.getString(R.string.no_data))
        }
    }

    companion object {
        const val recipeKey = "recipe"
    }
}