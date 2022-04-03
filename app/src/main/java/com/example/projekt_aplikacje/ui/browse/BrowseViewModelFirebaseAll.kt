package com.example.projekt_aplikacje.ui.browse

import com.example.projekt_aplikacje.api_communication.ErrorTypes
import com.example.projekt_aplikacje.database.Repository
import com.google.firebase.firestore.DocumentSnapshot
import timber.log.Timber

class BrowseViewModelFirebaseAll(private val error: (ErrorTypes) -> Unit) : BrowseViewModel() {

    private var lastRecipe: DocumentSnapshot? = null
    private var errorShown = false

    init {
        currentRecipes.value = recipesData
    }

    override fun searchWithNewCriteria(postAction: (() -> Unit)?) {
        lastRecipe = null
        errorShown = false
        super.searchWithNewCriteria(postAction)
    }

    override fun fetchRecipes(postAction: (() -> Unit)?, howMuch: Int) {

        if (!isMoreResults) {
            Timber.d("Nie ma więcej!!!")
            if (postAction != null) {
                postAction()
            }

            if (!errorShown) {
                error(ErrorTypes.NO_MORE_RESULTS)
                errorShown = true
            }

            return
        }

        if (searchDiets?.isEmpty() == true) {
            searchDiets = null
        }

        if (searchCuisines?.isEmpty() == true) {
            searchCuisines = null
        }

        if (searchTypes?.isEmpty() == true) {
            searchTypes = null
        }

        Repository.getManyRecipesWithFiltersFromAll(
            lastRecipe,
            howMuch = 50,
            diets = searchDiets,
            types = searchTypes,
            cuisines = searchCuisines,
            onSuccess = { list, last ->
                if (list.isEmpty()) {
                    isMoreResults = false
                    notifyObservers()
                    if (postAction != null) {
                        postAction()
                    }
                    error(ErrorTypes.NO_MORE_RESULTS)
                    return@getManyRecipesWithFiltersFromAll
                }

                if (list.size < 50) {
                    isMoreResults = false
                }

                recipesData.addAll(list)
                notifyObservers()
                if (postAction != null) {
                    postAction()
                }

                lastRecipe = last
            },
            onFailure = {
                error(ErrorTypes.API)
                Timber.e("Coś nie tak")
            })
    }
}