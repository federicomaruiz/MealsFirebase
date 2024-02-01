package com.utad.mymenu.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.utad.mymenu.databinding.FragmentCreateMealBinding
import com.utad.mymenu.firebase.real_time_db.RealTimeDBManager
import com.utad.mymenu.firebase.storage.FirebaseStorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class CreateMealFragment : Fragment() {

    private lateinit var _binding: FragmentCreateMealBinding

    private val binding: FragmentCreateMealBinding get() = _binding


    private var photoUrl: String? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreateMealBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnCreateMeal.setOnClickListener { createMeal() }
        binding.mcvSelectImage.setOnClickListener { checkStoragePermission() }


    }

    private fun createMeal() {
        val day = binding.etDayName.text.toString().trim()
        val meal = binding.etMealName.text.toString().trim()
        if (day != null && meal != null) {
            lifecycleScope.launch(Dispatchers.IO) {
                val result = RealTimeDBManager.saveMeal(
                    picUrl = photoUrl!!,
                    day = day,
                    name = meal,
                )
                withContext(Dispatchers.Main) {
                    if (result) {
                        Toast.makeText(requireContext(), "ok", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    } else {
                        Toast.makeText(requireContext(), "fallo", Toast.LENGTH_SHORT).show()
                    }
                }

            }
        } else {
            Toast.makeText(requireContext(), "Rellena todos los campos", Toast.LENGTH_SHORT).show()
        }
    }

    //region --- PERMISSION ---

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                openGallery()
            } else {
                showPermissionDialog()
            }
        }

    private fun checkStoragePermission() {
        // Guardo el permiso que vamos a pedir mirar el Manifest(android) a implementar
        val permission = Manifest.permission.READ_EXTERNAL_STORAGE

        // Compruebo si el permiso fue concedido por el usuario
        val isPermissionGranted = ContextCompat.checkSelfPermission(
            requireContext(),
            permission
        ) == PackageManager.PERMISSION_GRANTED

        // Compruebo si lo pedi previamente y necesito volver a pedirlo (el usuario lo ha denegado anteriormente)
        val shouldRequestRationale =
            ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), permission)

        if (isPermissionGranted) {
            openGallery()
            // Abrimos la galeria
        } else if (shouldRequestRationale) {
            showPermissionDialog()
            // Hemos pedido el pèrmiso y el usuario ha dicho que no
        } else {
            //Nunca le hemos pedido el permiso al usuario lo pedimos
            permissionLauncher.launch(permission)
        }


    }

    private fun showPermissionDialog() {
        Toast.makeText(
            requireContext(),
            "Por favor activa el permiso para acceder a la galeria",
            Toast.LENGTH_SHORT
        )
            .show()
    }


    //endregion ---  PERMISSION ---

    //region --- NAVIGATION ---


    // Va registrar el resultado del intent en la galeria,
    // Escuchamnos que imagen selecciono el usuario y compruebo el resultado y la info que lleva dentro
    // Resultado OK o MAL
    private val imageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val imageRoute = result.data

                if (imageRoute != null && imageRoute.data != null) {
                    val imageUri: Uri? = imageRoute.data
                    postImageOnFirebase(imageUri!!)
                    Toast.makeText(requireContext(), "Foto seleccionada", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "No se seelecciono imagen", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                Toast.makeText(requireContext(), "No se seleccino imagen", Toast.LENGTH_SHORT)
                    .show()
            }
        }

    private fun postImageOnFirebase(imageUri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            val response = FirebaseStorageManager.uploadPhoto(imageUri)

            if (response != null) {
                photoUrl = response
                withContext(Dispatchers.Main) {
                    Glide.with(requireContext())
                        .load(photoUrl)
                        .into(binding.ivPreview)
                }
                Log.i("postImageOnFirebase", "$response")
            } else {
                Log.i("postImageOnFirebase", "NULO $response")
            }
        }
    }

    // Abrir galeria, el launcher lanza un intent con la accion de seleccionar un contenido dentro de multimedia en las imagenes+
    // nos devuelve la ruta donde el usuario tiene selecciconada las imagenes , solo imagenes no quieremos videos
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*" // puedo especificar formato solo fotos filtro
        imageLauncher.launch(intent)
    }


    //endregion --- NAVIGATION ---
}