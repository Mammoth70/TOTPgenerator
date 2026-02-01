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

private const val PAYLOAD_TOTP = "PAYLOAD_TOTP"
private const val PAYLOAD_TOTP_NEXT = "PAYLOAD_TOTP_NEXT"
private const val PAYLOAD_PROGRESS = "PAYLOAD_PROGRESS"

internal class TokensAdapter : ListAdapter<Token, RecyclerView.ViewHolder>(TokenDiffComparator()) {
    // Класс ListAdapter для показа токенов.

    private var onBtnMenuClick: (view: View, id: Long) -> Unit = { _, _ ->}
    private var onItemClick: (totp: String) -> Unit = { }
    private var onItemLongClick: (totp: String) -> Boolean = { false }

    fun setOnBtnMenuClick(listener: (View, Long) -> Unit) { onBtnMenuClick = listener }
    fun setOnItemViewClick(listener: (String) -> Unit) { onItemClick = listener }
    fun setOnItemViewLongClick(listener: (String) -> Boolean) { onItemLongClick = listener }

    override fun getItemCount(): Int {
        // Функция вызывается LayoutManager'ом и возвращает общее количество элементов в списке + футер.
        val actualCount = super.getItemCount()
        // Футер нужен только если список не пуст
        return if (actualCount > 0) actualCount + 1 else 0
    }

    override fun getItemViewType(position: Int): Int {
        // Функция определяет тип элемента (токен или футер).
        return if (position == super.getItemCount()) TYPE_FOOTER else TYPE_ITEM
    }

    class FooterViewHolder(view: View) : RecyclerView.ViewHolder(view)
    // Представление viewHolder для футера.

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
        return if (viewType == TYPE_FOOTER) {
            // Создаём холдер для футера.
            val footer = View(parent.context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    (100 * resources.displayMetrics.density).toInt()
                )
            }
            FooterViewHolder(footer)
        } else {
            // Создаём холдер для токена.
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_token, parent, false)
            ViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // Функция привязывает к viewHolder'у данные списка токенов.
        if (holder is ViewHolder && position < super.getItemCount()) {
            val token = getItem(position)
            holder.nameView.text = if (token.issuer.isBlank()) token.label else "${token.issuer}:${token.label}"
            updateTOTP(holder, token)
            updateTOTPnext(holder, token)
            updateProgress(holder, token)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        // Перегруженная функция привязывает к viewHolder'у только изменённые данные списка токенов.
        val payloadsSet = payloads.firstOrNull() as? Set<*>
        if (holder is ViewHolder && position < super.getItemCount() && payloadsSet != null) {
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
            indicatorDirection = if (progressClockWise)
                CircularProgressIndicator.INDICATOR_DIRECTION_CLOCKWISE
            else
                CircularProgressIndicator.INDICATOR_DIRECTION_COUNTERCLOCKWISE

            this.progress = if (progressClockWise) token.progress else (100 - token.progress)
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