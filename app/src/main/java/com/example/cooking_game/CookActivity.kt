package com.example.cooking_game

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.activity_cook.*
import kotlinx.android.synthetic.main.activity_shop.*
import kotlinx.android.synthetic.main.activity_shop.shop_recycler_view
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CookActivity : AppCompatActivity() {
    private val BASE_URL = "https://api.spoonacular.com/"
    private val API_KEY = "d527da482f5f48be8629764a068e3ae1"
    private val TAG = "CookActivity"

    private var recipeList = ArrayList<Recipe>()
    private var adapter: MyRecipeRecyclerAdapter? = null
    lateinit private var retrofit: Retrofit
    lateinit private var  spoonacularAPI: SpoonacularService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cook)

        adapter = MyRecipeRecyclerAdapter(recipeList)

        cook_recycler_view.adapter = adapter
        cook_recycler_view.layoutManager = GridLayoutManager(this, 3)

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        spoonacularAPI = retrofit.create(SpoonacularService::class.java)

        searchRecipes("pasta")
    }

    fun search(view: View) {

    }

    private fun searchRecipes(query: String) {
        // clear list
        recipeList.clear()

        spoonacularAPI.getAllRecipes(API_KEY, query).enqueue(object:
            Callback<RecipeResponse> {
            override fun onResponse(call: Call<RecipeResponse>, response: Response<RecipeResponse>) {
                Log.d(TAG, "onResponse: $response")

                val body = response.body()
                if (body == null) {
                    Log.d(TAG, "Valid response was nto received")
                    return
                }

                recipeList.addAll(body.results)
                adapter?.notifyDataSetChanged()
            }

            override fun onFailure(call: Call<RecipeResponse>, t: Throwable) {
                Log.d(TAG, "onFailure: $t")
            }
        })
    }
}