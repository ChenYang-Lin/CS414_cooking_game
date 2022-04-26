package com.example.cooking_game

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    private val BASE_URL = "https://api.spoonacular.com/recipes/"
    private val API_KEY = "d527da482f5f48be8629764a068e3ae1"
    private val TAG = "MainActivity"

    private var recipeList = ArrayList<RecipesData>()
//    private val adapter
    lateinit private var retrofit: Retrofit
    lateinit private var spoonacularAPI: SpoonacularService

    private var list = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        spoonacularAPI = retrofit.create(SpoonacularService::class.java)

        list.add("apple")
        list.add("flour")
        list.add("sugar")
        spoonacularAPI.getRecipeList(API_KEY, list).enqueue(object: Callback<List<RecipesData>> {
            override fun onResponse(call: Call<List<RecipesData>>, response: Response<List<RecipesData>>) {
                Log.d(TAG, "onResponse: $response")

                val body = response.body()
                if (body == null) {
                    Log.d(TAG, "Valid response was nto received")
                    return
                }
                Log.d(TAG, "${body.get(0).id}")
                Log.d(TAG, "${body.get(0).title}")
                Log.d(TAG, "${body.get(0).image}")

                Log.d(TAG, "${body.get(1).id}")
                Log.d(TAG, "${body.get(1).title}")
                Log.d(TAG, "${body.get(1).image}")
            }

            override fun onFailure(call: Call<List<RecipesData>>, t: Throwable) {
                Log.d(TAG, "onFailure: $t")
            }
        })
    }
}