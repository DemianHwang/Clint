package com.demian.eventlog

enum class EventLog {
    SCREEN01,
    SCREEN02;

    fun event(event: Int): Builder {
        return Builder(name, event)
    }

    class Builder internal constructor(val screen: String, val event: Int) {
        fun param(key: String, value: String?): Builder {
            return this
        }

        fun log() {

        }
    }
}