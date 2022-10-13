package com.junkfood.seal.connectivity

import kotlinx.coroutines.flow.Flow

interface ConnectivityObserver {
    fun observe(): Flow<Status>

    enum class Status {
        Avaliable, Unavaliable, Losing, Lost
    }
}