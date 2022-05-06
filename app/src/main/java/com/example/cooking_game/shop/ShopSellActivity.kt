package com.example.cooking_game.shop

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.example.cooking_game.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.android.synthetic.main.activity_shop_checkout.*
import kotlinx.android.synthetic.main.activity_shop_checkout.checkout_item_total
import kotlinx.android.synthetic.main.activity_shop_checkout.checkout_quantity
import kotlinx.android.synthetic.main.activity_shop_sell.*
import retrofit2.Retrofit

class ShopSellActivity : AppCompatActivity() {
    private val TAG = "ShopSellActivity"

    private lateinit var fireBaseDb: FirebaseFirestore
    lateinit private var userID: String

    lateinit private var id: String
    lateinit private var type: String


    private var quantity = 1
    private var total: Float = 0.0F
    private var unitPrice: Float = 0.0F
    private var imageURL = ""
    private var name = ""
    private var stock = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop_sell)

        id = intent.getStringExtra("id").toString()
        type = intent.getStringExtra("type").toString()

        val currentUser = FirebaseAuth.getInstance().currentUser
        // If currentUser is null, open the RegisterActivity
        if (currentUser == null) {
            startRegisterActivity()
        } else {
            userID = currentUser.uid.toString()
        }

        val fireBaseDb = FirebaseFirestore.getInstance()
        fireBaseDb.collection("users").document(userID).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // users collection reference
                    val users = fireBaseDb.collection("users")

                    // get data for current user from firestore
                    val userData = document.toObject<UserData>()
                    var balance = userData?.balance
                    var ingredientInventory = userData?.ingredientInventory ?: HashMap<String, IngredientData>()
                    var foodInventory = userData?.foodInventory ?: HashMap<String, FoodData>()


                    if (type == "ingredient") {
                        unitPrice = ingredientInventory[id]?.price ?: 0.0f
                        total = unitPrice
                        imageURL = ingredientInventory[id]?.image ?: ""
                        name = ingredientInventory[id]?.name ?: "name"
                        stock = ingredientInventory[id]?.quantity ?: 1
                    } else {
                        unitPrice = foodInventory[id]?.price ?: 0.0f
                        total = unitPrice
                        imageURL = foodInventory[id]?.image ?: ""
                        name = foodInventory[id]?.name ?: "name"
                        stock = foodInventory[id]?.quantity ?: 1
                    }

                    // render activity layout
                    sell_item_name.text = name
                    Glide.with(applicationContext)
                        .load(imageURL)
                        .placeholder(R.drawable.ic_baseline_fastfood_24_gray)
                        .into(sell_item_icon)
                    sell_item_total.text = "$" + String.format("%.2f", total)

                    // user does not exist
                } else {
                    Log.d(TAG, "user data: null")
                    startRegisterActivity()
                }
            }
            .addOnFailureListener {
                Log.d(TAG, "Error getting documents")
            }
    }

    private fun startRegisterActivity() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun sell(view: View) {
        val fireBaseDb = FirebaseFirestore.getInstance()
        fireBaseDb.collection("users").document(userID).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // users collection reference
                    val users = fireBaseDb.collection("users")

                    // get data for current user from firestore
                    val userData = document.toObject<UserData>()
                    // Log.d(TAG, "$userData")
                    var balance = userData?.balance
                    var ingredientInventory = userData?.ingredientInventory ?: HashMap<String, IngredientData>()
                    var foodInventory = userData?.foodInventory ?: HashMap<String, FoodData>()

                    // if there is no balance, something wrong, exit
                    if (balance == null) {
                        return@addOnSuccessListener
                    }

                    var currentStock = stock - quantity
                    if (type == "ingredient") {
                        ingredientInventory[id]?.quantity = currentStock
                        if (currentStock <= 0) {
                            ingredientInventory.remove(id)
                        }
                    } else {
                        foodInventory[id]?.quantity = currentStock
                        if (currentStock <= 0) {
                            foodInventory.remove(id)
                        }
                    }


                    // update user date
                    val user = UserData(
                        balance + total,
                        ingredientInventory,
                        foodInventory,
                    )
                    users.document(userID).set(user)
                    // user does not exist
                } else {
                    Log.d(TAG, "user data: null")
                    startRegisterActivity()
                }
            }
            .addOnCompleteListener{
                finish()
            }
            .addOnFailureListener {
                Log.d(TAG, "Error getting documents")
            }
    }

    fun addOne(view: View) {
        if (quantity >= stock)
            return
        updateQuantity(1)
    }

    fun removeOne(view: View) {
        if (quantity <= 1)
            return
        updateQuantity(-1)
    }

    private fun updateQuantity(num: Int) {
        quantity += num
        total = quantity * unitPrice
        sell_quantity.text = quantity.toString()
        sell_item_total.text = "$" + String.format("%.2f", total)
    }
}