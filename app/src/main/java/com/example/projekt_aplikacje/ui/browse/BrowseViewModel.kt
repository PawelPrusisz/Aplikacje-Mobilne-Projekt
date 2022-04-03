package com.example.projekt_aplikacje.ui.browse

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.projekt_aplikacje.model.Recipe

abstract class BrowseViewModel : ViewModel() {
    protected val recipesData = ArrayList<Recipe>()
    protected val currentRecipes = MutableLiveData<List<Recipe>>()
    protected var isMoreResults = true
    val recipes: LiveData<List<Recipe>> = currentRecipes

    var searchQuery = ""
    var searchDiets: List<String>? = null
    var searchCuisines: List<String>? = null
    var searchTypes: List<String>? = null


    init {
        currentRecipes.value = recipesData
    }

    abstract fun fetchRecipes(postAction: (() -> Unit)? = null, howMuch: Int = 25)

    open fun searchWithNewCriteria(postAction: (() -> Unit)?) {
        isMoreResults = true
        recipesData.clear()
        fetchRecipes(postAction)
    }

    protected fun addRecipe(recipe: Recipe) {
        recipesData.add(recipe)
    }

    protected fun notifyObservers() {
        currentRecipes.value = recipesData
    }
}