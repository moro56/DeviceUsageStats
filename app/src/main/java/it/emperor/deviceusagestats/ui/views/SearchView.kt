package it.emperor.deviceusagestats.ui.views

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import it.emperor.deviceusagestats.R

class SearchView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0, defStyleRes: Int = 0) : FrameLayout(context, attrs, defStyle, defStyleRes) {

    private var inputText: EditText
    private var clearButton: ImageButton

    var listener: OnTextChangeListener? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.system_search, this, true)

        inputText = findViewById(R.id.input)
        clearButton = findViewById(R.id.clear)

        inputText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) clearButton.visibility = View.GONE else clearButton.visibility = View.VISIBLE
                listener?.onTextChange(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

        })
        clearButton.setOnClickListener {
            inputText.text = null
        }
    }

    interface OnTextChangeListener {
        fun onTextChange(text: String)
    }
}