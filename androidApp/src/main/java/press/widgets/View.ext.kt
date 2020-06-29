package press.widgets

import android.graphics.Rect
import android.os.Build.VERSION.SDK_INT
import android.text.Editable
import android.util.TypedValue.COMPLEX_UNIT_PX
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import androidx.core.view.children
import me.saket.wysiwyg.widgets.SimpleTextWatcher
import kotlin.DeprecationLevel.ERROR

fun View.string(@StringRes stringRes: Int) = resources.getString(stringRes)

fun View.attr(@AttrRes resId: Int) = Attr(resId, context)

@get:Deprecated(message = "Impossible", level = ERROR)
var EditText.hintRes: Int
  get() = throw UnsupportedOperationException()
  set(resId) {
    hint = string(resId)
  }

@get:Deprecated(message = "Impossible", level = ERROR)
var View.padding: Int
  get() = throw UnsupportedOperationException()
  set(padding) {
    setPadding(padding, padding, padding, padding)
  }

var TextView.textColor: Int
  get() = currentTextColor
  set(color) {
    setTextColor(color)
  }

var TextView.textSizePx: Float
  get() = textSize
  set(size) {
    setTextSize(COMPLEX_UNIT_PX, size)
  }

inline fun fromOreo(block: () -> Unit) {
  if (SDK_INT >= 26) {
    block()
  }
}

fun Toolbar.findTitleView(): TextView {
  if (subtitle != null && subtitle.isNotBlank()) {
    throw UnsupportedOperationException("TODO")
  }
  if (title == null) {
    // Toolbar lazy creates the title TextView.
    title = " "
  }
  return children.find { it is TextView } as TextView
}

fun EditText.showKeyboard(): Boolean {
  requestFocus()
  val inputManager = context.getSystemService(InputMethodManager::class.java)!!
  return inputManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

fun View.hideKeyboard() {
  val inputManager = context.getSystemService(InputMethodManager::class.java)!!
  inputManager.hideSoftInputFromWindow(windowToken, 0)
}

fun View.locationOnScreen(): Rect {
  val loc = IntArray(2)
  getLocationOnScreen(loc)
  return Rect(loc[0], loc[1], loc[0] + width, loc[1] + height)
}

inline fun EditText.doOnTextChange(crossinline action: (Editable) -> Unit) {
  addTextChangedListener(object : SimpleTextWatcher {
    override fun afterTextChanged(text: Editable) = action(text)
  })
}

val View.parentView: ViewGroup get() = parent as ViewGroup

inline fun View.doOnAttach(crossinline action: () -> Unit) {
  if (isAttachedToWindow) {
    action()
  }
  addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
    override fun onViewDetachedFromWindow(v: View) = Unit
    override fun onViewAttachedToWindow(v: View) =
      removeOnAttachStateChangeListener(this).also {
        action()
      }
  })
}
