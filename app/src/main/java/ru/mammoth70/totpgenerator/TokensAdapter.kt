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

internal class TokensAdapter : ListAdapter<Token, RecyclerView.ViewHolder>(TokenDiffComparator()) {
    // Класс ListAdapter для показа токенов.


    companion object {
        private const val PAYLOAD_TOTP = "PAYLOAD_TOTP"
        private const val PAYLOAD_TOTP_NEXT = "PAYLOAD_TOTP_NEXT"
        private const val PAYLOAD_PROGRESS = "PAYLOAD_PROGRESS"
    }


    private var onBtnMenuClick: (view: View, id: Long) -> Unit = { _, _ ->}
    private var onItemClick: (totp: String) -> Unit = { }
    private var onItemLongClick: (totp: String) -> Boolean = { false }

    fun setOnBtnMenuClick(listener: (View, Long) -> Unit) { onBtnMenuClick = listener }
    fun setOnItemViewClick(listener: (String) -> Unit) { onItemClick = listener }
    fun setOnItemViewLongClick(listener: (String) -> Boolean) { onItemLongClick = listener }


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Представление viewHolder для токена.

        val itemToken: MaterialCardView = view.findViewById(R.id.itemToken)
        val btnMenu: Button = view.findViewById(R.id.btnMenu)
        val nameView: TextView = view.findViewById(R.id.name)
        val totpView: TextView = view.findViewById(R.id.totp)
        val totpNextView: TextView = view.findViewById(R.id.totpNext)
        val remainView: TextView = view.findViewById(R.id.remain)
        val progressView: CircularProgressIndicator = view.findViewById(R.id.progress)


        init {
            // Привязка листенеров.

            btnMenu.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION)
                    onBtnMenuClick(it, getItem(pos).id)
            }

            itemToken.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION)
                    onItemClick(getItem(pos).totp)
            }

            itemToken.setOnLongClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onItemLongClick(getItem(pos).totp)
                } else false
           }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // Функция вызывается LayoutManager'ом, чтобы создать viewHolder'ы и передать им макет.

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_token, parent, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // Функция привязывает к viewHolder'у данные списка токенов.

        val token = getItem(position)
        val vh = holder as ViewHolder
        vh.nameView.text = if (token.issuer.isBlank()) token.label else "${token.issuer}:${token.label}"
        updateTOTP(vh, token)
        updateTOTPnext(vh, token)
        updateProgress(vh, token)
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        // Перегруженная функция привязывает к viewHolder'у только изменённые данные списка токенов.

        val payloadsSet = payloads.firstOrNull() as? Set<*>
        if (holder is ViewHolder && payloadsSet != null) {
            val token = getItem(position)

            if (payloadsSet.contains(PAYLOAD_TOTP)) {
                updateTOTP(holder, token)
            }

            if (payloadsSet.contains(PAYLOAD_TOTP_NEXT)) {
                updateTOTPnext(holder, token)
            }

            if (payloadsSet.contains(PAYLOAD_PROGRESS)) {
                updateProgress(holder, token)
            }

        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }


    private fun updateTOTP(holder: ViewHolder, token: Token) {
        // Вывод текста с токеном.
        holder.totpView.text = token.totp
    }


    private fun updateTOTPnext(holder: ViewHolder, token: Token) {
        // Вывод текста со следующим токеном.
        holder.totpNextView.text = token.totpNext
    }


    private fun updateProgress(holder: ViewHolder, token: Token) {
        // Настройка и вывод колёсика прогресса, вывод оставшихся секунд.

        holder.remainView.text = token.remain.toString()
        holder.progressView.apply {
            indicatorDirection = if (SettingsManager.progressClockWise)
                CircularProgressIndicator.INDICATOR_DIRECTION_CLOCKWISE
            else
                CircularProgressIndicator.INDICATOR_DIRECTION_COUNTERCLOCKWISE

            this.progress = if (SettingsManager.progressClockWise) token.progress else (100 - token.progress)
        }
    }


    class TokenDiffComparator : DiffUtil.ItemCallback<Token>() {
        // Callback для рассчёта разницы между двумя элементами.

        override fun areItemsTheSame(oldItem: Token, newItem: Token) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Token, newItem: Token) = oldItem == newItem

        override fun getChangePayload(oldItem: Token, newItem: Token): Any? {
            // Функция определяет конкретные изменённые поля.
            val diff = mutableSetOf<String>()
            if (oldItem.totp != newItem.totp) diff.add(PAYLOAD_TOTP)
            if (oldItem.totpNext != newItem.totpNext) diff.add(PAYLOAD_TOTP_NEXT)
            if (oldItem.progress != newItem.progress || oldItem.remain != newItem.remain) {
                diff.add(PAYLOAD_PROGRESS)
            }
            return diff.ifEmpty { null }
        }
    }

}