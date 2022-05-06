package com.example.cooking_game.cook

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cooking_game.ExtendedIngredients
import com.example.cooking_game.R
import com.example.cooking_game.shop.ShopCheckoutActivity
import kotlinx.android.synthetic.main.cook_prepare_item.view.*
import kotlin.math.ceil

class MyPrepareRecyclerAdapter(private val requiedIngredients: ArrayList<ExtendedIngredients>): RecyclerView.Adapter<MyPrepareRecyclerAdapter.MyViewHolder>() {
    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val id = itemView.findViewById<TextView>(R.id.cook_prepare_item_id)
        val name = itemView.findViewById<TextView>(R.id.cook_prepare_item_name)
        val img = itemView.findViewById<ImageView>(R.id.cook_prepare_item_img)
        val hold = itemView.findViewById<TextView>(R.id.cook_prepare_hold)
        val amount = itemView.findViewById<TextView>(R.id.cook_prepare_amount)
        val unit = itemView.findViewById<TextView>(R.id.cook_prepare_amount_unit)



        init {
            itemView.setOnClickListener {
                val selectedItem = adapterPosition
                Toast.makeText(itemView.context, "You clicked on $selectedItem", Toast.LENGTH_SHORT).show()

                val intent = Intent(itemView.context, ShopCheckoutActivity::class.java)
                intent.putExtra("id", itemView.cook_prepare_item_id.text.toString()) // send id of current ingredient to checkout activity
                itemView.context.startActivity(intent)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        // create a new views
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cook_prepare_item, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentIngredient = requiedIngredients[position]
        val context = holder.itemView.context

        val requiredAmount = ceil(currentIngredient.amount ?: 1.0f).toInt()

        // render based on data
        holder.id.text = currentIngredient.id
        holder.name.text = currentIngredient.name
        Glide.with(context)
            .load("https://spoonacular.com/cdn/ingredients_100x100/" + currentIngredient.image)
            .placeholder(R.drawable.ic_baseline_fastfood_24_gray)
            .into(holder.img)
        holder.amount.text = requiredAmount.toString()
        holder.hold.text = currentIngredient?.hold.toString()
        if (currentIngredient.hold < requiredAmount) {
            holder.hold.setTextColor(Color.RED);
        } else {
            holder.hold.setTextColor(Color.GREEN);
        }
        if (currentIngredient.unit != "")
            holder.unit.text = currentIngredient.unit
    }

    override fun getItemCount(): Int {
        return requiedIngredients.size
    }
}