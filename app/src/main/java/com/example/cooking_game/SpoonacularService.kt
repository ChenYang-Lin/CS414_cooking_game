package com.example.cooking_game

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface SpoonacularService {

    @GET("./food/ingredients/search/")
    fun getAllIngredients(
        @Query("apiKey") apiKey: String,
        @Query("query") query: String
    ) : Call<IngredientResponse>

    @GET("/food/ingredients/{ingredientID}/information/")
    fun getIngredientDetail(
        @Path("ingredientID") ingredientID: String,
        @Query("apiKey") apiKey: String,
        @Query("amount") amount: String
    ) : Call<IngredientDetail>

    @GET("./recipes/complexSearch/")
    fun getAllRecipes(
        @Query("apiKey") apiKey: String,
        @Query("query") query: String
    ) : Call<RecipeResponse>

    @GET("/recipes/{recipeID}/information/")
    fun getRecipeDetail(
        @Path("recipeID") recipeID: String,
        @Query("apiKey") apiKey: String,
        @Query("includeNutrition") includeNutrition: Boolean
    ) : Call<RecipeDetail>
}