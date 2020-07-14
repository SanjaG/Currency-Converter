package com.example.currencyconverter.di

import android.content.Context
import com.example.currencyconverter.CurrencyConverterApplication
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule

@Component(
    modules = [
        AndroidSupportInjectionModule::class,
        ActivityBuilderModule::class
    ]
)
interface AppComponent : AndroidInjector<CurrencyConverterApplication> {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun context(context: Context): Builder

        fun build(): AppComponent
    }
}