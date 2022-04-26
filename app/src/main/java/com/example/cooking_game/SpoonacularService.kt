package com.example.cooking_game

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface SpoonacularService {

    @GET("./findByIngredients")
    fun getRecipeList(
        @Query("apiKey") apiKey: String,
        @Query("ingredients") ingredients: ArrayList<String>) : Call<List<RecipesData>>
}