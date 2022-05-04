package com.example.cooking_game

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.android.synthetic.main.activity_inventory.*
import kotlinx.android.synthetic.main.activity_shop.*
import kotlinx.android.synthetic.main.activity_shop.shop_recycler_view
import kotlinx.android.synthetic.main.activity_shop_checkout.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class InventoryActivity : AppCompatActivity() {
    private val BASE_URL = "https://api.spoonacular.com/"
    private val API_KEY = "d527da482f5f48be8629764a068e3ae1"
    private val TAG = "InventoryActivity"

    private var ingredientList = ArrayList<Ingredient>()
    private var adapter: MyInventoryRecyclerAdapter? = null

    private lateinit var fireBaseDb: FirebaseFirestore
    lateinit private var userID: String
    lateinit private var retrofit: Retrofit
    lateinit private var  spoonacularAPI: SpoonacularService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventory)

        fireBaseDb = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser
        // If currentUser is null, open the RegisterActivity
        if (currentUser == null) {
            startRegisterActivity()
        } else {
            userID = currentUser.uid.toString()
        }

        adapter = MyInventoryRecyclerAdapter(ingredientList)

        inventory_recycler_view.adapter = adapter
        inventory_recycler_view.layoutManager = GridLayoutManager(this, 3)

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        spoonacularAPI = retrofit.create(SpoonacularService::class.java)

        // clear list and replace with ingredients that user have
        ingredientList.clear()
        getUserIngredientsFromFireStore()

    }
    private fun startRegisterActivity() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun getUserIngredientsFromFireStore() {
        fireBaseDb.collection("users").document(userID).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // get data for current user from firestore
                    val userData = document.toObject<UserData>()

                    var ingredientInventory = userData?.ingredientInventory ?: HashMap<String, Int>()
                    var foodInventory = userData?.foodInventory ?: HashMap<String, Int>()

                    for ((ingredientID, ingredientQuantity) in ingredientInventory) {
                        spoonacularAPI.getIngredientDetail(ingredientID,API_KEY, "1").enqueue(object:
                            Callback<IngredientDetail> {
                            override fun onResponse(call: Call<IngredientDetail>, response: Response<IngredientDetail>) {
                                Log.d(TAG, "onResponse: $response")

                                val body = response.body()
                                if (body == null) {
                                    Log.d(TAG, "Valid response was not received")
                                    return
                                }
                                val ingredientElement = Ingredient(
                                    body.id,
                                    body.name,
                                    body.image
                                )

                                ingredientList.add(ingredientElement)
                                adapter?.notifyDataSetChanged()
                            }

                            override fun onFailure(call: Call<IngredientDetail>, t: Throwable) {
                                Log.d(TAG, "onFailure: $t")
                            }
                        })
                    }
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
}