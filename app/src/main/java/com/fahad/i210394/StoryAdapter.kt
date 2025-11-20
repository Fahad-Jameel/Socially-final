package com.fahad.i210394
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.fahad.i210394.R
import com.fahad.i210394.StoriesDB.Story
import de.hdodenhof.circleimageview.CircleImageView
// …

class StoryAdapter(
    private val stories: List<Story>,
    private val onClick: (storyPicBase64: String) -> Unit
) : RecyclerView.Adapter<StoryAdapter.VH>() {

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // ← use CircleImageView here
        val imgProfile: CircleImageView = itemView.findViewById(R.id.imgProfile)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_story, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val story = stories[position]

        // decode Base64 → Bitmap
        val bytes = Base64.decode(story.prfPic, Base64.DEFAULT)
        val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

        // set on CircleImageView
        holder.imgProfile.setImageBitmap(bmp)

        holder.imgProfile.setOnClickListener {
            onClick(story.storyPic)
        }
    }

    override fun getItemCount() = stories.size
}
