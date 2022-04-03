package com.example.projekt_aplikacje.ui.browse

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.setMargins
import androidx.fragment.app.Fragment
import com.example.projekt_aplikacje.R
import com.example.projekt_aplikacje.database.Repository
import com.example.projekt_aplikacje.database.User
import com.example.projekt_aplikacje.databinding.FragmentNewRecipeBinding
import com.example.projekt_aplikacje.model.RecipeCreator
import com.google.android.material.chip.Chip
import com.skydoves.progressview.ProgressView
import com.skydoves.transformationlayout.onTransformationStartContainer
import com.squareup.picasso.Picasso
import timber.log.Timber
import www.sanju.motiontoast.MotionToast
import kotlin.math.roundToInt

@ExperimentalStdlibApi
class NewRecipeFragment : Fragment() {

    private var _binding: FragmentNewRecipeBinding? = null
    private val binding get() = _binding!!

    private val recipeCreator = RecipeCreator(User.name)
    private val chipMap = HashMap<String, Chip>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onTransformationStartContainer()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentNewRecipeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                return
            }
            Timber.d(data.toString())
            setImage(data.data!!)
        }

    }

    private fun setImage(image: Uri) {
        Picasso.get().load(image).fit().centerCrop()
            .placeholder(R.drawable.dish_placeholder).into(binding.imageViewDishPicture)

        recipeCreator.image = image
    }

    private fun initUI() {

        Timber.d("Init UI")

        binding.attributesDesc.root.text = resources.getText(R.string.attributes)
        attributesChipGroup()
        binding.cuisinesDesc.root.text = resources.getText(R.string.cuisines_word)
        cuisinesChipGroup()
        binding.dishTypesDesc.root.text = resources.getText(R.string.meal_types_word)
        dishTypesChipGroups()
        binding.prepareTimeDesc.root.text = resources.getText(R.string.prepare_time)
        binding.servingsDesc.root.text = resources.getText(R.string.serving_word)
        binding.pictureDesc.root.text = resources.getText(R.string.picture)
        binding.imageViewDishPicture.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), IMAGE_REQUEST)
        }
        binding.ratingBarDesc.root.text = resources.getText(R.string.health_score)

        initCards()

        binding.ingredientsDesc.root.text = resources.getText(R.string.ingredients)
        binding.nutritionDesc.root.text = resources.getText(R.string.percent_of_daily_needs)
        binding.stepsDesc.root.text = resources.getText(R.string.steps)

        binding.buttonAddRecipe.setOnClickListener { onClickAddRecipe() }
    }

    private fun onClickAddRecipe() {

        if (binding.editTextServings.text.toString() == "" || binding.editTextPrepareTime.text.toString() == "") {
            showToastError("Invalid data")
            return
        }

        setInputToRecipeCreator()

        if (recipeCreator.isCorrectRecipe()) {
            Repository.createRecipeAndAdd(recipeCreator, {
                Timber.d("Dodano do bazy")
                requireActivity().finish()
            }, { Timber.d("Nie udało się dodać") })

        } else {
            Timber.d("Nie Correct OK")
            showToastError("Can't add to database.")
        }
    }

    private fun initCards() {
        initIngredientCard()
        initNutritionCard()
        initStepsCard()
    }

    private fun initIngredientCard() {
        binding.buttonIngredients.setOnClickListener {
            newIngredientStart()
        }

        binding.ingredientsCard.buttonAdd.setOnClickListener {
            newIngredientAdd()
        }

        binding.ingredientsCard.root.setOnClickListener {
            binding.transformationLayoutIngredients.finishTransform()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initNutritionCard() {
        binding.buttonNutrition.setOnClickListener {
            newNutritionStart()
        }
        binding.nutritionCard.buttonAdd.setOnClickListener {
            newNutritionAdd()
        }

        binding.nutritionCard.textViewPercent.text =
            "${binding.nutritionCard.seekBarPercent.progress}%"
        binding.nutritionCard.seekBarPercent.setOnSeekBarChangeListener(object :
            OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                binding.nutritionCard.textViewPercent.text = "$progress%"
            }
        })

        binding.nutritionCard.root.setOnClickListener { binding.transformationLayoutNutrition.finishTransform() }
    }

    private fun initStepsCard() {
        binding.buttonSteps.setOnClickListener {
            newStepStart()
        }
        binding.stepsCard.buttonAdd.setOnClickListener {
            newStepAdd()
        }

        binding.stepsCard.root.setOnClickListener { binding.transformationLayoutSteps.finishTransform() }
    }

    private fun newIngredientStart() {
        binding.transformationLayoutIngredients.startTransform()
    }

    @SuppressLint("SetTextI18n")
    private fun newIngredientAdd() {
        val name: String = binding.ingredientsCard.editTextIngredientName.text.toString()
        val amount: String = binding.ingredientsCard.editTextAmount.text.toString()
        val unit: String = binding.ingredientsCard.editTextUnit.text.toString()

        if (RecipeCreator.isOKIngredient(name, amount, unit)) {
            recipeCreator.addIngredient(name, amount, unit)

            val ingredientEntry =
                layoutInflater.inflate(R.layout.ingredient_entry, binding.ingredientsLayout, false)
            ingredientEntry.findViewById<TextView>(R.id.textViewAmountAndUnit).text =
                "$amount $unit"
            ingredientEntry.findViewById<TextView>(R.id.textViewIngredientName).text = name
            binding.ingredientsLayout.addView(ingredientEntry)
            binding.ingredientsCard.editTextUnit.text.clear()
            binding.ingredientsCard.editTextAmount.text.clear()
            binding.ingredientsCard.editTextIngredientName.text.clear()

            binding.transformationLayoutIngredients.finishTransform()
        } else {
            showToastError("Invalid data")
        }
    }

    private fun newNutritionStart() {
        binding.transformationLayoutNutrition.startTransform()
    }

    private fun newNutritionAdd() {

        val nutrition: String = binding.nutritionCard.spinner.selectedItem.toString()
        val percent: Double = binding.nutritionCard.seekBarPercent.progress.toDouble()

        if (!recipeCreator.containNutrient(name = nutrition)) {

            recipeCreator.addNutrient(nutrition, percent)

            val progressBar = ProgressView(requireContext()).apply {
                progress = percent.toFloat()
                labelText = "$nutrition ${percent}%"
                labelColorInner = ContextCompat.getColor(requireContext(), R.color.blue1)
                labelColorOuter = ContextCompat.getColor(requireContext(), R.color.white)
                min = 0.toFloat()
                max = 100.toFloat()
                autoAnimate = true
                radius = 12 * requireContext().resources.displayMetrics.density
                borderColor = ContextCompat.getColor(requireContext(), R.color.white)
                borderWidth = 2
                colorBackground = ContextCompat.getColor(requireContext(), R.color.blue4)
                highlightView.color = getColorProgressBar(nutrition)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (25 * requireContext().resources.displayMetrics.density).toInt()
                )
                params.setMargins((5 * requireContext().resources.displayMetrics.density).toInt())
                params.gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(params)

            }

            binding.nutritionLayout.addView(progressBar)
        }



        binding.nutritionCard.seekBarPercent.progress = 10
        binding.transformationLayoutNutrition.finishTransform()
    }

    private fun newStepStart() {
        binding.transformationLayoutSteps.startTransform()
    }

    private fun newStepAdd() {
        val stepContent = binding.stepsCard.editTextTextMultiLine.text.toString()
        if (RecipeCreator.isOKStep(stepContent)) {
            val stepEntry = layoutInflater.inflate(R.layout.step_entry, binding.stepsLayout, false)
            stepEntry.findViewById<TextView>(R.id.textViewStepNumber).text =
                recipeCreator.addStep(stepContent).toString()
            stepEntry.findViewById<TextView>(R.id.textViewStepContent).text = stepContent
            binding.stepsLayout.addView(stepEntry)
            binding.stepsCard.editTextTextMultiLine.text.clear()
            binding.transformationLayoutSteps.finishTransform()
        } else {
            showToastError("Invalid data")
        }
    }

    private fun attributesChipGroup() {
        val diets = resources.getStringArray(R.array.diets)
        val dietImages = resources.obtainTypedArray(R.array.diet_images)
        Timber.d(dietImages.toString())
        val attributesChips = binding.chipGroupAttributes

        for (i in diets.indices) {
            val chip = Chip(context)
            chip.chipIcon = ContextCompat.getDrawable(requireContext(), dietImages.getResourceId(i, -1))
            chip.setChipBackgroundColorResource(R.color.blue5)
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue1))
            chip.text = diets[i]
            chip.isCheckable = true
            chip.isChecked = false
            attributesChips.addView(chip)
            chipMap[diets[i]] = chip
        }

        dietImages.recycle()

        for (i in listOf(
            Pair(resources.getString(R.string.cheap), R.drawable.cheap_icon),
            Pair(resources.getString(R.string.healthy), R.drawable.healthy_icon)
        )) {
            val chip = Chip(context)
            chip.chipIcon =
                ContextCompat.getDrawable(requireContext(), i.second)
            chip.setChipBackgroundColorResource(R.color.blue5)
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue1))
            chip.text = i.first
            chip.isCheckable = true
            chip.isChecked = false
            attributesChips.addView(chip)
            chipMap[i.first] = chip
        }
    }

    private fun cuisinesChipGroup() {
        val cuisines = resources.getStringArray(R.array.cuisines)
        val cuisineChips = binding.chipGroupCuisines

        for (i in cuisines) {
            val chip = Chip(context)
            chip.text = i
            chip.setChipBackgroundColorResource(R.color.blue5)
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue1))
            chip.isCheckable = true
            chip.isChecked = false
            cuisineChips.addView(chip)
        }
    }

    private fun dishTypesChipGroups() {
        val types = resources.getStringArray(R.array.meal_types)
        val typeChips = binding.chipGroupDishTypes

        for (i in types) {
            val chip = Chip(context)
            chip.text = i
            chip.setChipBackgroundColorResource(R.color.blue5)
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue1))
            chip.isCheckable = true
            chip.isChecked = false
            typeChips.addView(chip)
        }
    }

    private fun getColorProgressBar(name: String): Int {
        val coloName =
            name[0].toLowerCase() + name.subSequence(1, name.length).toString() + "_color"
        return ContextCompat.getColor(
            requireContext(), resources.getIdentifier(
                coloName,
                "color",
                requireContext().packageName
            )
        )
    }

    private fun showToastError(message: String) {
        MotionToast.createColorToast(
            requireActivity(),
            ":(",
            message,
            MotionToast.TOAST_ERROR,
            MotionToast.GRAVITY_BOTTOM,
            MotionToast.LONG_DURATION,
            ResourcesCompat.getFont(requireContext(), R.font.helvetica_regular)
        )
    }

    @ExperimentalStdlibApi
    private fun setInputToRecipeCreator() {
        recipeCreator.title = binding.editTextTextPersonName.text.toString()
        recipeCreator.healthScore = (20 * binding.healthRatingBar.rating).roundToInt().toDouble()
        recipeCreator.servings = binding.editTextServings.text.toString().toLong()
        recipeCreator.readyInMinutes = binding.editTextPrepareTime.text.toString().toLong()

        recipeCreator.vegetarian = chipMap[resources.getString(R.string.vegetarian)]!!.isChecked
        recipeCreator.vegan = chipMap[resources.getString(R.string.vegan)]!!.isChecked
        recipeCreator.cheap = chipMap[resources.getString(R.string.cheap)]!!.isChecked
        recipeCreator.ketogenic = chipMap[resources.getString(R.string.keto)]!!.isChecked
        recipeCreator.glutenFree = chipMap[resources.getString(R.string.gluten)]!!.isChecked
        recipeCreator.veryHealthy = chipMap[resources.getString(R.string.healthy)]!!.isChecked

        recipeCreator.diets.clear()
        recipeCreator.cuisines.clear()
        recipeCreator.dishTypes.clear()

        if (recipeCreator.vegetarian) {
            recipeCreator.diets.add(resources.getString(R.string.vegetarian).lowercase())
        }

        if (recipeCreator.ketogenic) {
            recipeCreator.diets.add(resources.getString(R.string.keto).lowercase())
        }

        if (recipeCreator.glutenFree) {
            recipeCreator.diets.add(resources.getString(R.string.vegan).lowercase())
        }

        if (recipeCreator.vegan) {
            recipeCreator.diets.add(resources.getString(R.string.gluten).lowercase())
        }

        for (c in binding.chipGroupCuisines.checkedChipIds) {
            recipeCreator.cuisines.add(requireView().findViewById<Chip>(c).text.toString())
        }

        for (c in binding.chipGroupDishTypes.checkedChipIds) {
            recipeCreator.dishTypes.add(requireView().findViewById<Chip>(c).text.toString())
        }
    }

    companion object {
        private const val IMAGE_REQUEST = 1
    }

}