package com.example.cooking_game

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface SpoonacularService {

    @GET("./recipes/findByIngredients")
    fun getRecipeList(
        @Query("apiKey") apiKey: String,
        @Query("ingredients") ingredients: ArrayList<String>) : Call<List<RecipesData>>

    @GET("./food/ingredients/search")
    fun getAllIngredients(
        @Query("apiKey") apiKey: String,
        @Query("query") query: String) : Call<IngredientResponse>

    @GET(".")
    fun getIngredientDetail(
        @Query("apiKey") apiKey: String,
        @Query("amount") amount: String) : Call<IngredientDetail>

}