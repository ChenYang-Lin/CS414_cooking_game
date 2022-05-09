package com.example.cooking_game

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import java.security.Key
import java.security.Timestamp

//data class UserData(
//    val balance: Int? = null,
//    val ingredientInventory: List<IngredientElement>? = null,
//    val foodInventory: List<FoodElement>? = null,
//)
data class UserData(
    val balance: Float? = null,
    val ingredientInventory: HashMap<String, IngredientData>? = null,
    val foodInventory: HashMap<String, FoodData>? = null,
    val stoves: List<CookingProgress>? = null,
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

data class CookingProgress(
    var status: String? = "empty",
    var id: String? = null,
    var startedTime: Long? = null,
    var completeTime: Long? = null,
    var quantity: Int? = null,
    var price: Float? = null,
    var name: String? = null,
    var image: String? = null
)