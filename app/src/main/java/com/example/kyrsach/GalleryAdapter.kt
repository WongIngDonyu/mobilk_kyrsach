package com.example.kyrsach

import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.os.Build
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.kyrsach.databinding.ItemMediaBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GalleryAdapter(private val mediaFiles: List<File>, private val onClick: (File) -> Unit) : RecyclerView.Adapter<GalleryAdapter.MediaViewHolder>() {

    class MediaViewHolder(private val binding: ItemMediaBinding) : RecyclerView.ViewHolder(binding.root) {
        @RequiresApi(Build.VERSION_CODES.Q)
        fun bind(file: File, onClick: (File) -> Unit) {
            val thumbnail: Bitmap? = if (file.extension == "mp4") {
                ThumbnailUtils.createVideoThumbnail(file.path, MediaStore.Images.Thumbnails.MINI_KIND)
            } else {
                ThumbnailUtils.extractThumbnail(ThumbnailUtils.createImageThumbnail(file.path, MediaStore.Images.Thumbnails.MINI_KIND), 120, 120)
            }
            if (thumbnail != null) {
                binding.imagePreview.setImageBitmap(thumbnail)
            } else {
                binding.imagePreview.setImageResource(android.R.color.darker_gray)
            }
            binding.contentType.text = if (file.extension == "mp4") "Видео" else "Фото"
            val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            val date = Date(file.lastModified())
            binding.creationDate.text = dateFormat.format(date)
            binding.imagePreview.setOnClickListener {
                onClick(file)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val binding = ItemMediaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MediaViewHolder(binding)
    }

    override fun getItemCount() = mediaFiles.size

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val file = mediaFiles[position]
        holder.bind(file, onClick)
    }
}

