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
    val ingredientInventory: HashMap<String, Int>? = null,
    val foodInventory: HashMap<String,Int>? = null,
)



