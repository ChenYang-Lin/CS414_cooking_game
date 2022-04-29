package com.example.cooking_game

data class UserData(
    val balance: Int? = null,
    val ingredientInventory: List<IngredientElement>? = null,
    val foodInventory: List <FoodElement>? = null,
)

data class IngredientElement(
    val id: Int,
    val quantity: Int,
)

data class FoodElement(
    val id: Int,
    val quantity: Int,
)



