package com.demian.customlint;


import com.demian.eventlog.EventLog;

class JavaTest {

    void test() {

    }

    void ternary() {
        boolean b = false;

        (b ? EventLog.SCREEN01.event(1) : EventLog.SCREEN01.event(2)).log();

        b(b ? EventLog.SCREEN01.event(1) : EventLog.SCREEN01.event(2));
    }

    void b(EventLog.Builder b) {

    }
}





