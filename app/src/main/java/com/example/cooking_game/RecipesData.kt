package com.example.cooking_game

data class Ingredient(
    val id: String,
    val name: String,
    val image: String,
)

data class IngredientResponse(
    val results: List<Ingredient>
)

data class IngredientDetail(
    val id: String,
    val name: String,
    val estimatedCost: Cost,
    val image: String,
    val unit: String,
)

data class Cost(
    val value: Float
)

data class Recipe(
    val id: String,
    val title: String,
    val image: String,
)

data class RecipeResponse(
    val results: List<Recipe>
)

data class RecipeDetail(
    val id: String,
    val title: String,
    val image: String,
    val servings: Int,
    val pricePerServing: Float,
    val readyInMinutes: Int,
    val extendedIngredients: List<ExtendedIngredients>
)

data class ExtendedIngredients(
    val id: String? = null,
    val image: String? = null,
    val name: String? = null,
    var hold: Int = 0,
    val amount: Float? = null,
    val unit: String? = null,
)