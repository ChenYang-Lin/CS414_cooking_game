package com.example.cooking_game.inventory

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.GridLayoutManager
import com.example.cooking_game.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.android.synthetic.main.activity_inventory.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class InventoryActivity : AppCompatActivity() {
    private val BASE_URL = "https://api.spoonacular.com/"
//    private val API_KEY = "d527da482f5f48be8629764a068e3ae1"
    private val API_KEY = "00dff5c2b2574ed1bb71971332ce5f3a"
    private val TAG = "InventoryActivity"


    private var ingredientList = ArrayList<Ingredient>()
    private var foodList = ArrayList<Recipe>()
    private var adapterIngredients: MyInventoryIngredientsRecyclerAdapter? = null
    private var adapterFood: MyInventoryFoodRecyclerAdapter? = null

    private lateinit var fireBaseDb: FirebaseFirestore
    lateinit private var userID: String
    lateinit private var retrofit: Retrofit
    lateinit private var spoonacularAPI: SpoonacularService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventory)

        fireBaseDb = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser
        // If currentUser is null, open the RegisterActivity
        if (currentUser == null) {
            startRegisterActivity()
        } else {
            userID = currentUser.uid.toString()
        }

        adapterIngredients = MyInventoryIngredientsRecyclerAdapter(ingredientList)
        adapterFood = MyInventoryFoodRecyclerAdapter(foodList)

        inventory_ingredients_recycler_view.adapter = adapterIngredients
        inventory_ingredients_recycler_view.layoutManager = GridLayoutManager(this, 3)
        inventory_food_recycler_view.adapter = adapterFood
        inventory_food_recycler_view.layoutManager = GridLayoutManager(this, 3)

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        spoonacularAPI = retrofit.create(SpoonacularService::class.java)

        // clear list and replace with ingredients/food that user have
        ingredientList.clear()
        foodList.clear()
        getUserInventoryFromFireStore()

    }
    private fun startRegisterActivity() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun getUserInventoryFromFireStore() {
        fireBaseDb.collection("users").document(userID).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // get data for current user from firestore
                    val userData = document.toObject<UserData>()

                    var ingredientInventory = userData?.ingredientInventory ?: HashMap<String, IngredientData>()
                    var foodInventory = userData?.foodInventory ?: HashMap<String, FoodData>()

                    // Ingredients
                    for ((ingredientID, ingredientData) in ingredientInventory) {
                        val ingredientElement = Ingredient(
                            ingredientID,
                            ingredientData.name ?: "",
                            ingredientData.image ?: "",
                        )

                        ingredientList.add(ingredientElement)
                        adapterIngredients?.notifyDataSetChanged()
                    }
                    // Food
                    for ((foodID, foodData) in foodInventory) {
                        val foodElement = Recipe(
                            foodID,
                            foodData.name ?: "",
                            foodData.image ?: "",
                        )

                        foodList.add(foodElement)
                        adapterFood?.notifyDataSetChanged()
                    }
                // user does not exist
                } else {
                    Log.d(TAG, "user data: null")
                    startRegisterActivity()
                }
            }
            .addOnFailureListener {
                Log.d(TAG, "Error getting documents")
            }
    }
}