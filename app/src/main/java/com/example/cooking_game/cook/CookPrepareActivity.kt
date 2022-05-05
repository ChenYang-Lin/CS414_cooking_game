package com.example.cooking_game.cook

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.example.cooking_game.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_cook_prepare.*
import kotlinx.android.synthetic.main.activity_shop_checkout.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CookPrepareActivity : AppCompatActivity() {
    private val BASE_URL = "https://api.spoonacular.com/"
    //    private val API_KEY = "d527da482f5f48be8629764a068e3ae1"
    private val API_KEY = "00dff5c2b2574ed1bb71971332ce5f3a"
    private val TAG = "CookPrepareActivity"

    private lateinit var fireBaseDb: FirebaseFirestore
    lateinit private var retrofit: Retrofit
    lateinit private var spoonacularAPI: SpoonacularService

    lateinit private var recipeID: String
    lateinit private var userID: String

    private var title = ""
    private var image = ""
    private var servings: Int = 1
    private var pricePerServing: Float = 0.0f
    private var readyInMinutes: Int = 0
    private var extendedIngredients = ArrayList<ExtendedIngredients>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cook_prepare)

        recipeID = intent.getStringExtra("id").toString() // id of selected ingredient from shop activity

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

                val body = response.body()
                if (body == null) {
                    Log.d(TAG, "Valid response was not received")
                    return
                }

                title = body.title
                image = body.image
                servings = body.servings
                pricePerServing = body.pricePerServing
                readyInMinutes = body.readyInMinutes
                extendedIngredients = body.extendedIngredients as ArrayList<ExtendedIngredients>


                // render activity layout
                cook_prepare_recipe_name.text = title
                Glide.with(applicationContext)
                    .load(image)
                    .placeholder(R.drawable.ic_baseline_fastfood_24_gray)
                    .into(cook_prepare_image)
                ready_in_minutes.text = readyInMinutes.toString()
//                checkout_item_total.text = "$" + String.format("%.2f", unitPrice)
            }

            override fun onFailure(call: Call<RecipeDetail>, t: Throwable) {
                Log.d(TAG, "onFailure: $t")
            }
        })
    }

    fun startCooking(view: View) {
        finish()
    }

    private fun startRegisterActivity() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
        finish()
    }

}