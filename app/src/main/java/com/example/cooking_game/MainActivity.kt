package com.example.cooking_game

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.cooking_game.cook.CookActivity
import com.example.cooking_game.inventory.InventoryActivity
import com.example.cooking_game.shop.ShopActivity
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import kotlin.collections.HashMap


class MainActivity : AppCompatActivity() {
    private val BASE_URL = "https://api.spoonacular.com/"
    // api keys from two accounts, just in case when free plan limitation reached
//    private val API_KEY = "d527da482f5f48be8629764a068e3ae1"
//    private val API_KEY = "00dff5c2b2574ed1bb71971332ce5f3a"
    private val API_KEY = "4f8651a8632a4f77acadea5f58162507"
    private val TAG = "MainActivity"


    private lateinit var fireBaseDb: FirebaseFirestore

    lateinit private var retrofit: Retrofit
    lateinit private var spoonacularAPI: SpoonacularService

    lateinit var userID: String



    private var selectedStove = 0 // keep track of which stove selected to cook




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

    // get newest data from firestore and render
    override fun onResume() {
        super.onResume()
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            startRegisterActivity()
        } else {
            renderUserProfile(currentUser.uid)
            Log.d(TAG, "onResume")
            return
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

    fun openCook(view: View?) {
        val intent = Intent(this, CookActivity::class.java)
        intent.putExtra("selectedStove", selectedStove)
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

                var stoves = 4
                var initCookingProgess = ArrayList<CookingProgress>()
                while (stoves > 0) {
                    var newStove = CookingProgress(
                        status = "empty"
                    )
                    initCookingProgess.add(newStove)
                    stoves--
                }


                val user = UserData(
                    100f,
                    HashMap<String, IngredientData>(),
                    HashMap<String, FoodData>(),
                    initCookingProgess,
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

                    // remove existing stoves and create new stoves
                    stove_grid.removeAllViews()
                    val StoveList = userData?.stoves
                    if (StoveList != null) {
                        for ((index, item) in StoveList.withIndex()) {
                            createViewElement(item, index)
                        }
                    }
                } else {
                    Log.d(TAG, "user data: null")
                }
            }
            .addOnFailureListener {
                Log.d(TAG, "Error getting documents")
            }
    }


    private fun createViewElement(cookingProgress: CookingProgress, index: Int) {
        val stoveView = layoutInflater.inflate(R.layout.stove, null)

        // get view by ids
        val imgBtn = stoveView.findViewById<ImageButton>(R.id.stove_img_btn)
        val progressBar = stoveView.findViewById<ProgressBar>(R.id.stove_progress_bar)
        val status = stoveView.findViewById<TextView>(R.id.remaining_time)

        // set event listener for image button
        imgBtn.setOnClickListener {
            selectedStove = index
            if (cookingProgress.status == "empty")
                openCook(null)
            else {
                var currentTime = Calendar.getInstance().apply { timeZone = TimeZone.getTimeZone("UTC") }.timeInMillis
                var completeTime = cookingProgress.completeTime ?: 0
                var secondsRemaining = completeTime - currentTime

                // check if cooking completed
                if (secondsRemaining <= 0) {
                    Log.d(TAG, "$secondsRemaining")
                    fireBaseDb.collection("users").document(userID).get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                val users = fireBaseDb.collection("users")
                                // get data for current user from firestore
                                val userData = document.toObject<UserData>()
                                var balance = userData?.balance
                                var newIngredientInventory = userData?.ingredientInventory ?: HashMap<String, IngredientData>()
                                var newFoodInventory = userData?.foodInventory ?: HashMap<String, FoodData>()
                                var stoves = userData?.stoves

                                // update meal
                                var id = stoves?.get(index)?.id ?: "0"
                                var quantity = stoves?.get(index)?.quantity ?: 1

                                val hold = newFoodInventory[id]?.quantity ?: 0
                                newFoodInventory[id] = FoodData(
                                    quantity + hold,
                                    stoves?.get(index)?.price,
                                    stoves?.get(index)?.name,
                                    stoves?.get(index)?.image,
                                )

                                // empty stove
                                stoves?.get(index)?.status = "empty"

                                val user = UserData(
                                    balance,
                                    newIngredientInventory,
                                    newFoodInventory,
                                    stoves
                                )
                                users.document(userID).set(user)

                                // user does not exist
                            } else {
                                Log.d(TAG, "user data: null")
                                startRegisterActivity()
                            }
                        }
                        .addOnCompleteListener {
                            renderUserProfile(userID)
                        }
                        .addOnFailureListener {
                            Log.d(TAG, "Error getting documents")
                        }
                }
            }
        }

        if (cookingProgress.status == "empty") {
            Log.d(TAG, "empty")
            imgBtn.setImageResource(R.drawable.stove_icon)
            progressBar.progress = 0
            status.text = "empty"
        } else {
            // render data
            var imageURL = cookingProgress.image ?: R.drawable.stove_icon
            Glide.with(this)
                .load(cookingProgress.image)
                .placeholder(R.drawable.ic_baseline_fastfood_24_gray)
                .into(imgBtn)

            // get progressbar
            var currentTime = Calendar.getInstance().apply { timeZone = TimeZone.getTimeZone("UTC") }.timeInMillis
            var startedTime = cookingProgress.startedTime ?: 0
            var completeTime = cookingProgress.completeTime ?: 0
            var requiredTime = completeTime - startedTime
            var passedTime = currentTime - startedTime
            var secondsRemaining = completeTime - currentTime

            // setup progress bar
            if (secondsRemaining >= 0) {


                // progressbar
                val mCountDownTimer: CountDownTimer

                progressBar.progress = ((passedTime / requiredTime) * 100).toInt()
                mCountDownTimer = object : CountDownTimer(secondsRemaining, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        passedTime += 1000
                        var progress: Float = 100 / (requiredTime.toFloat() / passedTime.toFloat())
                        progressBar.progress = progress.toInt()
                        var remainingTime = requiredTime - passedTime
                        var displayTime = (remainingTime / 1000).toInt().toString() + " seconds remaining"
                        status.text = displayTime
                    }

                    override fun onFinish() {
                        progressBar.progress = 100
                        status.text = "Ready!"
                    }
                }
                mCountDownTimer.start()
            } else {
                progressBar.progress = 100
                status.text = "Ready!"
            }

        }

        // addView to add this element/container to grid
        stove_grid.addView(stoveView)
    }


}
