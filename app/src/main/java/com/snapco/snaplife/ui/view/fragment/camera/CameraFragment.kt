package com.snapco.snaplife.ui.view.fragment.camera

import android.content.ContentValues
import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.PictureResult
import com.snapco.snaplife.R
import com.snapco.snaplife.ui.viewmodel.CameraViewModel
import java.io.File
import java.io.FileInputStream
import java.io.IOException


class CameraFragment : Fragment() {

    private lateinit var cameraView: CameraView
    private lateinit var captureButton: Button
    private lateinit var switchCameraButton: Button
    private val cameraViewModel: CameraViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_camera, container, false)
        cameraView = view.findViewById(R.id.cameraView)
        captureButton = view.findViewById(R.id.btnCapture)
        switchCameraButton = view.findViewById(R.id.btnSwitchCamera)

        // Thiết lập CameraView
        cameraView.setLifecycleOwner(viewLifecycleOwner)
        cameraView.addCameraListener(object : CameraListener() {
            override fun onPictureTaken(result: PictureResult) {
                super.onPictureTaken(result)
                savePicture(result)
            }
        })

        // Xử lý sự kiện nhấn nút chụp ảnh
        captureButton.setOnClickListener {
            cameraView.takePicture()
        }

        // Xử lý sự kiện nhấn nút chuyển đổi camera
        switchCameraButton.setOnClickListener {
            cameraViewModel.switchCamera()
        }

        // Quan sát thay đổi từ ViewModel
        cameraViewModel.facing.observe(viewLifecycleOwner, Observer { facing ->
            cameraView.facing = facing
        })

        return view
    }

    private fun savePicture(result: PictureResult) {
        result.toFile(getTemporaryFile(requireContext())) { file ->
            if (file != null) {
                saveToGallery(file)
            } else {
                Toast.makeText(context, "Failed to save picture", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getTemporaryFile(context: Context): File {
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("TEMP_", ".jpg", storageDir)
    }

    private fun saveToGallery(file: File) {
        val resolver = requireContext().contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }

        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        if (uri != null) {
            try {
                val outputStream = resolver.openOutputStream(uri)
                outputStream?.use { stream ->
                    FileInputStream(file).use { inputStream ->
                        inputStream.copyTo(stream)
                    }
                }
                Toast.makeText(context, "Picture saved to gallery", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                Toast.makeText(context, "Failed to save picture: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                file.delete()
            }
        } else {
            Toast.makeText(context, "Failed to create new MediaStore record", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraView.destroy()
    }
}