package com.example.jamplayer.Activities.Songs
import android.app.Dialog
import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.jamplayer.Activities.Songs.SplachActivity.ItemsManagers.jamViewModel
import com.example.jamplayer.R
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.curretSong
import com.example.jamplayer.Services.BaseApplication.PlayingMusicManager.userIsActive
import com.example.jamplayer.databinding.ActivityEditTagsBinding
import com.example.jamplayer.databinding.ConfirmActionDialogBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStream

class EditTagsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditTagsBinding
    private var oldCurrentSongName: String? = null
    private var newcurrentSongName: String? = null
    private var newCurrentSongArtist: String? = null
    private var oldCurrentSongArtist: String? = null
    private var newCurrentSongImage: Bitmap? = null
    private var oldCurrentSongImage: Bitmap? = null
    private lateinit var launchSongNewImages: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditTagsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userIsActive = true
initGoBackBtn()
        getCurrentSong()
        saveCurrentSongChanges()
        initInputs()
        initCurrentSongImage()
        initChangeImageBtn()
        getNewCurrentSongImage()
        onBackPressonBachPressed()
    }

    private fun initGoBackBtn() {
binding.editTagBackBtn.setOnClickListener {
    if(!shekAboutNewChanges()) {

    }else {
        finish()
    }

}    }
    private fun showConfiramtionExitDialog() {
        val confiramtionDialogView = layoutInflater.inflate(R.layout.confirm_action_dialog,null)
        val confiramtionDialogBinding = ConfirmActionDialogBinding.bind(confiramtionDialogView)
        val confiramtionDialog = Dialog(this)
        confiramtionDialogBinding.confermDiologActionTitle.text = getString(R.string.save_audio_chages)
        confiramtionDialogBinding.confermDiologActionMessage.text = getString(R.string.confiramtion_video_changes_message)
        confiramtionDialogBinding.confermDiologActionPositiveBtn.text = getString(R.string.save_text)
        confiramtionDialogBinding.confermDiologActionNegativeBtn.text = getString(R.string.exit_text)
        confiramtionDialog.setContentView(confiramtionDialogView)
        confiramtionDialog.setCancelable(false)
        confiramtionDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        confiramtionDialogBinding.confermDiologActionPositiveBtn.setOnClickListener {
            saveSongChanges()
            confiramtionDialog.cancel()
        }
        confiramtionDialogBinding.confermDiologActionNegativeBtn.setOnClickListener {
            confiramtionDialog.cancel()
finish()
        }
        confiramtionDialog.show()
    }

    private fun shekAboutNewChanges(): Boolean {
        if(newcurrentSongName == oldCurrentSongName && newCurrentSongArtist  == oldCurrentSongArtist && newCurrentSongImage == oldCurrentSongImage ) {
            return  false
        }else{
            return true
        }
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
                    newCurrentSongImage = resizeBitmap(bitmap, 800, 800)

                    if (newCurrentSongImage != null) {
                        binding.editTagsSongImage.setImageBitmap(newCurrentSongImage)
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
            newcurrentSongName = it.toString()
            initSaveBtn()
        }
        binding.editTagsArtistInput.addTextChangedListener {
            newCurrentSongArtist = it.toString()
            initSaveBtn()
        }
    }

    private fun navigateToNewActivity(newActivity: Class<*>) {
val newActivityIntent = Intent(baseContext,newActivity)
        newActivityIntent.putExtra("VideoIsDeleted" , false)
        startActivity(newActivityIntent)
        finish()
    }

    private fun saveCurrentSongChanges() {
        binding.saveSongChangesBtn.setOnClickListener {
            saveSongChanges()
        }
    }

    private fun saveSongChanges() {
        CoroutineScope(Dispatchers.Default).launch {
            runOnUiThread { binding.saveSongChangesBtn.text = "..." }
            jamViewModel.upDateCurrentSongById(
                curretSong!!.id,
                newcurrentSongName.orEmpty(),
                newCurrentSongArtist.orEmpty(),
                newCurrentSongImage
            )

            runOnUiThread {
                navigateToNewActivity(MainActivity::class.java)
                Toast.makeText(baseContext, R.string.current_song_edited_successful, Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun initSaveBtn() {
        if (!newcurrentSongName.isNullOrEmpty() && !newCurrentSongArtist.isNullOrEmpty()) {
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
        oldCurrentSongName = curretSong?.title
        oldCurrentSongArtist= curretSong?.artist
        oldCurrentSongImage = curretSong?.musicImage

        newcurrentSongName = curretSong?.title
        newCurrentSongArtist= curretSong?.artist
        newCurrentSongImage = curretSong?.musicImage
        binding.editTagsNameInput.setText(oldCurrentSongName)
        binding.editTagsArtistInput.setText(oldCurrentSongArtist)
        binding.editTagsPathText.setText(curretSong?.path)
        if (oldCurrentSongImage != null) {
            binding.editTagsSongImage.setImageBitmap(oldCurrentSongImage)
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
    override fun onDestroy() {
        super.onDestroy()
        userIsActive = false
    }
    private fun onBackPressonBachPressed() {
        onBackPressedDispatcher.addCallback(this,object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                if(!shekAboutNewChanges()) {
                    finish()
                }else {
                    showConfiramtionExitDialog()
                }
            }
        })
    }
}
