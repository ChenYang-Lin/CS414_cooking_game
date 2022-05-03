package com.example.cooking_game

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    private val BASE_URL = "https://api.spoonacular.com/"
    private val API_KEY = "d527da482f5f48be8629764a068e3ae1"
    private val TAG = "MainActivity"

    private lateinit var fireBaseDb: FirebaseFirestore

    private var recipeList = ArrayList<RecipesData>()
//    private val adapter
    lateinit private var retrofit: Retrofit
    lateinit private var spoonacularAPI: SpoonacularService

    private var list = ArrayList<String>()

    lateinit var USER: UserData
    lateinit var userID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        spoonacularAPI = retrofit.create(SpoonacularService::class.java)

        // Get a Cloud Firestore instance
        fireBaseDb = FirebaseFirestore.getInstance()

        // #### Authentication using FirebaseAuth #####

        // Get instance of the FirebaseAuth
        val currentUser = FirebaseAuth.getInstance().currentUser

        // If currentUser is null, open the RegisterActivity
        if (currentUser == null) {
            startRegisterActivity()
        } else {
            user_name.text = currentUser.displayName
            user_email.text = currentUser.email
            //currentUser.uid
            Glide.with(this)
                .load(currentUser.photoUrl)
                .placeholder(R.drawable.ic_baseline_person_24)
                .circleCrop()
                .into(user_image)
            userID = currentUser.uid

            initUserData(currentUser.uid);
        }
    }

    // get newest data from firestore
    override fun onResume() {
        super.onResume()
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            startRegisterActivity()
        } else {
            renderUserProfile(currentUser.uid)
        }
    }



    // An helper function to start our RegisterActivity
    private fun startRegisterActivity() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
        // Make sure to call finish(), otherwise the user would be able to go back to the MainActivity
        finish()
    }

    fun logout(view: View) {
        AuthUI.getInstance().signOut(this)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // After logout, start the RegisterActivity again
                    startRegisterActivity()
                }
                else {
                    Log.e(TAG, "Task is not successful:${task.exception}")
                }
            }
    }

    fun openShop(view: View) {
        val intent = Intent(this, ShopActivity::class.java)
        startActivity(intent)
    }

    fun openInventory(view: View) {
        val intent = Intent(this, InventoryActivity::class.java)
        startActivity(intent)
    }

    private fun initUserData(userID: String) {
        if (userID == null || userID.isEmpty()) {
            startRegisterActivity()
        }
        // initilize first time user data
        fireBaseDb.collection("users").document(userID).get()
        .addOnSuccessListener { document ->
            if (!document.exists()) {
                Log.d(TAG, "Current data: null")
                // If first time user, initialize user data
                val users = fireBaseDb.collection("users")

                val user = UserData(
                    100f,
                    HashMap<String, Int>(),
                    HashMap<String, Int>(),
                )
                users.document(userID).set(user)
            }
        }
        .addOnCompleteListener {
            renderUserProfile(userID)
        }
        .addOnFailureListener {
            Log.d(TAG, "Error getting documents")
        }
    }

    private fun renderUserProfile(userID: String) {
        if (userID == null || userID.isEmpty()) {
            startRegisterActivity()
        }
        // update layout
        fireBaseDb.collection("users").document(userID).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val userData = document.toObject<UserData>()
                    user_balance.text = "$" + String.format("%.2f", userData?.balance)
                } else {
                    Log.d(TAG, "user data: null")
                }
            }
            .addOnFailureListener {
                Log.d(TAG, "Error getting documents")
            }
    }

}