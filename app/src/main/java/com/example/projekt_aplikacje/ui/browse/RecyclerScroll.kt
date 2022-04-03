package com.example.projekt_aplikacje.ui.browse


import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import timber.log.Timber

class RecyclerScroll(private val viewModel: BrowseViewModel) : RecyclerView.OnScrollListener() {

    private var loading = false

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        if (dx > 0 && !loading) {
            val layout = recyclerView.layoutManager!! as LinearLayoutManager
            val visibleItemCount = layout.childCount
            val totalItemCount = layout.itemCount
            val pastVisibleItems = layout.findFirstVisibleItemPosition()

            if (visibleItemCount + pastVisibleItems >= totalItemCount) {
                loading = true
                Timber.d("Potrzebne załadownie nowych elementów")
                viewModel.fetchRecipes(postAction = { loading = false })
                Timber.d("Po załadowaniu")
            }
        }
    }
}
