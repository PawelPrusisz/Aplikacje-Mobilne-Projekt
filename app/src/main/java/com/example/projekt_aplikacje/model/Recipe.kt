package com.example.projekt_aplikacje.model

import com.google.firebase.firestore.DocumentId
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class Recipe(
    val owner: String = "spoonacular",
    val vegetarian: Boolean = false,
    val vegan: Boolean = false,
    val glutenFree: Boolean = false,
    val ketogenic: Boolean = false,
    val veryHealthy: Boolean = false,
    val cheap: Boolean = false,
    val veryPopular: Boolean = false,
    val healthScore: Double? = null,
    @DocumentId
    val documentId: String = "",
    var image: String = "",
    val title: String = "",
    val readyInMinutes: Long = -1,
    val servings: Long = -1,
    val cuisines: List<String>? = null,
    val dishTypes: List<String>? = null,
    val diets: List<String>? = null,
    val nutrition: Nutrition? = null,
    val analyzedInstructions: List<AnalyzedInstruction>? = null,
) : Serializable {

    @JsonClass(generateAdapter = true)
    data class AnalyzedInstruction(
        val steps: List<Step> = ArrayList(),
    ) : Serializable

    @JsonClass(generateAdapter = true)
    data class Step(
        val number: Long = 0,
        val step: String = "",
    ) : Serializable

    @JsonClass(generateAdapter = true)
    data class Nutrition(
        val nutrients: List<NutritionEntry> = ArrayList(),
        val ingredients: List<Ingredient> = ArrayList(),
    ) : Serializable

    @JsonClass(generateAdapter = true)
    data class NutritionEntry(
        val title: String = "",
        val percentOfDailyNeeds: Double = 0.0,
    ) : Serializable

    @JsonClass(generateAdapter = true)
    data class Ingredient(
        val name: String = "",
        val amount: Double = 0.0,
        val unit: String = "",
    ) : Serializable
}
