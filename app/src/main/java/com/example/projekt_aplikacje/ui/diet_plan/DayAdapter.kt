package com.example.projekt_aplikacje.ui.diet_plan

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.projekt_aplikacje.R
import com.example.projekt_aplikacje.model.Recipe

class DayAdapter (
    private var recipes: ArrayList<Recipe>, private val update : () -> Unit
) : RecyclerView.Adapter<DayAdapter.ViewHolder>(){

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.day_diet_item, parent, false)
        return ViewHolder(view)
    }

    fun setRecipes(recipesList : ArrayList<Recipe>) {
        recipes = recipesList
        notifyDataSetChanged()
    }

    fun addDay(recipe: Recipe){
        recipes.add(recipe)
        notifyItemInserted(recipes.size - 1)
        update()
    }

    fun deleteItem(pos:Int){
        recipes.removeAt((pos))
        notifyItemRemoved(pos)
        update()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val recipe = recipes[position]
        holder.itemView.apply {
            findViewById<TextView>(R.id.name).text = recipe.title
        }
        holder.itemView.setOnClickListener{v ->
            val intent = Intent(
                v.context,
                DayDetails::class.java
            )
            intent.putExtra("Recipe",recipe)
            v.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return recipes.size
    }

}
