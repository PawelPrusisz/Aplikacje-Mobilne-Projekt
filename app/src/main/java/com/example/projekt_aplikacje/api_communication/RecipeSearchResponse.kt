package com.example.projekt_aplikacje.api_communication

import com.example.projekt_aplikacje.model.Recipe
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RecipeSearchResponse(val results : List<Recipe>, val offset : Int, val number : Int, val totalResults : Int)