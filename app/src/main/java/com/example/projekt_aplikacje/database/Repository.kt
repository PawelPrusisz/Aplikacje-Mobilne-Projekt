package com.example.projekt_aplikacje.database

import android.net.Uri
import com.example.projekt_aplikacje.model.Day
import com.example.projekt_aplikacje.model.Recipe
import com.example.projekt_aplikacje.model.RecipeCreator
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import timber.log.Timber


class Repository {

    companion object {

        fun createRecipeAndAdd(
            recipeCreator: RecipeCreator,
            onSuccess: (Recipe) -> Unit,
            onFailure: (Exception) -> Unit,
        ) {
            if (BuildConfig.DEBUG && !recipeCreator.isCorrectRecipe()) {
                error("Assertion failed")
            }

            val recipe = recipeCreator.getRecipe()

            if (recipeCreator.image == Uri.EMPTY) {
                addToFavourite(recipe, { onSuccess(recipe) }, { onFailure(it) })
                return
            }

            val storage = FirebaseStorage.getInstance()
            val imageRef =
                storage.reference.child("images/${recipeCreator.owner}_${recipeCreator.image.hashCode()}.jpg")


            val uploadTask = imageRef.putFile(recipeCreator.image)
            uploadTask.addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    imageRef.downloadUrl.addOnSuccessListener {
                        recipe.image = it.toString()
                        addToFavourite(
                            recipe,
                            { onSuccess(recipe) },
                            { onFailure(java.lang.Exception()) })
                    }.addOnFailureListener(onFailure)

                } else {
                    Timber.e("Upload not OK")
                }
            }.addOnFailureListener {
                Timber.e("Upload not OK")
                onFailure(it)
            }
        }


        /**
         * Dodaje przepis do bazy.
         * Jeśli już taki istnieje bedzie duplikat!
         */
        fun addNewRecipe(
            recipe: Recipe,
            onSuccess: (DocumentReference) -> Unit,
            onFailure: (Exception) -> Unit,
        ) {
            val db = Firebase.firestore

            db.collection("recipes/").add(recipe)
                .addOnSuccessListener {
                    Timber.d("Document added!")
                    onSuccess(it)
                }
                .addOnFailureListener { e ->
                    Timber.w("Error adding document $e")
                    onFailure(e)
                }
        }

        /**
         * Dodaje do ulubionych podany przepis, jeśli przepisu nie ma w bazie,
         * dodaje też przepis.
         */
        fun addToFavourite(
            recipe: Recipe,
            onSuccess: (DocumentReference) -> Unit,
            onFailure: (Exception) -> Unit,
        ) {
            val db = Firebase.firestore

            val append = fun(document: DocumentReference) {
                db.document("users/" + User.name)
                    .update("favourites", FieldValue.arrayUnion(document.id))
                    .addOnSuccessListener {
                        Timber.d("Added to favourite")
                        onSuccess(document)
                    }
                    .addOnFailureListener {
                        Timber.d("Failure (adding to favourite {$it}")
                        onFailure(it)
                    }
            }

            checkIfExists(recipe, {
                if (it != null) {
                    append(it)
                } else {
                    addNewRecipe(recipe, append, onFailure)
                }
            }) {
                onFailure(it)
            }
        }

        /**
         * Dodaje do ulubionych podany przepis po jego id
         */
        fun addToFavourite(
            recipeId: String,
            onSuccess: () -> Unit,
            onFailure: (Exception) -> Unit,
        ) {
            val db = Firebase.firestore

            db.document("users/" + User.name)
                .update("favourites", FieldValue.arrayUnion(recipeId))
                .addOnSuccessListener {
                    Timber.d("Added to favourite")
                    onSuccess()
                }
                .addOnFailureListener {
                    Timber.d("Failure (adding to favourite {$it}")
                    onFailure(it)
                }

        }

        fun checkIfExists(
            recipe: Recipe,
            onSuccess: (DocumentReference?) -> Unit,
            onFailure: (Exception) -> Unit,
        ) {
            val db = Firebase.firestore

            var query = db.collection("recipes/").whereEqualTo("title", recipe.title)
                .whereEqualTo("owner", recipe.owner)

            if (recipe.documentId != "") {
                query = query.whereEqualTo(FieldPath.documentId(), recipe.documentId)
            }

            query
                .get()
                .addOnSuccessListener { documents ->
                    Timber.d("Documents: ${documents.size()}")
                    if (documents.isEmpty) {
                        onSuccess(null)
                    } else {
                        onSuccess(documents.documents[0].reference)
                    }

                }
                .addOnFailureListener { e ->
                    Timber.w("Error  $e")
                    onFailure(e)
                }
        }

        /**
         * Dodaje dzień do planu użytkownika, jeśli istnieje nadpisuje.
         */
        fun addNewDayPlan(
            day: Day, onSuccess: () -> Unit,
            onFailure: (Exception) -> Unit,
        ) {
            val db = Firebase.firestore

            db.collection("users/${User.name}/plans").document(day.id).set(day)
                .addOnSuccessListener {
                    Timber.d("Day added!")
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    Timber.w("Error adding day $e")
                    onFailure(e)
                }
        }

        /**
         * @param id -> data DD/MM/RRRR jako String
         */
        fun getPlanOnDay(
            id: String, onSuccess: (Day?) -> Unit,
            onFailure: (Exception) -> Unit,
        ) {
            val db = Firebase.firestore
            db.document("users/${User.name}/$id").get().addOnSuccessListener {
                Timber.d("OK: ${it.id}")
                onSuccess(it.toObject(Day::class.java))
            }.addOnFailureListener {
                Timber.d("Error $it")
                onFailure(it)
            }
        }

        fun getManyPlansOnDay(
            ids: List<String>,
            onSuccess: (List<Day>) -> Unit,
            onFailure: (Exception) -> Unit,
        ) {
            val db = Firebase.firestore
            db.collection("users/${User.name}/plans").whereIn(FieldPath.documentId(), ids).get()
                .addOnSuccessListener {
                    Timber.d("OK")
                    val days = ArrayList<Day>(it.size())
                    for (document in it) {
                        Timber.d(document.data.toString())
                        days.add(document.toObject(Day::class.java))
                    }
                    onSuccess(days)
                }.addOnFailureListener {
                    Timber.d("Error $it")
                    onFailure(it)
                }
        }

        fun getFavourites(onSuccess: (List<String>?) -> Unit, onFailure: (Exception) -> Unit) {
            val db = Firebase.firestore
            db.document("users/" + User.name).get()
                .addOnSuccessListener { documentSnapshot ->
                    Timber.d("OK: ${documentSnapshot.id}")
                    @Suppress("UNCHECKED_CAST")
                    onSuccess(documentSnapshot.get("favourites") as List<String>?)
                }
                .addOnFailureListener { e ->
                    Timber.w("Error adding document $e")
                    onFailure(e)
                }
        }

        fun getRecipe(
            documentId: String,
            onSuccess: (Recipe?) -> Unit,
            onFailure: (Exception) -> Unit,
        ) {
            val db = Firebase.firestore
            db.document("recipes/$documentId").get().addOnSuccessListener {
                Timber.d("OK: ${it.id}")
                onSuccess(it.toObject(Recipe::class.java))
            }.addOnFailureListener {
                Timber.d("Error $it")
                onFailure(it)
            }
        }

        fun getManyRecipes(
            ids: List<String>,
            onSuccess: (List<Recipe>) -> Unit,
            onFailure: (Exception) -> Unit,
        ) {
            val db = Firebase.firestore
            db.collection("recipes/").whereIn(FieldPath.documentId(), ids).get()
                .addOnSuccessListener {
                    Timber.d("OK")
                    val recipes = ArrayList<Recipe>(it.size())
                    for (document in it) {
                        recipes.add(document.toObject(Recipe::class.java))
                    }
                    onSuccess(recipes)
                }.addOnFailureListener {
                    Timber.d("Error $it")
                    onFailure(it)
                }
        }

        fun getManyRecipesFromAll(
            offset: DocumentSnapshot? = null,
            howMuch: Int = 50,
            onSuccess: (List<Recipe>, DocumentSnapshot?) -> Unit,
            onFailure: (Exception) -> Unit,
        ) {
            val db = Firebase.firestore
            var query =
                db.collection("recipes/").orderBy(FieldPath.documentId()).limit(howMuch.toLong())

            if (offset != null) {
                query = query.startAfter(offset)
            }

            query.get()
                .addOnSuccessListener {
                    Timber.d("OK")
                    val recipes = ArrayList<Recipe>(it.size())

                    for (document in it) {
                        recipes.add(document.toObject(Recipe::class.java))
                    }

                    if (!it.isEmpty) {
                        onSuccess(recipes, it.last())
                    } else {
                        onSuccess(recipes, null)
                    }

                }.addOnFailureListener {
                    Timber.d("Error $it")
                    onFailure(it)
                }

        }


        fun getManyRecipesWithFiltersFromAll(
            offset: DocumentSnapshot? = null,
            howMuch: Int = 50,
            onSuccess: (List<Recipe>, DocumentSnapshot?) -> Unit,
            onFailure: (Exception) -> Unit,
            diets: List<String>? = null,
            types: List<String>? = null,
            cuisines: List<String>? = null,
        ) {
            val db = Firebase.firestore
            var query =
                db.collection("recipes/").orderBy(FieldPath.documentId()).limit(howMuch.toLong())


            if (diets != null) {
                query = query.whereArrayContainsAny("diets", diets)
            }

            if (types != null) {
                query = query.whereArrayContainsAny("dishTypes", types)
            }

            if (cuisines != null) {
                query = query.whereArrayContainsAny("cuisines", cuisines)
            }

            if (offset != null) {
                query = query.startAfter(offset)
            }

            Timber.d("%s, %s, %s", diets.toString(), types.toString(), cuisines.toString())

            query.get()
                .addOnSuccessListener {
                    Timber.d("OK")
                    val recipes = ArrayList<Recipe>(it.size())

                    for (document in it) {
                        recipes.add(document.toObject(Recipe::class.java))
                    }

                    if (!it.isEmpty) {
                        onSuccess(recipes, it.last())
                    } else {
                        onSuccess(recipes, null)
                    }

                }.addOnFailureListener {
                    Timber.d("Error $it")
                    onFailure(it)
                }
        }


        fun addUserIfNotExist(
            onSuccess: (Boolean) -> Unit,
            onFailure: (Exception) -> Unit,
        ) {
            val db = Firebase.firestore

            val query = db.collection("users/").whereEqualTo(FieldPath.documentId(), User.name)

            query
                .get()
                .addOnSuccessListener { documents ->
                    Timber.d("Documents: ${documents.size()}")
                    if (documents.isEmpty) {
                        addUser()
                        onSuccess(true)
                    } else {
                        onSuccess(false)
                    }

                }
                .addOnFailureListener { e ->
                    Timber.w("Error  $e")
                    onFailure(e)
                }
        }


        fun addUser() {
            val db = Firebase.firestore
            val user = User(name = User.name, ArrayList())
            db.collection("users").document(User.name).set(user)
                .addOnSuccessListener {
                    Timber.d("User added!")
                }
                .addOnFailureListener { e ->
                    Timber.w("Error adding user $e")
                }
        }
    }

}
