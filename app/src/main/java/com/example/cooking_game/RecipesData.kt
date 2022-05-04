package com.example.cooking_game

data class RecipesData (
    val id: String,
    val title: String,
    val image: String,
    val usedIngredientCount: Int,
    val missedIngredientCount: Int,
    val missedIngredients: List<Ingredient>,
    val usedIngredients: List<Ingredient>,
)

data class Ingredient(
    val id: String,
//    val amount: Float,
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