package com.example.cooking_game

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_shop_checkout.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ShopCheckoutActivity : AppCompatActivity() {
    private val BASE_URL = "https://api.spoonacular.com/"
    private val API_KEY = "d527da482f5f48be8629764a068e3ae1"
    private val TAG = "ShopCheckoutActivity"

    private lateinit var fireBaseDb: FirebaseFirestore
    lateinit private var retrofit: Retrofit
    lateinit private var  spoonacularAPI: SpoonacularService

    private var quantity = 1
    private var total: Float = 0.0F
    private var unitPrice: Float = 0.0F

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop_checkout)

        val id = intent.getStringExtra("id") // id of selected ingredient from shop activity

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL + "food/ingredients/" + id +"/information/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        spoonacularAPI = retrofit.create(SpoonacularService::class.java)

        spoonacularAPI.getIngredientDetail(API_KEY, "1").enqueue(object:
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
                // render activity layout
                checkout_item_name.text = body.name
                Glide.with(applicationContext)
                    .load("https://spoonacular.com/cdn/ingredients_250x250/" + body.image)
                    .placeholder(R.drawable.ic_baseline_fastfood_24_gray)
                    .into(checkout_item_icon)
                checkout_item_total.text = "$" + String.format("%.2f", unitPrice)
            }

            override fun onFailure(call: Call<IngredientDetail>, t: Throwable) {
                Log.d(TAG, "onFailure: $t")
            }
        })
    }

    fun checkout(view: View) {
        finish()
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
        total += num * unitPrice
        checkout_quantity.text = quantity.toString()
        checkout_item_total.text = "$" + String.format("%.2f", total)
    }
}