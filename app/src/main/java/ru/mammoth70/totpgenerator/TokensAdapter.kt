package ru.mammoth70.totpgenerator

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.CircularProgressIndicator

internal class TokensAdapter(context: Context, private val layout: Int, private val tokensList: ArrayList<Token>) :
    ArrayAdapter<Token>(context, layout, tokensList) {
    // Класс ArrayAdapter для показа токенов.
    private var btnMenuClick: (view: View) -> Unit = { }
    private var itemClick: (view: View) -> Unit = { }
    private var itemViewLongClick: (view: View) -> Boolean = { false }

    private class ViewHolder(view: View) {
        // Представление viewHolder'а для списка токенов.
        val itemToken: MaterialCardView = view.findViewById(R.id.itemToken)
        val btnMenu: Button = view.findViewById(R.id.btnMenu)
        val nameView: TextView = view.findViewById(R.id.name)
        val totpView: TextView = view.findViewById(R.id.totp)
        val remainView: TextView = view.findViewById(R.id.remain)
        val progressView: CircularProgressIndicator = view.findViewById(R.id.progress)
    }

    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Обработка и вывод токена.
        val holder: ViewHolder
        val rowView : View

        if (convertView == null) {
            rowView = LayoutInflater.from(context).inflate(this.layout, parent, false)
            holder = ViewHolder(rowView)
            rowView.tag = holder
        } else {
            rowView = convertView
            holder = rowView.tag as ViewHolder
        }

        val token = tokensList[position]

        if (token.id != FOOTER_TOKEN) {
            holder.nameView.text = if (token.issuer.isBlank()) token.label else "${token.issuer}:${token.label}"
            holder.totpView.text = token.totp

            val hasTotp = token.totp.isNotEmpty()
            holder.remainView.visibility = if (hasTotp) View.VISIBLE else View.INVISIBLE
            holder.progressView.visibility = if (hasTotp) View.VISIBLE else View.INVISIBLE

            if (hasTotp) {
                holder.remainView.text = token.remain.toString()
                holder.progressView.apply {
                    indicatorDirection = if (appPassed)
                        CircularProgressIndicator.INDICATOR_DIRECTION_CLOCKWISE
                    else
                        CircularProgressIndicator.INDICATOR_DIRECTION_COUNTERCLOCKWISE

                    progress = if (appPassed) token.progress else (100 - token.progress)
                }
            }

            holder.btnMenu.apply {
                tag = position
                visibility = View.VISIBLE
                setOnClickListener { btnMenuClick(it) }
            }

            holder.itemToken.apply {
                tag = position
                setOnClickListener { itemClick(it) }
                setOnLongClickListener { itemViewLongClick(it) }
                isClickable = true
                isLongClickable = true
                setStrokeColor(ContextCompat.getColorStateList(context, R.color.md_theme_outline))
            }

        } else {
            // Состояние футера
            holder.nameView.text = ""
            holder.totpView.text = ""
            holder.progressView.visibility = View.INVISIBLE
            holder.remainView.visibility = View.INVISIBLE
            holder.btnMenu.apply {
                tag = FOOTER_TOKEN
                visibility = View.INVISIBLE
                setOnClickListener(null)
            }

            holder.itemToken.apply {
                tag = FOOTER_TOKEN
                setOnClickListener(null)
                setOnLongClickListener(null)
                isClickable = false
                isLongClickable = false
                setStrokeColor(ContextCompat.getColorStateList(context, android.R.color.transparent))
            }
        }

        return rowView
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