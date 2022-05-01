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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    private val BASE_URL = "https://api.spoonacular.com/"
    private val API_KEY = "d527da482f5f48be8629764a068e3ae1"
    private val TAG = "MainActivity"
    private val REQUEST_CODE = 0

    private lateinit var fireBaseDb: FirebaseFirestore

    private var recipeList = ArrayList<RecipesData>()
//    private val adapter
    lateinit private var retrofit: Retrofit
    lateinit private var spoonacularAPI: SpoonacularService

    private var list = ArrayList<String>()

    lateinit var USER: UserData
    lateinit var user_id: String

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
            user_id = currentUser.uid

            initUserData(currentUser.uid);
//            renderUserProfile(currentUser.uid)
        }

//        testRecipeAPI();
//        firebaseAdd();
//        firebaseReadAll();
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

//        intent.putExtra("balance", balance)
        startActivityForResult(intent, REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK){
            // update data
        }
    }

    fun testRecipeAPI() {
        list.add("apple")
        list.add("flour")
        list.add("sugar")
        spoonacularAPI.getRecipeList(API_KEY, list).enqueue(object: Callback<List<RecipesData>> {
            override fun onResponse(call: Call<List<RecipesData>>, response: Response<List<RecipesData>>) {
                Log.d(TAG, "onResponse: $response")

                val body = response.body()
                if (body == null) {
                    Log.d(TAG, "Valid response was nto received")
                    return
                }
                Log.d(TAG, "${body[0].id}")
                Log.d(TAG, "${body.get(0).title}")
                Log.d(TAG, "${body.get(0).image}")

                Log.d(TAG, "${body.get(1).id}")
                Log.d(TAG, "${body.get(1).title}")
                Log.d(TAG, "${body.get(1).image}")
            }

            override fun onFailure(call: Call<List<RecipesData>>, t: Throwable) {
                Log.d(TAG, "onFailure: $t")
            }
        })
    }

    fun addone(view: View) {
        fireBaseDb.collection("users").document(user_id).get()
        .addOnSuccessListener { document ->
            val updatedUser = mapOf(
                "balance" to 101
            )
            document.reference.update(updatedUser)
        }
        .addOnFailureListener {
            Log.d(TAG, "Update Failed")
        }
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
//                val user = hashMapOf(
//                    "balance" to 100,
//                    "ingredientInventory" to ArrayList<IngredientElement>(),
//                    "foodInventory" to ArrayList<FoodElement>(),
//                )
                val user = UserData(
                    100,
                    null,
                    null,
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
        // initilize first time user data
        fireBaseDb.collection("users").document(userID).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val userData = document.toObject<UserData>()
                    user_balance.text = userData?.balance.toString()
                } else {
                    Log.d(TAG, "user data: null")
                }
            }
            .addOnFailureListener {
                Log.d(TAG, "Error getting documents")
            }
    }

    fun firebaseReadAll() {
        Log.d(TAG, "Start Firebase")
        // Get data using addOnSuccessListener
        fireBaseDb.collection("contacts")
            .orderBy("name")  // Here you can also use orderBy to sort the results based on a field such as id
            //.orderBy("id", Query.Direction.DESCENDING)  // this would be used to orderBy in DESCENDING order
            .get()
            .addOnSuccessListener { documents ->
                Log.d(TAG, "success Firebase")
                Log.d(TAG, "${documents.size()}")

                // The result (documents) contains all the records in db, each of them is a document
                for (document in documents) {

                    Log.d(TAG, "${document.id} => ${document.data}")

                    Log.d(TAG, "contact:, ${document.get("name")}")
                }

                // show all the records as a string in a dialog
            }
            .addOnFailureListener {
                Log.d(TAG, "Error getting documents")
            }
    }
    fun firebaseAdd() {

        // Get an instance of our collection
        val contacts = fireBaseDb.collection("contacts")

        // Map or Dictionary objects is used to represent your document
        val contact = hashMapOf(
            "name" to "Jacob",
        )

        // Get an auto generated id for a document that you want to insert
        val documentId = contacts.document().id

        // Add data
        contacts.document(documentId).set(contact)



    }
}