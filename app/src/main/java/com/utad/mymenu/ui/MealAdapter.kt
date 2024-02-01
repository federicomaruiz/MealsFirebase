package com.utad.mymenu.ui


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.utad.mymenu.databinding.ItemMealPlanBinding
import com.utad.mymenu.firebase.real_time_db.model.Meal


class MealAdapter(
    val deleteAction: (meal: Meal) -> Unit
) : ListAdapter<Meal, MealAdapter.MealViewHolder>(MealItemCallBack) {

    inner class MealViewHolder(val binding: ItemMealPlanBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemMealPlanBinding.inflate(layoutInflater, parent, false)
        return MealViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        val meal = getItem(position)
        holder.binding.tvMealName.text = meal.mealName
        holder.binding.tvDayName.text = meal.day
        holder.binding.fabDelete.setOnClickListener {
            deleteAction(meal)
        }
        Glide.with(holder.binding.root)
            .load(meal.picUrl)
            .into(holder.binding.ivMealPhoto)

    }

}

object MealItemCallBack : DiffUtil.ItemCallback<Meal>() {
    override fun areItemsTheSame(oldItem: Meal, newItem: Meal): Boolean {
        return oldItem.key == newItem.key
    }

    override fun areContentsTheSame(oldItem: Meal, newItem: Meal): Boolean {
        return oldItem == newItem
    }
}