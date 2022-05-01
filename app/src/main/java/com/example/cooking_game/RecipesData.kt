package com.example.cooking_game

data class RecipesData (
    val id: Int,
    val title: String,
    val image: String,
    val usedIngredientCount: Int,
    val missedIngredientCount: Int,
    val missedIngredients: List<Ingredient>,
    val usedIngredients: List<Ingredient>,
)

data class Ingredient(
    val id: Int,
//    val amount: Float,
    val name: String,
    val image: String,
)

data class IngredientResponse(
    val results: List<Ingredient>
)

data class IngredientDetail(
    val id: Int,
    val name: String,
    val estimatedCost: Cost,
    val image: String,
)

data class Cost(
    val value: Float
)