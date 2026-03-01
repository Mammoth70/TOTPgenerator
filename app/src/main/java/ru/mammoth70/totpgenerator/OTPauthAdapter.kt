package ru.mammoth70.totpgenerator

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class OTPauthAdapter(
    private val secrets: List<OTPauth>,
    private val onSelectionJSONChanged: (Boolean) -> Unit,
    private val onSelectionQRChanged: (Boolean) -> Unit,
) : RecyclerView.Adapter<OTPauthAdapter.OTPauthViewHolder>() {
    // Класс RecyclerViewAdapter для показа OTPauth.

    private val selectedPositions = mutableSetOf<Int>()  // Для хранения выбранных секретов.


    private fun notifyCallbacks() {
        // Функция настройки коллбеков для включения/выключения кнопок.

        val count = selectedPositions.size
        onSelectionJSONChanged(count > 0)
        onSelectionQRChanged(count in 1..10)
    }


    inner class OTPauthViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Представление viewHolder для токена.

        val nameView: TextView = view.findViewById(R.id.name)
        val checkBox: CheckBox = view.findViewById(R.id.checkBox)

        fun bind(secret: OTPauth) {
            nameView.text = if (secret.issuer.isBlank()) secret.label else "${secret.issuer}:${secret.label}"

            checkBox.setOnCheckedChangeListener(null)
            checkBox.isChecked = selectedPositions.contains(bindingAdapterPosition)
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                val currentPos = bindingAdapterPosition
                if (isChecked) selectedPositions.add(currentPos)
                else selectedPositions.remove(currentPos)
                notifyCallbacks()
            }
        }
    }


    fun selectAll() {
        // Выбрать все секреты.
        selectedPositions.addAll(secrets.indices)
        notifyCallbacks()
        notifyItemRangeChanged(0, secrets.size)
    }


    fun deselectAll() {
        // Снять выбор со всех секретов.
        selectedPositions.clear()
        notifyCallbacks()
        notifyItemRangeChanged(0, secrets.size)
    }


    fun getSelectedItems(): List<OTPauth> {
        // Вернуть список выбранных секретов.
        return selectedPositions.map { secrets[it] }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OTPauthViewHolder {
        // Функция вызывается LayoutManager'ом, чтобы создать viewHolder'ы и передать им макет,
        // по которому будут отображаться элементы списка.

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_otpauth,
            parent, false)
        return OTPauthViewHolder(view)
    }


    override fun onBindViewHolder(holder: OTPauthViewHolder, position: Int) {
        // Функция вызывается LayoutManager'ом, чтобы привязать к viewHolder'у данные, которые он должен отображать.

        holder.bind(secrets[position])
    }


    override fun getItemCount(): Int {
        // Функция вызывается LayoutManager'ом и возвращает общее количество элементов в списке.

        return secrets.size
    }

}