package com.example.cooking_game.cook

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.recyclerview.widget.GridLayoutManager
import com.example.cooking_game.*
import kotlinx.android.synthetic.main.activity_cook.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.content.Intent




class CookActivity : AppCompatActivity() {
    private val BASE_URL = "https://api.spoonacular.com/"
//    private val API_KEY = "d527da482f5f48be8629764a068e3ae1"
    private val API_KEY = "00dff5c2b2574ed1bb71971332ce5f3a"
    private val TAG = "CookActivity"

    private var recipeList = ArrayList<Recipe>()
    private var adapter: MyRecipeRecyclerAdapter? = null
    lateinit private var retrofit: Retrofit
    lateinit private var  spoonacularAPI: SpoonacularService

    private var selectedStove: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cook)


        selectedStove = intent.getIntExtra("selectedStove", 0)

        adapter = MyRecipeRecyclerAdapter(recipeList, selectedStove)

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
        val query = cook_search_input.text.toString()
        cook_search_input.text.clear()
        cook_search_btn.hideKeyboard()
        searchRecipes(query)
    }

    fun View.hideKeyboard() {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
    }

    fun back(view: View) {
        finish()
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