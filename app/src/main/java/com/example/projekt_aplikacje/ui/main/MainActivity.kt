package com.example.projekt_aplikacje.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.projekt_aplikacje.R
import com.example.projekt_aplikacje.database.Repository
import com.example.projekt_aplikacje.database.User
import com.example.projekt_aplikacje.databinding.ActivityMainBinding
import com.example.projekt_aplikacje.ui.browse.BrowseActivity
import com.example.projekt_aplikacje.ui.browse.NewRecipeActivity
import com.example.projekt_aplikacje.ui.diet_plan.WeeklyDietViewer
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
import timber.log.Timber


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    init {
        Timber.plant(Timber.DebugTree())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        Timber.d("OnCreate")

        initUI()

        val db = Firebase.firestore
        val auth = Firebase.auth

        if (auth.currentUser == null) {
            signIn()
        } else {
            User.name = Firebase.auth.currentUser!!.displayName!!
            Repository.addUserIfNotExist({}, {})
        }

        val settings = firestoreSettings {
            isPersistenceEnabled = true
        }
        db.firestoreSettings = settings
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Timber.d("Result sign in ok")
                if (Firebase.auth.currentUser != null) {
                    User.name = Firebase.auth.currentUser!!.displayName!!
                    Repository.addUserIfNotExist({}, {})
                }
            } else {
                Timber.d("Result sign in nie ok")
                finish()
            }
        }
    }


    private fun signIn() {
        startActivityForResult(
            AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(
                listOf(
                    AuthUI.IdpConfig.EmailBuilder().build()
                )
            ).setTheme(R.style.Theme_Projekt_Aplikacje).setIsSmartLockEnabled(false)
                .setLogo(R.drawable.dish_placeholder).build(), 1)
    }

    private fun initUI() {
        binding.buttonSearchForRecipes.setOnClickListener {
            binding.transformationLayout.startTransform()
        }
        binding.button2.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    WeeklyDietViewer::class.java
                )
            )
        }

        binding.buttonNewRecipe.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    NewRecipeActivity::class.java
                )
            )
        }

        binding.searchForCard.root.setOnClickListener {
            binding.transformationLayout.finishTransform()
        }

        binding.searchForCard.APIButton.setOnClickListener {
            startSearching(BrowseActivity.BrowseMode.API)
        }

        binding.searchForCard.favouritesButton.setOnClickListener {
            startSearching(BrowseActivity.BrowseMode.FirebaseFavourites)
        }

        binding.searchForCard.communityButton.setOnClickListener {
            startSearching(BrowseActivity.BrowseMode.FirebaseAll)
        }
    }


    private fun startSearching(browseMode: BrowseActivity.BrowseMode) {
        val intent = Intent(
            this,
            BrowseActivity::class.java
        )

        intent.putExtra("browseMode", browseMode)
        intent.putExtra("actionMode", BrowseActivity.ActionMode.AddToFavourites)

        startActivity(intent)
    }
}