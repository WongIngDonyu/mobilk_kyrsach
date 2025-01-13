package com.example.kyrsach

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.example.kyrsach.databinding.FragmentMediaViewerBinding
import java.io.File

class MediaViewerFragment : Fragment() {

    private var _binding: FragmentMediaViewerBinding? = null
    private val binding get() = _binding?: throw IllegalStateException("Binding is not initialized")
    private val handler = Handler(Looper.getMainLooper())
    private var isPlay = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMediaViewerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val filePath = arguments?.getString("filePath")
        val file = File(filePath ?: return)
        if (file.extension == "mp4") {
            setupVideo(file)
        } else {
            setupImage(file)
        }

        binding.backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.deleteButton.setOnClickListener {
            if (file.exists() && file.delete()) {
                Toast.makeText(requireContext(), "Файл удален", Toast.LENGTH_SHORT).show()
                requireActivity().onBackPressedDispatcher.onBackPressed()
            } else {
                Toast.makeText(requireContext(), "Ошибка удаления файла", Toast.LENGTH_SHORT).show()
            }
        }

        binding.playPauseButton.setOnClickListener {
            switchPlayPause()
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    binding.videoView.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                handler.removeCallbacksAndMessages(null)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                binding.videoView.seekTo(binding.seekBar.progress)
                if (isPlay) {
                    updateSeekBar()
                } else {
                    binding.seekBar.progress = binding.videoView.currentPosition
                }
            }
        })
    }

    private fun setupVideo(file: File) {
        binding.videoView.apply {
            visibility = View.VISIBLE
            setVideoPath(file.absolutePath)
            setOnPreparedListener { mediaPlayer ->
                binding.seekBar.max = mediaPlayer.duration
                start()
                isPlay = true
                binding.playPauseButton.setImageResource(android.R.drawable.ic_media_pause)
                updateSeekBar()
            }

            setOnCompletionListener {
                binding.playPauseButton.setImageResource(android.R.drawable.ic_media_play)
                isPlay = false
            }
        }
        binding.playPauseButton.visibility = View.VISIBLE
        binding.seekBar.visibility = View.VISIBLE
        binding.imageView.visibility = View.GONE
    }

    private fun setupImage(file: File) {
        binding.imageView.apply {
            visibility = View.VISIBLE
            setImageURI(file.toUri())
        }
        binding.videoView.visibility = View.GONE
        binding.playPauseButton.visibility = View.GONE
        binding.seekBar.visibility = View.GONE
    }

    private fun updateSeekBar() {
        binding.seekBar.progress = binding.videoView.currentPosition
        if (isPlay) {
            handler.postDelayed({ updateSeekBar() }, 100)
        }
    }

    private fun switchPlayPause() {
        if (isPlay) {
            binding.videoView.pause()
            binding.playPauseButton.setImageResource(android.R.drawable.ic_media_play)
            handler.removeCallbacksAndMessages(null)
        } else {
            binding.videoView.start()
            binding.playPauseButton.setImageResource(android.R.drawable.ic_media_pause)
            updateSeekBar()
        }
        isPlay = !isPlay
        updateSeekBar() //без этого после изменения sekbara в паузе он не будет норм отрабатывать
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        handler.removeCallbacksAndMessages(null)
    }
}

