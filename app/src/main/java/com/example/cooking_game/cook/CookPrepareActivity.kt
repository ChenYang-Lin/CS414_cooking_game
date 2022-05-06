package com.example.cooking_game.cook

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.cooking_game.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.android.synthetic.main.activity_cook_prepare.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.cooking_game.MainActivity
import kotlin.math.ceil


class CookPrepareActivity : AppCompatActivity() {
    private val BASE_URL = "https://api.spoonacular.com/"
    private val API_KEY = "d527da482f5f48be8629764a068e3ae1"
//    private val API_KEY = "00dff5c2b2574ed1bb71971332ce5f3a"
    private val TAG = "CookPrepareActivity"

    private var ingredientInventory = HashMap<String, IngredientData>()
    private var requiredIngredientsList = ArrayList<ExtendedIngredients>()
    private var adapter: MyPrepareRecyclerAdapter? = null

    private lateinit var fireBaseDb: FirebaseFirestore
    lateinit private var retrofit: Retrofit
    lateinit private var spoonacularAPI: SpoonacularService

    lateinit private var recipeID: String
    lateinit private var userID: String

    private var quantity = 1
    private var price = 0.0f
    private var name = ""
    private var image = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cook_prepare)

        recipeID = intent.getStringExtra("id").toString() // id of selected ingredient from shop activity

        adapter = MyPrepareRecyclerAdapter(requiredIngredientsList)

        required_ingredients_recycler_view.adapter = adapter
        required_ingredients_recycler_view.layoutManager = GridLayoutManager(this, 3)

        fireBaseDb = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser
        // If currentUser is null, open the RegisterActivity
        if (currentUser == null) {
            startRegisterActivity()
        } else {
            userID = currentUser.uid.toString()
        }

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        spoonacularAPI = retrofit.create(SpoonacularService::class.java)

        spoonacularAPI.getRecipeDetail(recipeID, API_KEY, false).enqueue(object:
            Callback<RecipeDetail> {
            override fun onResponse(call: Call<RecipeDetail>, response: Response<RecipeDetail>) {
                Log.d(TAG, "onResponse: $response")
                Log.d(TAG, "Data: ${response.body()}")

                val body = response.body()
                if (body == null) {
                    Log.d(TAG, "Valid response was not received")
                    return
                }

                name = body.title
                image = body.image
                quantity = body.servings
                price = body.pricePerServing
//                readyInMinutes = body.readyInMinutes
//                extendedIngredients = body.extendedIngredients as ArrayList<ExtendedIngredients>

                // update adapter
                requiredIngredientsList.addAll(body.extendedIngredients)
                adapter?.notifyDataSetChanged()

                updateRequiredIngredientsView()


                // render activity layout
                cook_prepare_recipe_name.text = name
                Glide.with(applicationContext)
                    .load(image)
                    .placeholder(R.drawable.ic_baseline_fastfood_24_gray)
                    .into(cook_prepare_image)
                ready_in_minutes.text = body.readyInMinutes.toString()
                servings.text = quantity.toString()
            }

            override fun onFailure(call: Call<RecipeDetail>, t: Throwable) {
                Log.d(TAG, "onFailure: $t")
            }
        })
    }

    // update data when user returned
    override fun onResume() {
        super.onResume()
        updateRequiredIngredientsView()
    }

    private fun startRegisterActivity() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun updateRequiredIngredientsView() {
        // get currently owned ingredients from firestore ingredientsInventory and update the list
        fireBaseDb.collection("users").document(userID).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // get data for current user from firestore
                    val userData = document.toObject<UserData>()
                    ingredientInventory = userData?.ingredientInventory ?: HashMap<String, IngredientData>()

                    val itr = requiredIngredientsList.listIterator()
                    while (itr.hasNext()) {
                        var currentIngredient = itr.next()
                        var id = currentIngredient.id
                        if (id == null || id == "") {
                            itr.remove()
                        }
                        currentIngredient.hold = ingredientInventory[id]?.quantity ?: 0
                        adapter?.notifyDataSetChanged()
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


    fun startCooking(view: View) {
        // check to see if user have all required ingredients for that meal
        if(!gotAllIngredients()) {
            // alert user missing ingredients
            return;
        }
        // update firestore data, take off used ingredients and add meal
        fireBaseDb.collection("users").document(userID).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val users = fireBaseDb.collection("users")

                    // get data for current user from firestore
                    val userData = document.toObject<UserData>()
                    var balance = userData?.balance
                    var newIngredientInventory = userData?.ingredientInventory ?: HashMap<String, IngredientData>()
                    var newFoodInventory = userData?.foodInventory ?: HashMap<String, FoodData>()

                    // update ingredients
                    for (currentIngredient in requiredIngredientsList) {
                        var hold = ingredientInventory[currentIngredient.id]?.quantity ?: 0
                        var required = ceil(currentIngredient.amount ?: 1.0f).toInt()
                        hold -= required
                        newIngredientInventory[currentIngredient.id]?.quantity = hold
                        if (hold <= 0) {
                            newIngredientInventory.remove(currentIngredient.id)
                        }
                    }

                    // update meal
                    val hold = newFoodInventory[recipeID]?.quantity ?: 0
                    newFoodInventory[recipeID] = FoodData(
                        quantity + hold,
                        price,
                        name,
                        image,
                    )

                    // update user date
                    val user = UserData(
                        balance,
                        newIngredientInventory,
                        newFoodInventory,
                    )
                    users.document(userID).set(user)
                    // user does not exist
                } else {
                    Log.d(TAG, "user data: null")
                    startRegisterActivity()
                }
            }
            .addOnCompleteListener{
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent)
            }
            .addOnFailureListener {
                Log.d(TAG, "Error getting documents")
            }
    }

    private fun gotAllIngredients(): Boolean {
        for (currentIngredient in requiredIngredientsList) {
            var hold = ingredientInventory[currentIngredient.id]?.quantity ?: 0
            var required = ceil(currentIngredient.amount ?: 1.0f).toInt()
            if (hold < required) {
                return false
            }
        }
        return true
    }

}