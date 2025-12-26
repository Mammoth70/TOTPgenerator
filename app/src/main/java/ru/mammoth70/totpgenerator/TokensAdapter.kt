package ru.mammoth70.totpgenerator

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.CircularProgressIndicator

internal class TokensAdapter(context: Context, private val layout: Int, private val tokensList: ArrayList<Token>) :
// Класс ArrayAdapter для показа токенов.

    ArrayAdapter<Token>(
        context,
        layout,
        tokensList
    ) {

    private var btnMenuClick: (view: View) -> Unit = { }
    private var itemClick: (view: View) -> Unit = { }
    private var itemViewLongClick: (view: View) -> Boolean = { false }

    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Обработка и вывод токена.
        var convertView = convertView
        val viewHolder: ViewHolder
        if ( convertView == null) {
            val inflater: LayoutInflater = LayoutInflater.from(context)
            convertView = inflater.inflate(this.layout, parent, false)
            viewHolder = ViewHolder(convertView)
            convertView.tag = viewHolder
        } else {
            viewHolder = convertView.tag as ViewHolder
        }
        val token = tokensList[position]
        viewHolder.nameView.text = if (token.issuer.isBlank()) {
            token.label
        } else {
            token.issuer + ":" + token.label
        }
        viewHolder.totpView.text = token.totp
        if (token.totp.isEmpty()) {
            viewHolder.remainView.visibility = View.INVISIBLE
            viewHolder.progressView.visibility = View.INVISIBLE
        } else {
            viewHolder.remainView.visibility = View.VISIBLE
            viewHolder.progressView.visibility  = View.VISIBLE
        }
        viewHolder.remainView.text = token.remain.toString()
        viewHolder.progressView.progress = if (appPassed) {
            token.progress
        } else {
            100 - token.progress
        }

        viewHolder.btnMenu.tag = position
        viewHolder.btnMenu.setOnClickListener(btnMenuClick)
        viewHolder.itemToken.tag = position
        viewHolder.itemToken.setOnClickListener(itemClick)
        viewHolder.itemToken.setOnLongClickListener(itemViewLongClick)

        return convertView
    }

    private class ViewHolder(view: View) {
        // Представление viewHolder'а для списка токенов.
        val itemToken: MaterialCardView = view.findViewById(R.id.itemToken)
        val btnMenu: Button = view.findViewById(R.id.btnMenu)
        val nameView: TextView = view.findViewById(R.id.name)
        val totpView: TextView = view.findViewById(R.id.totp)
        val remainView: TextView = view.findViewById(R.id.remain)
        val progressView: CircularProgressIndicator = view.findViewById(R.id.progress)
    }

    fun setOnBtnMenuClick(listener: (View) -> Unit) {
        // Функция устанавливает click listener для кнопки меню на элементе.
        btnMenuClick = listener
    }

    fun setOnItemViewClick(listener: (View) -> Unit) {
        // Функция устанавливает click listener для всего элемента списка.
        itemClick = listener
    }
    fun setOnItemViewLongClick(listener: (View) -> Boolean) {
        // Функция устанавливает long click listener для всего элемента списка.
        itemViewLongClick = listener
    }

}