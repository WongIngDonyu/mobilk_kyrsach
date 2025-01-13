package com.example.kyrsach

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.kyrsach.databinding.FragmentVideoBinding
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class VideoFragment : Fragment() {

    private var _binding: FragmentVideoBinding? = null
    private val binding get() = _binding?: throw IllegalStateException("Binding is not initialized")
    private var videoCapture: VideoCapture<Recorder>? = null
    private lateinit var cameraExecutor: ExecutorService
    private var activeRecording: Recording? = null
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var isRecording = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVideoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
        binding.recordingTime.visibility = View.GONE

        binding.photoButton.setOnClickListener {
            if (isRecording) {
                stopRecording()
            } else {
                startRecording()
            }
        }

        binding.switchCameraButton.setOnClickListener {
            switchCamera()
        }

        binding.videoButton.setOnClickListener{
            findNavController().navigate(R.id.action_videoFragment_to_photoFragment)
        }

        binding.galleryButton.setOnClickListener{
            findNavController().navigate(R.id.action_videoFragment_to_galleryFragment)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = androidx.camera.core.Preview.Builder()
                .build()
                .also { it.setSurfaceProvider(binding.previewView.surfaceProvider) }
            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                .build()
            videoCapture = VideoCapture.withOutput(recorder)
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    cameraSelector,
                    preview,
                    videoCapture
                )
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при запуске камеры", e)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun switchCamera() {
        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
        startCamera()
    }

    private fun startRecording() {
        val videoCapture = this.videoCapture ?: return
        val videoFile = File(
            requireContext().getExternalFilesDir(Environment.DIRECTORY_MOVIES),
            "video_${System.currentTimeMillis()}.mp4"
        )
        val outputOptions = FileOutputOptions.Builder(videoFile).build()
        activeRecording = if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            videoCapture.output
                .prepareRecording(requireContext(), outputOptions)
                .withAudioEnabled()
                .start(ContextCompat.getMainExecutor(requireContext())) { recordEvent -> handleRecordEvent(recordEvent, videoFile)
                }
        } else {
            videoCapture.output
                .prepareRecording(requireContext(), outputOptions)
                .start(ContextCompat.getMainExecutor(requireContext())) { recordEvent -> handleRecordEvent(recordEvent, videoFile)
                }
        }
    }

    private fun handleRecordEvent(recordEvent: VideoRecordEvent, videoFile: File) {
        when (recordEvent) {
            is VideoRecordEvent.Start -> {
                isRecording = true
                updateUIForRecording(true)
                Log.d(TAG, "Запись началась")
            }
            is VideoRecordEvent.Finalize -> {
                isRecording = false
                updateUIForRecording(false)
                if (recordEvent.error == VideoRecordEvent.Finalize.ERROR_NONE) {
                    Toast.makeText(requireContext(), "Видео сохранено", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Видео сохранено по пути: ${videoFile.absolutePath}")
                } else {
                    Log.e(TAG, "Ошибка записи: ${recordEvent.error}")
                }
            }
        }
    }

    private fun stopRecording() {
        activeRecording?.stop()
        activeRecording = null
        Log.d(TAG, "Запись остановлена")
    }

    private fun updateUIForRecording(isRecording: Boolean) {
        if (isRecording) {
            binding.photoButton.background = ContextCompat.getDrawable(requireContext(), R.drawable.square_button)
            binding.photoButton.backgroundTintList=null
            binding.recordingTime.apply {
                visibility = View.VISIBLE
                base = SystemClock.elapsedRealtime()
                start()
            }
            binding.galleryButton.isVisible = false
            binding.videoButton.isVisible = false
        } else {
            binding.photoButton.background = ContextCompat.getDrawable(requireContext(), R.drawable.circle_button)
            binding.photoButton.backgroundTintList=null
            binding.recordingTime.apply {
                stop()
                visibility = View.GONE
            }
            binding.galleryButton.isVisible = true
            binding.videoButton.isVisible = true
        }
    }


    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(requireContext(), "Необходимо разрешение для использования камеры", Toast.LENGTH_SHORT).show()
                activity?.finish()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "VideoFragment"
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
        private const val REQUEST_CODE_PERMISSIONS = 99
    }
}
