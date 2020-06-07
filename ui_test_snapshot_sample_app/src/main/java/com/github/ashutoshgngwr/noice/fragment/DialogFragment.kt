package com.github.ashutoshgngwr.noice.fragment

import android.os.Bundle
import android.text.InputType
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.annotation.ArrayRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.core.widget.TextViewCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.FragmentManager
import com.github.ashutoshgngwr.noice.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textview.MaterialTextView
import kotlinx.android.synthetic.main.fragment_dialog__base.view.*
import kotlinx.android.synthetic.main.fragment_dialog__text_input.view.*


/**
 * A generic implementation with extensions for use-case specific designs.
 * API inspired by https://github.com/afollestad/material-dialogs but not using it
 * due to its reliance on old AppCompat API. I tried to make material-dialogs
 * work but it was bringing appearance inconsistencies and was generally rigid in
 * terms of styling.
 */
class DialogFragment : BottomSheetDialogFragment() {

  /**
   * A lambda for calling functions to configure the dialog, passed while invoking [show].
   */
  private var displayOptions: DialogFragment.() -> Unit = { }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    retainInstance = true // so this instance is retained when screen orientation changes.
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_dialog__base, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    displayOptions()
  }

  /**
   * Adds given [View] to the [R.id.content] layout in the dialog
   */
  private fun addContentView(view: View) {
    requireView().content.addView(view)
  }

  /**
   * Configures the given button [R.id.positive] or [R.id.negative] with the given text resId and
   * the onClick listener
   */
  private fun setButton(@IdRes which: Int, @StringRes resId: Int, onClick: () -> Unit) {
    val button = requireView().findViewById<Button>(which)
    button.visibility = View.VISIBLE
    button.text = getString(resId)
    button.setOnClickListener {
      onClick()
      dismiss()
    }
  }

  /**
   * An extension on the attribute resource [R.attr] for resolving its value as set in the current
   * theme.
   */
  private fun @receiver:androidx.annotation.AttrRes Int.resolveAttributeValue(): Int {
    val value = TypedValue()
    requireNotNull(dialog).context.theme.resolveAttribute(this, value, true)
    return value.data
  }

  /**
   * Sets the title of the dialog
   */
  fun title(@StringRes resId: Int) {
    requireView().title.text = getString(resId)
  }

  /**
   * Configures the positive button of the dialog. Wrapper around [setButton]
   */
  fun positiveButton(@StringRes resId: Int, onClick: () -> Unit = { }) {
    setButton(R.id.positive, resId, onClick)
  }

  /**
   * Configures the negative button of the dialog. Wrapper around [setButton]
   */
  fun negativeButton(@StringRes resId: Int, onClick: () -> Unit = { }) {
    setButton(R.id.negative, resId, onClick)
  }

  /**
   * shows the dialog and schedules the passed `options` lambda to be invoked in [onViewCreated]
   */
  fun show(fragmentManager: FragmentManager, options: DialogFragment.() -> Unit = { }) {
    displayOptions = options
    show(fragmentManager, javaClass.simpleName)
  }

  /**
   * Creates a [MaterialTextView] with given string resource and adds it to [R.id.content] layout
   * in the dialog
   */
  fun message(@StringRes resId: Int, vararg formatArgs: Any) {
    addContentView(
      MaterialTextView(requireContext()).apply {
        val textAppearance = android.R.attr.textAppearance.resolveAttributeValue()
        TextViewCompat.setTextAppearance(this, textAppearance)
        text = getString(resId, *formatArgs)
      }
    )
  }

  /**
   * Creates a [com.google.android.material.textfield.TextInputLayout] with given configuration
   * and adds it to [R.id.content] layout
   *
   * @param preFillValue value to pre-fill in the text field
   * @param type input type
   * @param validator a validation function that is called on text every time it is changed.
   */
  fun input(
    @StringRes hintRes: Int = 0,
    preFillValue: CharSequence = "",
    type: Int = InputType.TYPE_CLASS_TEXT,
    singleLine: Boolean = true,
    validator: (String) -> Boolean = { it.isNotBlank() },
    @StringRes errorRes: Int = 0
  ) {
    requireView().positive.isEnabled = false
    layoutInflater.inflate(R.layout.fragment_dialog__text_input, requireView().content, false)
      .apply {
        textInputLayout.hint = getString(hintRes)
        editText.inputType = type
        editText.isSingleLine = singleLine
        editText.setText(preFillValue)
        editText.addTextChangedListener {
          val valid = validator(it.toString())
          requireView().positive.isEnabled = valid
          textInputLayout.error = if (valid) {
            null
          } else {
            getString(errorRes)
          }
        }

        addContentView(this)
      }
  }

  /**
   * returns the text in the text field added using [input]. don't know what it'll do if called
   * without invoking [input] \o/
   */
  fun getInputText(): String {
    return requireView().editText.text.toString()
  }

  /**
   * creates a single choice list in the dialog with given configuration.
   * Creating this control removes the button panel. Dialog is dismissed when user
   * selects an item from the list.
   *
   * TODO(whenever-needed): handle scrolling for long lists (bottom sheet intercepts touch events now).
   * currently the only usage involves displaying a 3 item list.
   *
   * @param currentChoice must be >= -1 and < arraySize
   * @param onItemSelected listener invoked when a choice is selected by the user
   */
  fun singleChoiceItems(
    @ArrayRes itemsRes: Int,
    currentChoice: Int = -1,
    onItemSelected: (Int) -> Unit = { }
  ) {
    val items = requireContext().resources.getStringArray(itemsRes)
    require(currentChoice >= -1 && currentChoice < items.size)
    addContentView(
      ListView(requireContext()).apply {
        id = android.R.id.list
        dividerHeight = 0
        choiceMode = ListView.CHOICE_MODE_SINGLE
        adapter = ArrayAdapter(context, android.R.layout.simple_list_item_single_choice, items)
        if (currentChoice > -1) {
          setItemChecked(currentChoice, true)
        }

        setOnItemClickListener { _, _, position, _ ->
          onItemSelected(position)
          dismiss()
        }
      }
    )
  }
}
