package com.example.projekt_aplikacje.ui.browse

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.projekt_aplikacje.R
import com.example.projekt_aplikacje.api_communication.ErrorTypes
import com.example.projekt_aplikacje.database.Repository
import com.example.projekt_aplikacje.databinding.ActivityBrowseBinding
import com.example.projekt_aplikacje.model.Recipe
import com.google.android.material.chip.Chip
import com.skydoves.transformationlayout.TransformationCompat
import com.skydoves.transformationlayout.TransformationLayout
import com.skydoves.transformationlayout.onTransformationStartContainer
import timber.log.Timber
import www.sanju.motiontoast.MotionToast
import www.sanju.zoomrecyclerlayout.ZoomRecyclerLayout


class BrowseActivity : AppCompatActivity() {

    enum class BrowseMode {
        API, FirebaseFavourites, FirebaseAll
    }

    enum class ActionMode {
        ReturnIntent, AddToFavourites
    }

    private lateinit var recyclerAdapter: RecipeAdapter
    private lateinit var viewModel: BrowseViewModel
    private lateinit var binding: ActivityBrowseBinding
    private var isToast: Boolean = false
    private var browseMode: BrowseMode = BrowseMode.API
    private var actionMode: ActionMode = ActionMode.AddToFavourites

    override fun onCreate(savedInstanceState: Bundle?) {
        onTransformationStartContainer()
        super.onCreate(savedInstanceState)
        binding = ActivityBrowseBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        browseMode = intent.getSerializableExtra("browseMode") as BrowseMode
        actionMode = intent.getSerializableExtra("actionMode") as ActionMode

        viewModel = when (browseMode) {
            BrowseMode.API -> BrowseViewModelAPI { errorType -> error(errorType) }
            BrowseMode.FirebaseFavourites ->
                BrowseViewModelFirebaseFavourites { errorType ->
                    error(
                        errorType
                    )
                }
            BrowseMode.FirebaseAll -> BrowseViewModelFirebaseAll { errorType -> error(errorType) }
        }

        when (actionMode) {
            ActionMode.AddToFavourites -> binding.saveRecipeButton.setOnClickListener { saveRecipe() }
            ActionMode.ReturnIntent -> binding.saveRecipeButton.setOnClickListener { returnRecipeWithIntent() }
        }

        Timber.d("onCreate")

        configureRecyclerView()
        configureSearchingView()
    }

    fun search(query: String) {
        val imm: InputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchingView.searchPanel.windowToken, 0)
        binding.appbar.setExpanded(false)

        val diets = ArrayList<String>()
        val cuisines = ArrayList<String>()
        val types = ArrayList<String>()

        for (pair in listOf(
            Pair(binding.searchingView.filterDiets.chipGroup.checkedChipIds, diets),
            Pair(binding.searchingView.filterCuisines.chipGroup.checkedChipIds, cuisines),
            Pair(binding.searchingView.filterTypes.chipGroup.checkedChipIds, types)
        )) {
            for (id in pair.first) {
                pair.second.add(findViewById<Chip>(id).text.toString())
            }
        }

        Timber.d("seraching $query")
        Timber.d("dietes $diets")
        Timber.d("cuisines $cuisines")
        Timber.d("types $types")

