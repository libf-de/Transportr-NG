package de.libf.transportrng

import org.koin.core.context.startKoin

fun initKoin(){
    startKoin {
        modules(
            TransportrModule,
            DatabaseModule,
            ViewModelModule,
            PlatformModule
        )
    }
}

