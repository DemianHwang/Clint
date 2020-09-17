package com.demian.customlint

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.demian.eventlog.EventLog

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Toast.makeText(this, "Not called toast", Toast.LENGTH_SHORT)
        val toast = Toast.makeText(this, "Called toast", Toast.LENGTH_SHORT)




        EventLog.SCREEN01.event(1).log()



        EventLog.SCREEN01.event(1).param("a", "1")
    }

    fun example() {
        EventLog.SCREEN01.event(3).log()
        val event = EventLog.SCREEN01.event(2).param("a", "1")
        foo()
        bar()
        baz()

    }

    fun example2(): EventLog.Builder {
        return EventLog.SCREEN01.event(3)
    }

    fun byif() {
        var a = true
        if (a) { //when도 마찬가지
            EventLog.SCREEN01.event(1)
        } else {
            EventLog.SCREEN01.event(2)
        }.log()
    }

    fun byWhen() {
        var a = 1
        when (a) {
            1 -> EventLog.SCREEN01.event(1)
            2 -> EventLog.SCREEN01.event(2)
            else -> EventLog.SCREEN01.event(3)
        }.log()
    }

    fun test() {
        logWithParam(EventLog.SCREEN01.event(1).param("a", "1"))
    }

    fun logWithParam(logBuilder: EventLog.Builder) {
        logBuilder.param("a", "1").log()
    }

//    fun test() {
//        val event = EventLog.SCREEN01.event(1)
//        //...
//        //...
//        event.log()
//    }

    fun foo() {
        example2()
    }

    fun bar() {

    }

    fun baz() {
        hi()
    }


    fun aaa() = EventLog.SCREEN01.event(4)



    fun getEventBuilder(): EventLog.Builder {
        return EventLog.SCREEN01.event(1).param("a", "1")
    }


    fun logEventWithParam(event: EventLog.Builder) {
        event.param("a", "1")
        event.log()
    }

    fun hi() {
        val event = EventLog.SCREEN01.event(1)
        logEventWithParam(event)
    }


    companion object {
        val a = MainActivity()

        init {
            a.hi()
        }
    }


}