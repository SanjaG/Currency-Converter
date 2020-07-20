package com.example.currencyconverter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.currencyconverter.util.LiveEvent

open class BaseViewModel<State : Any, Event : Any> : ViewModel() {

    private val stateLiveData: MutableLiveData<State> = MutableLiveData()
    private val eventLiveData: LiveEvent<Event> = LiveEvent()

    fun viewStateData(): LiveData<State> = stateLiveData
    fun viewEventData(): LiveData<Event> = eventLiveData

    protected var viewState: State? = null
        get() = stateLiveData.value ?: field
        set(value) {
            stateLiveData.value = value
        }

    protected fun emitEvent(event: Event) {
        eventLiveData.value = event
    }
}