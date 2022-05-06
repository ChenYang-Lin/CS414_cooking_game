package com.example.cooking_game

import com.google.firebase.firestore.DocumentReference
import java.security.Key

//data class UserData(
//    val balance: Int? = null,
//    val ingredientInventory: List<IngredientElement>? = null,
//    val foodInventory: List<FoodElement>? = null,
//)
data class UserData(
    val balance: Float? = null,
    val ingredientInventory: HashMap<String, IngredientData>? = null,
    val foodInventory: HashMap<String, FoodData>? = null,
)

data class IngredientData(
    var quantity: Int? = null,
    var price: Float? = null,
    val name: String? = null,
    val image: String? = null
)

data class FoodData(
    var quantity: Int? = null,
    var price: Float? = null,
    val name: String? = null,
    val image: String? = null
)
