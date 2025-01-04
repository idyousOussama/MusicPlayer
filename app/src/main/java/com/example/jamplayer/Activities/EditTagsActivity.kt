package com.example.jamplayer.Activities
import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.widget.addTextChangedListener
import com.example.jamplayer.Activities.SplachActivity.ItemsManagers.jamViewModel
import com.example.jamplayer.R
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.curretSong
import com.example.jamplayer.databinding.ActivityEditTagsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

class EditTagsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditTagsBinding
    private var currentSongName: String? = null
    private var currentSongArtist: String? = null
    private var currentSongImage: Bitmap? = null
    private lateinit var launchSongNewImages: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditTagsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getCurrentSong()
        saveCurrentSongChanges()
        initInputs()
        initCurrentSongImage()
        initChangeImageBtn()
        getNewCurrentSongImage()
    }

    private fun getNewCurrentSongImage() {
        launchSongNewImages = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null && result.data!!.data != null) {
                val imageUri = result.data!!.data!! // Get the Uri directly
                val contentResolver: ContentResolver = baseContext.contentResolver

                // Get the Bitmap from the image URI
                val bitmap = uriToBitmap(contentResolver,imageUri)

                if (bitmap != null) {
                    // Resize the bitmap if it's not null
                    currentSongImage = resizeBitmap(bitmap, 800, 800)

                    if (currentSongImage != null) {
                        binding.editTagsSongImage.setImageBitmap(currentSongImage)
                    } else {
                        Toast.makeText(this, "Image resize failed", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun initChangeImageBtn() {
        binding.changeCurrentSongImageBtn.setOnClickListener {
            openGalleryIntent()
        }
    }

    private fun initCurrentSongImage() {
        binding.editTagsSongImage.setOnClickListener {
            openGalleryIntent()
        }
    }

    private fun openGalleryIntent() {
        val openGalleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        launchSongNewImages.launch(openGalleryIntent)
    }

    private fun initInputs() {
        binding.editTagsNameInput.addTextChangedListener {
            currentSongName = it.toString()
            initSaveBtn()
        }
        binding.editTagsArtistInput.addTextChangedListener {
            currentSongArtist = it.toString()
            initSaveBtn()
        }
    }

    private fun saveCurrentSongChanges() {
        binding.saveSongChangesBtn.setOnClickListener {
            CoroutineScope(Dispatchers.Default).launch {
                runOnUiThread { binding.saveSongChangesBtn.text = "..." }
                jamViewModel.upDateCurrentSongById(
                    curretSong!!.id,
                    currentSongName.orEmpty(),
                    currentSongArtist.orEmpty(),
                    currentSongImage
                )
                runOnUiThread {
                    finish()
                    Toast.makeText(baseContext, R.string.current_song_edited_successful, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun initSaveBtn() {
        if (!currentSongName.isNullOrEmpty() && !currentSongArtist.isNullOrEmpty()) {
            enableSaveChangesBtn()
        } else {
            disableSaveChangesBtn()
        }
    }

    private fun enableSaveChangesBtn() {
        binding.saveSongChangesBtn.setBackgroundResource(R.drawable.enable_btns_backround)
        binding.saveSongChangesBtn.setTextColor(resources.getColor(R.color.white, theme))
        binding.saveSongChangesBtn.isEnabled = true
    }

    private fun disableSaveChangesBtn() {
        binding.saveSongChangesBtn.setBackgroundResource(R.drawable.disable_btns_backround)
        binding.saveSongChangesBtn.setTextColor(resources.getColor(R.color.disable_text_color, theme))
        binding.saveSongChangesBtn.isEnabled = false
    }

    private fun getCurrentSong() {
        currentSongName = curretSong?.title
        currentSongArtist = curretSong?.artist
        currentSongImage = curretSong?.musicImage
        binding.editTagsNameInput.setText(currentSongName)
        binding.editTagsArtistInput.setText(currentSongArtist)
        binding.editTagsPathText.setText(curretSong?.path)
        if (currentSongImage != null) {
            binding.editTagsSongImage.setImageBitmap(currentSongImage)
        } else {
            binding.editTagsSongImage.setImageResource(R.drawable.small_place_holder_image)
        }
    }


    fun uriToBitmap(contentResolver: ContentResolver, uri: Uri): Bitmap? {
        return try {
            // Open an InputStream from the URI
            val inputStream: InputStream? = contentResolver.openInputStream(uri)

            // Decode the InputStream into a Bitmap
            val bitmap = BitmapFactory.decodeStream(inputStream)

            // Close the InputStream
            inputStream?.close()

            // Return the Bitmap
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val scaleFactor = Math.min(maxWidth.toFloat() / width, maxHeight.toFloat() / height)

        return Bitmap.createScaledBitmap(
            bitmap,
            (width * scaleFactor).toInt(),
            (height * scaleFactor).toInt(),
            true
        )
    }
}
