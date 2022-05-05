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
import com.example.cooking_game.Ingredient
import com.example.cooking_game.R
import com.example.cooking_game.shop.ShopCheckoutActivity
import kotlinx.android.synthetic.main.item.view.*

class MyInventoryRecyclerAdapter(private val ingredients: ArrayList<Ingredient>): RecyclerView.Adapter<MyInventoryRecyclerAdapter.MyViewHolder>() {
    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val id = itemView.findViewById<TextView>(R.id.item_id)
        val name = itemView.findViewById<TextView>(R.id.item_name)
        val img = itemView.findViewById<ImageView>(R.id.item_img)

        init {
            itemView.setOnClickListener {
                val selectedItem = adapterPosition
                Toast.makeText(itemView.context, "You clicked on $selectedItem", Toast.LENGTH_SHORT).show()

                val intent = Intent(itemView.context, ShopCheckoutActivity::class.java)
                intent.putExtra("id", itemView.item_id.text.toString()) // send id of current ingredient to checkout activity
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
        val currentIngredient = ingredients[position]
        val context = holder.itemView.context

        holder.id.text = currentIngredient.id
        holder.name.text = currentIngredient.name
        Glide.with(context)
            .load(currentIngredient.image)
            .placeholder(R.drawable.ic_baseline_fastfood_24_gray)
            .into(holder.img)
    }

    override fun getItemCount(): Int {
        return ingredients.size
    }



}