package com.utad.mymenu.firebase.real_time_db

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.utad.mymenu.firebase.real_time_db.model.Meal
import kotlinx.coroutines.tasks.await

object RealTimeDBManager {
    // Guardo la referencia de la base de datos del proyecto
    private val dbReference = FirebaseDatabase.getInstance().reference

    suspend fun saveMeal(picUrl: String, name: String, day: String): Boolean {
        // Creo un conexion , para que dentro de firebase realDataBase
        // Para guardar los objetos de los almuerzo que vaya creando en una subCarpeta (meals)
        val connection = dbReference.child("meals")

        // Creo una key única para poder guardar el objeto
        val key = connection.push().key

        if (key != null) {
            // Creo el objeto con los parametros que me van llegando en la funcion
            val meal = Meal(key, day, name, picUrl)
            // Lo guardo , creo un child dentro de la subCarpeta meals
            // Con una key que identifique el objeto y dentro guardo el objeto
            connection.child(key).setValue(meal).await()
            return true
        } else {
            Log.d("saveMeal", "error")
            return false
        }
    }

    suspend fun getMeals(): List<Meal> {
        val connection = dbReference.child("meals")
        val result = connection.orderByKey().get().await() // Ordenamos por como se fueron guardando
        val mealList: MutableList<Meal> =
            mutableListOf() // Lista para guardar los objetos que me van llegando

        if (result.childrenCount > 0) {
            result.children.mapNotNull { dataSnapshot ->
                val data = dataSnapshot.getValue(Meal::class.java)
                if (!data?.key.isNullOrEmpty()
                    && !data?.day.isNullOrEmpty()
                    && !data?.mealName.isNullOrEmpty()
                    && !data?.picUrl.isNullOrEmpty()
                    ){
                    mealList.add(data!!)
                }

            }
        }
        return mealList.toList()
    }

    fun deleteMeal(key:String){
        val connection = dbReference.child("meals")
        connection.child(key).removeValue()
    }
}