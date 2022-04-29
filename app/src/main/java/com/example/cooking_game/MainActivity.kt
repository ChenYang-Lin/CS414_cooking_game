package com.example.cooking_game

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    private val BASE_URL = "https://api.spoonacular.com/recipes/"
    private val API_KEY = "d527da482f5f48be8629764a068e3ae1"
    private val TAG = "MainActivity"

    private lateinit var fireBaseDb: FirebaseFirestore

    private var recipeList = ArrayList<RecipesData>()
//    private val adapter
    lateinit private var retrofit: Retrofit
    lateinit private var spoonacularAPI: SpoonacularService

    private var list = ArrayList<String>()

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
            Glide.with(this)
                .load(currentUser.photoUrl)
                .placeholder(R.drawable.ic_baseline_person_24)
                .circleCrop()
                .into(user_image)
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