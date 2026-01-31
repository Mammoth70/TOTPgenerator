package ru.mammoth70.totpgenerator

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.CircularProgressIndicator

private const val TYPE_ITEM = 0
private const val TYPE_FOOTER = 1

internal class TokensAdapter : ListAdapter<Token, RecyclerView.ViewHolder>(TokenDiffComparator()) {

    private var onBtnMenuClick: (view: View, position: Int) -> Unit = {  _, _ ->}
    private var onItemClick: (position: Int) -> Unit = { }
    private var onItemLongClick: (position: Int) -> Boolean = { false }

    fun setOnBtnMenuClick(listener: (View, Int) -> Unit) { onBtnMenuClick = listener }
    fun setOnItemViewClick(listener: (Int) -> Unit) { onItemClick = listener }
    fun setOnItemViewLongClick(listener: (Int) -> Boolean) { onItemLongClick = listener }

    override fun getItemCount(): Int {
        val actualCount = super.getItemCount()
        // Футер нужен только если список не пуст
        return if (actualCount > 0) actualCount + 1 else 0
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == super.getItemCount()) TYPE_FOOTER else TYPE_ITEM
    }

    class FooterViewHolder(view: View) : RecyclerView.ViewHolder(view)

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemToken: MaterialCardView = view.findViewById(R.id.itemToken)
        val btnMenu: Button = view.findViewById(R.id.btnMenu)
        val nameView: TextView = view.findViewById(R.id.name)
        val totpView: TextView = view.findViewById(R.id.totp)
        val remainView: TextView = view.findViewById(R.id.remain)
        val progressView: CircularProgressIndicator = view.findViewById(R.id.progress)

        init {
            btnMenu.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) onBtnMenuClick(it, pos)
            }
            itemToken.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) onItemClick(pos)
            }
            itemToken.setOnLongClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) { onItemLongClick(pos) } else false
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_FOOTER) {
            val footer = View(parent.context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    (100 * resources.displayMetrics.density).toInt()
                )
            }
            FooterViewHolder(footer)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_token, parent, false)
            ViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder && position < super.getItemCount()) {
            val token = getItem(position)
            holder.nameView.text = if (token.issuer.isBlank()) token.label else "${token.issuer}:${token.label}"
            holder.totpView.text = token.totp
            updateProgress(holder, token)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        val payloadsSet = payloads.firstOrNull() as? Set<*>
        if (holder is ViewHolder && position < super.getItemCount() && payloadsSet != null) {
            val token = getItem(position)

            if (payloadsSet.contains("PAYLOAD_TOTP")) {
                holder.totpView.text = token.totp
            }

            if (payloadsSet.contains("PAYLOAD_PROGRESS")) {
                updateProgress(holder, token)
            }

        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    private fun updateProgress(holder: ViewHolder, token: Token) {
        holder.remainView.text = token.remain.toString()
        holder.progressView.apply {
            indicatorDirection = if (progressClockWise)
                CircularProgressIndicator.INDICATOR_DIRECTION_CLOCKWISE
            else
                CircularProgressIndicator.INDICATOR_DIRECTION_COUNTERCLOCKWISE

            this.progress = if (progressClockWise) token.progress else (100 - token.progress)
        }
    }

    class TokenDiffComparator : DiffUtil.ItemCallback<Token>() {
        override fun areItemsTheSame(oldItem: Token, newItem: Token) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Token, newItem: Token) = oldItem == newItem

        override fun getChangePayload(oldItem: Token, newItem: Token): Any? {
            val diff = mutableSetOf<String>()
            if (oldItem.totp != newItem.totp) diff.add("PAYLOAD_TOTP")
            if (oldItem.progress != newItem.progress || oldItem.remain != newItem.remain) {
                diff.add("PAYLOAD_PROGRESS")
            }
            return diff.ifEmpty { null }
        }
    }
}