package com.example.currencyconverter.ui.main.convert

import android.animation.*
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.DecelerateInterpolator
import androidx.core.view.forEach
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.currencyconverter.BaseFragment
import com.example.currencyconverter.R
import com.example.currencyconverter.data.models.Currency
import com.example.currencyconverter.ui.main.selectcurrency.SelectCurrencyDialog
import com.example.currencyconverter.util.*
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_convert_currency.*


class ConvertCurrencyFragment : BaseFragment<ConvertCurrencyState, ConvertCurrencyEvent>() {

    override val viewModel by viewModels<ConvertCurrencyViewModel> { factory }

    private lateinit var onGlobalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_convert_currency, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.viewStateData().observe(viewLifecycleOwner, Observer { state ->
            when (state) {
                is ConvertCurrencyState -> setupLayout(state)
            }
        })

        viewModel.shouldSwitchCurrencies().observe(viewLifecycleOwner, Observer {shouldSwitch ->
            if (shouldSwitch) switchCurrencies(50, 0, null)
        })

        viewModel.viewEventData().observe(viewLifecycleOwner, Observer { event ->
            when (event) {
                is InvalidNumberInput -> context?.toast(getString(R.string.invalid_number_input))
                is NoInternetConnection -> setupNoInternetConnectionLayout()
            }
        })

        initializeOnGlobalLayoutListener()
        viewModel.init()
        setupSwitchCurrenciesButton()
        setupCalculateButton()
    }

    override fun onStart() {
        super.onStart()
        // Previous listener needs to be removed so we do not add the listener twice, which
        // can result in NullPointerException.
        removeOnGlobalLayoutListenerForKeyboardVisibility()
        addOnGlobalLayoutListenerForKeyboardVisibility()
    }

    override fun onPause() {
        super.onPause()
        removeOnGlobalLayoutListenerForKeyboardVisibility()
        requireActivity().currentFocus?.hideKeyboard()
    }

    private fun initializeOnGlobalLayoutListener() {
        // Due to lack of integrated methods with which we could get basic information about
        // keyboard (like whether it is visible or not), this is somewhat hacky way of knowing that.
        onGlobalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
            val activityContainer = requireActivity().container
            val r = Rect()
            // r will be populated with the visible area.
            activityContainer.getWindowVisibleDisplayFrame(r)

            val heightDiff = activityContainer.rootView.height - r.height()
            // If heightDiff is more than 25% of the screen, it is probably keyboard.
            if (heightDiff > PERCENTAGE_OF_NON_VISIBLE_SCREEN * activityContainer.rootView.height) {
                disclaimerContainer.hide()
                // Once disclaimer is hidden, we want to animate rootContainer so disclaimer
                // can show nicely with no flickering.
                rootContainer.layoutTransition = LayoutTransition()
            } else {
                if (disclaimerContainer.isVisible.not()) {
                    disclaimerContainer.run {
                        show()
                        setBaseAnimation()
                    }
                }
            }
        }
    }

    private fun addOnGlobalLayoutListenerForKeyboardVisibility() {
        requireActivity().container.viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener)
    }

    private fun removeOnGlobalLayoutListenerForKeyboardVisibility() {
        requireActivity().container.viewTreeObserver.removeOnGlobalLayoutListener(onGlobalLayoutListener)
    }

    private fun setupLayout(state: ConvertCurrencyState) {
        setupSelectedFromCurrencyButton(state.selectedFromCurrency)
        setupSelectedToCurrencyButton()
        fromCurrencyInput.apply {
            setText(state.fromValue)
            fromCurrencyInput.text?.length?.let { setSelection(it) }
        }
        toCurrencyInput.setText(state.toValue)
        showCurrencyConvertContainer()
    }

    private fun setupSelectedFromCurrencyButton(selectedFromCurrency: Currency) {
        selectedFromCurrencyButton.run {
            text = selectedFromCurrency.currencyName
            setOnClickListener {
                requireActivity().checkIfFragmentAlreadyOpened(
                    SelectCurrencyDialog.TAG,
                    { showSelectCurrencyDialog(selectedFromCurrency) }
                )
            }
        }
    }

    private fun switchCurrencies(
        delayMillis: Long = 0,
        animationDuration: Long,
        animationInterpolator: DecelerateInterpolator? = DecelerateInterpolator()
    ) {
        Handler().postDelayed({
            val toPosition = selectedToCurrencyButton.x

            val fromAnimator = ObjectAnimator.ofFloat(selectedFromCurrencyButton, TRANSLATION_X, toPosition)
            val toAnimator = ObjectAnimator.ofFloat(selectedToCurrencyButton, TRANSLATION_X, -toPosition)

            AnimatorSet().run {
                playTogether(fromAnimator, toAnimator)
                duration = animationDuration
                interpolator = animationInterpolator
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator?) {
                        super.onAnimationStart(animation)
                        switchCurrenciesButton.isEnabled = false
                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        switchCurrenciesButton.isEnabled = true
                    }
                })
                start()
            }
        }, delayMillis)
    }

    private fun setupSelectedToCurrencyButton() {
        selectedToCurrencyButton.text = getString(R.string.hrk)
    }

    private fun showSelectCurrencyDialog(selectedFromCurrency: Currency) {
        SelectCurrencyDialog.newInstance(selectedFromCurrency).apply {
            onConfirmListener = { newSelectedCurrencyName ->
                viewModel.onNewCurrencySelected(newSelectedCurrencyName)
            }
        }.show(requireActivity().supportFragmentManager, SelectCurrencyDialog.TAG)
    }

    private fun setupSwitchCurrenciesButton() {
        switchCurrenciesButton.setOnClickListener {
            it.animate().rotationBy(SWITCH_CURRENCIES_BUTTON_ROTATION_VALUE).start()
            viewModel.onCurrencySwitch(fromCurrencyInput.text.toString())
            switchCurrencies(animationDuration = SWITCH_CURRENCIES_ANIMATION_DURATION)
        }
    }

    private fun setupCalculateButton() {
        buttonCalculate.setOnClickListener {
            viewModel.onCalculateClicked(fromCurrencyInput.text.toString())
        }
    }

    private fun showCurrencyConvertContainer() {
        noInternetConnectionContainer.hide()
        convertCurrencyContainer.show()
        shouldEnableBottomNavigationView(true)
    }

    private fun setupNoInternetConnectionLayout() {
        convertCurrencyContainer.hide()
        noInternetConnectionContainer.show()
        wifiOffImage.run {
            pathAnimator.duration(ANIMATION_DURATION).start()
            pathColor = requireContext().getCompatColor(R.color.colorPrimary)
            setFillAfter(true)
        }

        Snackbar.make(
            convertCurrencyContainer,
            getString(R.string.no_internet_connection_title),
            Snackbar.LENGTH_INDEFINITE
        )
            .setAction(getString(R.string.retry_button)) { viewModel.init() }
            .setActionTextColor(requireContext().getCompatColor(R.color.colorAccent))
            .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE)
            .show()

        shouldEnableBottomNavigationView(false)
    }

    private fun shouldEnableBottomNavigationView(shouldEnable: Boolean) {
        requireActivity().bottomNavigationView.menu.forEach { it.isEnabled = shouldEnable }
    }
}