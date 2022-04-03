package com.example.projekt_aplikacje.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Recipes(val recipes : List<Recipe>)