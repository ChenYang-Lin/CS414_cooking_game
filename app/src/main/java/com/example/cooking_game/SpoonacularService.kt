package com.example.cooking_game

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface SpoonacularService {

    @GET("./recipes/findByIngredients")
    fun getRecipeList(
        @Query("apiKey") apiKey: String,
        @Query("ingredients") ingredients: ArrayList<String>
    ) : Call<List<RecipesData>>

    @GET("./food/ingredients/search")
    fun getAllIngredients(
        @Query("apiKey") apiKey: String,
        @Query("query") query: String
    ) : Call<IngredientResponse>

    @GET("./recipes/complexSearch")
    fun getAllRecipes(
        @Query("apiKey") apiKey: String,
        @Query("query") query: String
    ) : Call<RecipeResponse>

    @GET("/food/ingredients/{ingredientID}/information/")
    fun getIngredientDetail(
        @Path("ingredientID") ingredientID: String,
        @Query("apiKey") apiKey: String,
        @Query("amount") amount: String
    ) : Call<IngredientDetail>

}