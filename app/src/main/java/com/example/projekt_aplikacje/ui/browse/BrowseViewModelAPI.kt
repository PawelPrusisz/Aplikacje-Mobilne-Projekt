package com.example.projekt_aplikacje.ui.browse

import com.example.projekt_aplikacje.api_communication.APIService
import com.example.projekt_aplikacje.api_communication.ErrorTypes
import com.example.projekt_aplikacje.api_communication.RecipeSearchResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

class BrowseViewModelAPI(private val error: (ErrorTypes) -> Unit) : BrowseViewModel() {
    private val API = APIService.api

    init {
        currentRecipes.value = recipesData
    }

    override fun fetchRecipes(postAction: (() -> Unit)?, howMuch: Int) {
        val diet = if (searchDiets != null) searchDiets!!.joinToString(separator = ",") else ""
        val cuisine =
            if (searchCuisines != null) searchCuisines!!.joinToString(separator = ",") else ""
        val type = if (searchTypes != null) searchTypes!!.joinToString(separator = ",") else ""

        Timber.d(
            "fetchRecipes -> howMuch: %d, query: %s, diet: %s, type: %s, cuisine: %s",
            howMuch,
            searchQuery,
            diet,
            type,
            cuisine
        )

        if (!isMoreResults) {
            Timber.d("Nie ma więcej!!!")
            if (postAction != null) {
                postAction()
            }
            return
        }

        val request = API.searchRecipes(
            number = howMuch,
            query = searchQuery,
            diet = diet,
            type = type,
            cuisine = cuisine,
            offset = recipes.value!!.size
        )
        request.enqueue(object : Callback<RecipeSearchResponse?> {
            override fun onResponse(
                call: Call<RecipeSearchResponse?>,
                response: Response<RecipeSearchResponse?>,
            ) {
                if (response.isSuccessful) {
                    val recipeSearchResponse = response.body()

                    if (recipeSearchResponse != null) {

                        Timber.d("number=${recipeSearchResponse.number}, total=${recipeSearchResponse.totalResults}, offset=${recipeSearchResponse.offset}")

                        if (recipeSearchResponse.totalResults <= 1) {
                            Timber.w("no more results!")
                            isMoreResults = false
                            error(ErrorTypes.NO_MORE_RESULTS)
                        }

                        var counter = 0
                        for (recipe in recipeSearchResponse.results) {
                            recipe.image = recipe.image.replace("312x231", "636x393")
                            addRecipe(recipe)
                            counter++
                            if (counter == 5) {
                                notifyObservers()
                                counter = 0
                            }
                        }

                        notifyObservers()

                        if (postAction != null) {
                            postAction()
                        }
                    }
                } else {
                    error(ErrorTypes.API)
                    Timber.e("response not successful: t= %s", response)
                }
            }

            override fun onFailure(call: Call<RecipeSearchResponse?>, t: Throwable) {
                error(ErrorTypes.INTERNET_CONNECTION)
                Timber.e(t, "onFailure")
            }
        })
    }
}