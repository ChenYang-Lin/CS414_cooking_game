package com.example.cooking_game.inventory

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cooking_game.R
import com.example.cooking_game.Recipe
import com.example.cooking_game.shop.ShopCheckoutActivity
import com.example.cooking_game.shop.ShopSellActivity
import kotlinx.android.synthetic.main.item.view.*

class MyInventoryFoodRecyclerAdapter(private val food: ArrayList<Recipe>): RecyclerView.Adapter<MyInventoryFoodRecyclerAdapter.MyViewHolder>() {
    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val id = itemView.findViewById<TextView>(R.id.item_id)
        val name = itemView.findViewById<TextView>(R.id.item_name)
        val img = itemView.findViewById<ImageView>(R.id.item_img)

        init {
            itemView.setOnClickListener {
                val selectedItem = adapterPosition
                Toast.makeText(itemView.context, "You clicked on $selectedItem", Toast.LENGTH_SHORT).show()

                val intent = Intent(itemView.context, ShopSellActivity::class.java)
                intent.putExtra("id", itemView.item_id.text.toString()) // send id of current item
                intent.putExtra("type", "food") // type of item (food in this case)
                itemView.context.startActivity(intent)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        // create a new views
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentFood = food[position]
        val context = holder.itemView.context

        holder.id.text = currentFood.id
        holder.name.text = currentFood.title
        Glide.with(context)
            .load(currentFood.image)
            .placeholder(R.drawable.ic_baseline_fastfood_24_gray)
            .into(holder.img)
    }

    override fun getItemCount(): Int {
        return food.size
    }


}