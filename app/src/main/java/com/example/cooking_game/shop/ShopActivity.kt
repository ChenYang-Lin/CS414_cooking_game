package com.example.cooking_game.shop

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.cooking_game.*
import kotlinx.android.synthetic.main.activity_shop.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class ShopActivity : AppCompatActivity() {
    private val BASE_URL = "https://api.spoonacular.com/"
//    private val API_KEY = "d527da482f5f48be8629764a068e3ae1"
//    private val API_KEY = "00dff5c2b2574ed1bb71971332ce5f3a"
    private val API_KEY = "4f8651a8632a4f77acadea5f58162507"
    private val TAG = "ShopActivity"

    private var ingredientList = ArrayList<Ingredient>()
    private var adapter: MyIngredientRecyclerAdapter? = null
    lateinit private var retrofit: Retrofit
    lateinit private var  spoonacularAPI: SpoonacularService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop)

        adapter = MyIngredientRecyclerAdapter(ingredientList)

        shop_recycler_view.adapter = adapter
        shop_recycler_view.layoutManager = GridLayoutManager(this, 3)
//        shop_recycler_view.layoutManager = LinearLayoutManager(this)



        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        spoonacularAPI = retrofit.create(SpoonacularService::class.java)

        searchIngredients("cheese")
    }

    fun backToHome(view: View) {
//        val intent = Intent(this, MainActivity::class.java)
        finish()
    }

    fun search(view: View) {
        val query = shop_search_input.text.toString()
        shop_search_input.text.clear()
        shop_search_btn.hideKeyboard()
        searchIngredients(query)
    }

    fun View.hideKeyboard() {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
    }

    private fun searchIngredients(query: String) {
        // clear list
        ingredientList.clear()

        spoonacularAPI.getAllIngredients(API_KEY, query).enqueue(object: Callback<IngredientResponse> {
            override fun onResponse(call: Call<IngredientResponse>, response: Response<IngredientResponse>) {
                Log.d(TAG, "onResponse: $response")

                val body = response.body()
                if (body == null) {
                    Log.d(TAG, "Valid response was nto received")
                    return
                }
//                Log.d(TAG, "${body.results[0].id}")

                ingredientList.addAll(body.results)
                adapter?.notifyDataSetChanged()
            }

            override fun onFailure(call: Call<IngredientResponse>, t: Throwable) {
                Log.d(TAG, "onFailure: $t")
            }
        })
    }
}