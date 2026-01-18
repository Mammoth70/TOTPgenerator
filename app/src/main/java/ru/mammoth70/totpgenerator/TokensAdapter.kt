package ru.mammoth70.totpgenerator

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat.getColor
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.CircularProgressIndicator

// fun convertDpToPixels(context: Context, dp: Float) = (dp * context.resources.displayMetrics.density).toInt()

internal class TokensAdapter(context: Context, private val layout: Int, private val tokensList: ArrayList<Token>) :
    ArrayAdapter<Token>(context, layout, tokensList) {
    // Класс ArrayAdapter для показа токенов.
    private var btnMenuClick: (view: View) -> Unit = { }
    private var itemClick: (view: View) -> Unit = { }
    private var itemViewLongClick: (view: View) -> Boolean = { false }

    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Обработка и вывод токена.
        var convertView = convertView
        val viewHolder: ViewHolder
        if (convertView == null) {
            val inflater: LayoutInflater = LayoutInflater.from(context)
            convertView = inflater.inflate(this.layout, parent, false)
            viewHolder = ViewHolder(convertView)
            convertView.tag = viewHolder
        } else {
            viewHolder = convertView.tag as ViewHolder
        }
        viewHolder.apply {
            val token = tokensList[position]
            if (token.id != FOOTER_TOKEN) {
                nameView.text = if (token.issuer.isBlank()) {
                    token.label
                } else {
                    token.issuer + ":" + token.label
                }
                totpView.text = token.totp
                if (token.totp.isEmpty()) {
                    remainView.visibility = View.INVISIBLE
                    progressView.visibility = View.INVISIBLE
                } else {
                    remainView.visibility = View.VISIBLE
                    progressView.visibility = View.VISIBLE
                }
                remainView.text = token.remain.toString()
                progressView.apply {
                    if (appPassed) {
                        indicatorDirection =
                            CircularProgressIndicator.INDICATOR_DIRECTION_CLOCKWISE
                        progress = token.progress
                    } else {
                        indicatorDirection =
                            CircularProgressIndicator.INDICATOR_DIRECTION_COUNTERCLOCKWISE
                        progress = 100 - token.progress
                    }
                }
                btnMenu.apply {
                    visibility = View.VISIBLE
                    tag = position
                    setOnClickListener(btnMenuClick)
                }
                itemToken.apply {
                    tag = position
                    // itemToken.strokeWidth = convertDpToPixels(context, 1f)
                    strokeColor = getColor(context, R.color.md_theme_outline)
                    setOnClickListener(itemClick)
                    setOnLongClickListener(itemViewLongClick)
                    isClickable = true
                }
            } else {
                nameView.text = ""
                totpView.text = ""
                progressView.visibility = View.INVISIBLE
                remainView.visibility = View.INVISIBLE
                btnMenu.apply {
                    visibility = View.INVISIBLE
                    tag = FOOTER_TOKEN
                    setOnClickListener(null)
                }
                itemToken.apply {
                    // strokeWidth = 0
                    strokeColor = getColor(context, R.color.md_theme_surface)
                    tag = FOOTER_TOKEN
                    setOnClickListener(null)
                    setOnLongClickListener(null)
                    isClickable = false
                }
            }
        }
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