package com.example.kyrsach

import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.kyrsach.databinding.FragmentGalleryBinding
import java.io.File

class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding?: throw IllegalStateException("Binding is not initialized")
    private val mediaFiles = mutableListOf<File>()
    private lateinit var mediaAdapter: MediaAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadMediaFiles()
        mediaAdapter = MediaAdapter(mediaFiles, ::onMediaClick)

        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = mediaAdapter
        }

        binding.backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun loadMediaFiles() {
        val mediaDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (mediaDir == null || !mediaDir.exists()) {
            return
        }
        val videoDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_MOVIES)
        if (videoDir == null || !videoDir.exists()) {
            return
        }
        mediaFiles.clear()
        val mediaFilesFromPictures = mediaDir.listFiles()?.toList() ?: emptyList()
        mediaFiles.addAll(mediaFilesFromPictures)
        val mediaFilesFromVideos = videoDir.listFiles()?.toList() ?: emptyList()
        mediaFiles.addAll(mediaFilesFromVideos)
        mediaFiles.sortByDescending {
            it.lastModified()
        }
    }

    private fun onMediaClick(file: File) {
        val bundle = Bundle().apply {
            putString("filePath", file.absolutePath)
        }
        findNavController().navigate(R.id.action_galleryFragment_to_mediaViewerFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
