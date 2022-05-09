package com.example.cooking_game.shop

import android.app.AlertDialog
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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ShopCheckoutActivity : AppCompatActivity() {
    private val BASE_URL = "https://api.spoonacular.com/"
//    private val API_KEY = "d527da482f5f48be8629764a068e3ae1"
//    private val API_KEY = "00dff5c2b2574ed1bb71971332ce5f3a"
    private val API_KEY = "4f8651a8632a4f77acadea5f58162507"
    private val TAG = "ShopCheckoutActivity"

    private lateinit var fireBaseDb: FirebaseFirestore
    lateinit private var retrofit: Retrofit
    lateinit private var spoonacularAPI: SpoonacularService

    lateinit private var ingredientID: String
    lateinit private var userID: String


    private var quantity = 1
    private var total: Float = 0.0F
    private var unitPrice: Float = 0.0F
    private var imageURL = ""
    private var name = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop_checkout)

        Log.d(TAG, "Create Activity: ShopCheckoutActivity ")

        ingredientID = intent.getStringExtra("id").toString() // id of selected ingredient from shop activity

        val currentUser = FirebaseAuth.getInstance().currentUser
        // If currentUser is null, open the RegisterActivity
        if (currentUser == null) {
            startRegisterActivity()
        } else {
            userID = currentUser.uid.toString()
        }

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        spoonacularAPI = retrofit.create(SpoonacularService::class.java)

        spoonacularAPI.getIngredientDetail(ingredientID, API_KEY, "1").enqueue(object:
            Callback<IngredientDetail> {
            override fun onResponse(call: Call<IngredientDetail>, response: Response<IngredientDetail>) {
                Log.d(TAG, "onResponse: $response")

                val body = response.body()
                if (body == null) {
                    Log.d(TAG, "Valid response was not received")
                    return
                }
                unitPrice = body.estimatedCost.value / 100
                total = unitPrice
                imageURL = "https://spoonacular.com/cdn/ingredients_250x250/" + body.image
                name = body.name
                // render activity layout
                checkout_item_name.text = name
                Glide.with(applicationContext)
                    .load(imageURL)
                    .placeholder(R.drawable.ic_baseline_fastfood_24_gray)
                    .into(checkout_item_icon)
                checkout_item_total.text = "$" + String.format("%.2f", unitPrice)
            }

            override fun onFailure(call: Call<IngredientDetail>, t: Throwable) {
                Log.d(TAG, "onFailure: $t")
            }
        })
    }

    private fun startRegisterActivity() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun checkout(view: View) {

        val builder = AlertDialog.Builder(this)
        val fireBaseDb = FirebaseFirestore.getInstance()
        builder.setTitle("Buy Confirmation")
        builder.setMessage("Are you sure that you want to buy this product?")
        builder.setPositiveButton("YES"){ dialog, which ->

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
                        var stoves = userData?.stoves

                        // if there is no balance, something wrong, exit
                        if (balance == null) {
                            return@addOnSuccessListener
                        }

                        if (balance >= total) {
                            // new quantity for current ingredient
                            val hold = ingredientInventory[ingredientID]?.quantity ?: 0
                            ingredientInventory[ingredientID] = IngredientData(
                                quantity + hold,
                                unitPrice,
                                name,
                                imageURL,
                            )

                            // update user date
                            val user = UserData(
                                balance - total,
                                ingredientInventory,
                                foodInventory,
                                stoves
                            )
                            users.document(userID).set(user)
                        }
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
        builder.setNeutralButton("Cancel"){dialog, which ->
            return@setNeutralButton
        }
        val dialog = builder.create()
        dialog.show()
    }

    fun addOne(view: View) {
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
        checkout_quantity.text = quantity.toString()
        checkout_item_total.text = "$" + String.format("%.2f", total)
    }
}