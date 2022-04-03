package com.example.projekt_aplikacje.api_communication

import com.example.projekt_aplikacje.model.Recipes
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface RecipeEndpoints {

    @GET("/recipes/complexSearch")
    fun searchRecipes(
        @Query("apiKey") key: String = API.key,
        @Query("query") query: String = "",
        @Query("cuisine") cuisine: String = "",
        @Query("diet") diet: String = "",
        @Query("type") type: String = "",
        @Query("number") number: Int = 5,
        @Query("offset") offset: Int = 0,
        @Query("addRecipeNutrition") includeNutrition: Boolean = true
    ): Call<RecipeSearchResponse>

    @GET("/recipes/random/")
    fun getRandomRecipe(@Query("apiKey") key: String = API.key): Call<Recipes>
}