package ru.mammoth70.totpgenerator

import android.content.res.ColorStateList
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
    private val onItemClick: (OTPauth) -> Unit,
) : RecyclerView.Adapter<OTPauthAdapter.OTPauthViewHolder>() {
    // Класс RecyclerViewAdapter для показа OTPauth.


    private val selectedPositions = mutableSetOf<Int>()  // Для хранения выбранных секретов.
    private var colorCheckBoxUnstandart: ColorStateList? = null
    private var colorCheckBoxStandart: ColorStateList? = null


    private fun notifyCallbacks() {
        // Функция настройки коллбеков для включения/выключения кнопок.

        val count = selectedPositions.size
        val standardCount = selectedPositions.count { pos ->
            val otpauth = secrets[pos]
            otpauth.digits != 7 && otpauth.period == 30 && otpauth.secret.length % 8 == 0
        }
        onSelectionJSONChanged(count > 0)
        onSelectionQRChanged(standardCount in 1..10)
    }


    inner class OTPauthViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Представление viewHolder для токена.

        val nameView: TextView = view.findViewById(R.id.name)
        val checkBox: CheckBox = view.findViewById(R.id.checkBox)

        fun bind(otpauth: OTPauth) {
            nameView.text = if (otpauth.issuer.isBlank()) otpauth.label else "${otpauth.issuer}:${otpauth.label}"

            checkBox.buttonTintList = if ( otpauth.digits != 7 && otpauth.period == 30 && otpauth.secret.length % 8 == 0 ) {
                colorCheckBoxStandart
            } else {
                colorCheckBoxUnstandart
            }

            itemView.setOnClickListener {
                onItemClick(otpauth)
            }

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
        if (colorCheckBoxUnstandart == null) {
            colorCheckBoxUnstandart = ColorStateList.valueOf(parent.context.getThemeColor(R.attr.colorError))
        }
        if (colorCheckBoxStandart == null) {
            colorCheckBoxStandart = ColorStateList.valueOf(parent.context.getThemeColor(R.attr.colorOnSurface))
        }
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