        viewModel.searchQuery = query
        viewModel.searchDiets = diets
        viewModel.searchCuisines = cuisines
        viewModel.searchTypes = types
        viewModel.searchWithNewCriteria { binding.recyclerView.smoothScrollToPosition(0) }
    }

    private fun returnRecipeWithIntent() {
        val intent = Intent()
        intent.putExtra("recipe", getCurrentItem())
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun saveRecipe() {
        val recipe = getCurrentItem()
        Timber.d("Zapisuje $recipe")
        if (recipe != null) {
            Repository.addToFavourite(recipe, {
                showToast(
                    "Added to favourites!",
                    "${recipe.title} added to your favourites!",
                    MotionToast.TOAST_SUCCESS
                )
                Timber.d("Dodano $isToast")
            }) {
                showToast(
                    "Error!",
                    "Something went wrong...",
                    MotionToast.TOAST_ERROR
                )
                Timber.d("Nie udało się")
            }
        }
    }

    private fun seeRecipe(recipe: Recipe, layout: TransformationLayout) {
        Timber.d("Pokazuje $recipe")
        val intent = Intent(this, RecipeInfoActivity::class.java)
        intent.putExtra(RecipeInfo.recipeKey, recipe)
        TransformationCompat.startActivity(layout, intent)
    }

    private fun getCurrentItem(): Recipe? {
        val idInList =
            (binding.recyclerView.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
        return if (viewModel.recipes.value != null && idInList != RecyclerView.NO_POSITION) {
            viewModel.recipes.value!![idInList]
        } else {
            null
        }
    }

    private fun configureSearchingView() {
        Timber.d("configureSearchingView")

        if (browseMode == BrowseMode.FirebaseFavourites) {
            binding.searchingView.root.visibility = View.GONE
            return
        }

        val searchPanel = binding.searchingView.searchPanel
        searchPanel.queryHint = resources.getString(R.string.search_recipes)
        searchPanel.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Timber.d("onSubmit $query")
                if (query != null) {
                    search(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                Timber.d("onChange $newText")
                // podpowiedzi?
                return true
            }

        })

        binding.searchingView.searchButton.setOnClickListener { search(searchPanel.query.toString()) }
        binding.searchingView.root.setBackgroundResource(R.color.blue4)
        val diets = resources.getStringArray(R.array.diets)
        val dietImages = resources.obtainTypedArray(R.array.diet_images)
        Timber.d(dietImages.toString())
        val dietChips = binding.searchingView.filterDiets.chipGroup

        binding.searchingView.filterDiets.textViewHeader.text =
            resources.getString(R.string.diets_word)
        for (i in diets.indices) {
            val chip = Chip(this)
            chip.chipIcon = ContextCompat.getDrawable(this, dietImages.getResourceId(i, -1))
            chip.chipBackgroundColor =
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.blue5))
            chip.setTextColor(ContextCompat.getColor(this, R.color.blue1))
            chip.text = diets[i]
            chip.isCheckable = true
            chip.isChecked = false
            dietChips.addView(chip)
        }

        dietImages.recycle()

        binding.searchingView.filterCuisines.textViewHeader.text =
            resources.getString(R.string.cuisines_word)
        binding.searchingView.filterTypes.textViewHeader.text =
            resources.getString(R.string.meal_types_word)
        val cuisines = resources.getStringArray(R.array.cuisines)
        val cuisineChips = binding.searchingView.filterCuisines.chipGroup
        val types = resources.getStringArray(R.array.meal_types)
        val typeChips = binding.searchingView.filterTypes.chipGroup

        for (pair in listOf(Pair(cuisines, cuisineChips), Pair(types, typeChips))) {
            for (i in pair.first.indices) {
                val chip = Chip(this)
                chip.text = pair.first[i]
                chip.setChipBackgroundColorResource(R.color.blue5)
                chip.setTextColor(ContextCompat.getColor(this, R.color.blue1))
                chip.isCheckable = true
                chip.isChecked = false
                pair.second.addView(chip)
            }
        }

    }

    private fun configureRecyclerView() {
        Timber.d("configureRecyclerView before")

        recyclerAdapter = RecipeAdapter(
            resources.getStringArray(R.array.nutrients).toList()
        ) { recipe, transformationLayout ->
            seeRecipe(
                recipe,
                transformationLayout
            )
        }

        val linearLayoutManager = ZoomRecyclerLayout(this)
        linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        binding.recyclerView.layoutManager = linearLayoutManager

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.recyclerView)
        binding.recyclerView.isNestedScrollingEnabled = false
        binding.recyclerView.adapter = recyclerAdapter

        viewModel.recipes.observe(this) { recipes ->
            recyclerAdapter.submitList(recipes)
            recyclerAdapter.notifyDataSetChanged()
        }

        binding.recyclerView.addOnScrollListener(RecyclerScroll(viewModel))
        viewModel.fetchRecipes(howMuch = 5)
        Timber.d("configureRecyclerView after")
    }

    private fun error(errorType: ErrorTypes) {
        when (errorType) {
            ErrorTypes.NO_MORE_RESULTS -> noMoreResults()
            ErrorTypes.INTERNET_CONNECTION -> connectionError()
            ErrorTypes.API -> connectionError()
        }
    }

    private fun noMoreResults() {
        Timber.d("noMoreResultsError")
        showToast(
            "No more results",
            "Cannot find more recipes!\nChange parameters",
            MotionToast.TOAST_INFO
        )
        binding.appbar.setExpanded(true, true)
    }

    private fun connectionError() {
        Timber.d("connectionError")
        showToast(
            "Connection problem",
            "Cannot download more recipes!",
            MotionToast.TOAST_NO_INTERNET
        )
    }

    private fun showToast(title: String, message: String, toastType: String) {
        if (!isToast) {
            MotionToast.createColorToast(
                this,
                title,
                message,
                toastType,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.LONG_DURATION,
                ResourcesCompat.getFont(this, R.font.helvetica_regular)
            )
            isToast = true

            // zrobione tak aby nie wyświetlał się tost na toście
            val timer = object : CountDownTimer(MotionToast.LONG_DURATION / 2, 10000) {
                override fun onTick(millisUntilFinished: Long) {
                }

                override fun onFinish() {
                    isToast = false
                }
            }
            timer.start()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.searchingView.searchPanel.clearFocus()
    }

}