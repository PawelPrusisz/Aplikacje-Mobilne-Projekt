package com.example.projekt_aplikacje.model

import android.net.Uri

data class RecipeCreator(
    val owner: String,
    var vegetarian: Boolean = false,
    var vegan: Boolean = false,
    var glutenFree: Boolean = false,
    var ketogenic: Boolean = false,
    var veryHealthy: Boolean = false,
    var cheap: Boolean = false,
    var veryPopular: Boolean = false,
    var healthScore: Double = 0.0,
    var id: String = "",
    var image: Uri = Uri.EMPTY,
    var title: String = "",
    var readyInMinutes: Long = -1,
    var servings: Long = -1,
    val cuisines: ArrayList<String> = ArrayList(),
    val dishTypes: ArrayList<String> = ArrayList(),
    val diets: ArrayList<String> = ArrayList(),
    val ingredients: ArrayList<Recipe.Ingredient> = ArrayList(),
    val nutrients: ArrayList<Recipe.NutritionEntry> = ArrayList(),
    val steps: ArrayList<Recipe.Step> = ArrayList()
) {


    fun getRecipe(): Recipe {
        return Recipe(
            owner,
            vegetarian,
            vegan,
            glutenFree,
            ketogenic,
            veryHealthy,
            cheap,
            veryPopular,
            healthScore,
            id,
            "",
            title,
            readyInMinutes,
            servings,
            diets = if (diets.size == 0) null else diets,
            cuisines = if (cuisines.size == 0) null else cuisines,
            dishTypes = if (dishTypes.size == 0) null else dishTypes,
            nutrition = Recipe.Nutrition(nutrients, ingredients),
            analyzedInstructions = if (steps.size == 0) null else listOf(
                Recipe.AnalyzedInstruction(
                    steps
                )
            )
        )
    }

    fun isCorrectRecipe(): Boolean {
        return healthScore >= 0.0 && title != "" && readyInMinutes > 0 && servings > 0
    }

    fun containNutrient(name: String): Boolean {
        for (i in nutrients) {
            if (i.title == name) {
                return true
            }
        }

        return false
    }

    fun addIngredient(name: String, amount: String, unit: String) {
        ingredients.add(Recipe.Ingredient(name, amount.toDouble(), unit))
    }

    fun addNutrient(name: String, percent: Double) {
        nutrients.add(Recipe.NutritionEntry(name, percent))
    }

    fun addStep(stepContent: String): Int {
        val number = steps.size.toLong() + 1
        steps.add(Recipe.Step(number, stepContent))
        return number.toInt()
    }

    companion object {
        fun isOKStep(stepContent: String): Boolean {
            return (stepContent != "")
        }

        fun isOKIngredient(name: String, amount: String, unit: String): Boolean {
            val amountDouble = amount.toDoubleOrNull() ?: return false
            return name != "" && amountDouble > 0 && unit != "" && unit.contains("[A-z ]*".toRegex())
        }
    }
}