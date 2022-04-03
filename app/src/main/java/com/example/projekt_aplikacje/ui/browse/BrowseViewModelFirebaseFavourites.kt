package com.example.projekt_aplikacje.ui.browse

import com.example.projekt_aplikacje.api_communication.ErrorTypes
import com.example.projekt_aplikacje.database.Repository
import timber.log.Timber

class BrowseViewModelFirebaseFavourites(private val error: (ErrorTypes) -> Unit) :
    BrowseViewModel() {

    private var favourites: List<String>
    private var firstFetch = false
    private var initialized = false

    init {
        currentRecipes.value = recipesData
        favourites = ArrayList()
        Repository.getFavourites({
            if (it != null) {
                favourites = it
                Timber.d(favourites.toString())
            } else {
                Timber.d("Nie ma ulubionych")
            }
            initialized = true

            if (firstFetch) {
                isMoreResults = true
                fetchRecipes(howMuch = 5)
            }

        }) {
            Timber.d("Nie ma ulubionych")
        }
    }

    override fun fetchRecipes(postAction: (() -> Unit)?, howMuch: Int) {
        firstFetch = true
        if (!initialized) {
            if (postAction != null) {
                postAction()
            }
            return
        }

        if (!isMoreResults) {
            Timber.d("Nie ma więcej!!!")
            if (postAction != null) {
                postAction()
            }
            return
        }

        val endIndex = (recipesData.size + howMuch).coerceAtMost(favourites.size)
        val nextFetch = favourites.subList(recipesData.size, endIndex)

        if (nextFetch.isEmpty()) {
            isMoreResults = false
            error(ErrorTypes.NO_MORE_RESULTS)
            return
        }

        Repository.getManyRecipes(
            nextFetch,
            {

                if (it.isEmpty()) {
                    isMoreResults = false
                    error(ErrorTypes.NO_MORE_RESULTS)
                    return@getManyRecipes
                }

                recipesData.addAll(it)
                notifyObservers()
                if (postAction != null) {
                    postAction()
                }
            },
            {
                error(ErrorTypes.API)
                Timber.e("Coś nie tak")
            })
    }
}