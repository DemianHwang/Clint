package com.demian.clint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.checks.infrastructure.TestFile
import com.android.tools.lint.checks.infrastructure.TestLintTask
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import com.demian.clint.detector.EventLogDetector2
import org.junit.Test

class EventLogDetectorTest {
    init {
//        EventLogDetector2.printDebug = true
    }

    fun TestLintTask.setTestFiles(vararg testFile: TestFile): TestLintTask {
        val list = arrayOf(EventLogDetectorTestStubs.EventLogStub)
        return this.files(*list, *testFile)
    }


    @Test
    fun `Logged`() {
        val src: LintDetectorTest.TestFile = LintDetectorTest.kotlin(
            """
            import com.demian.eventlog.EventLog
            class Main() {
                fun a() {
                    EventLog.SCREEN01.event(1).log()
                }
            } 
            """.trimIndent()
        ).within("src")

        lint().files(EventLogDetectorTestStubs.EventLogStub, src)
            .allowMissingSdk().issues(EventLogDetector2.ISSUE)
            .run()
            .expectClean()
    }

    @Test
    fun `Not logged`() {
        val src: LintDetectorTest.TestFile = LintDetectorTest.kotlin(
            """
            import com.demian.eventlog.EventLog
            class Main() {
                fun a() {
                    EventLog.SCREEN01.event(1).param("a", "1")
                }
            } 
            """.trimIndent()
        ).within("src")

        lint().files(EventLogDetectorTestStubs.EventLogStub, src)
            .allowMissingSdk().issues(EventLogDetector2.ISSUE)
            .run()
            .expectErrorCount(1)
    }

    @Test
    fun `One logged one not logged`() {
        val src: LintDetectorTest.TestFile = LintDetectorTest.kotlin(
            """
            import com.demian.eventlog.EventLog
            class Main() {
                fun a() {
                    EventLog.SCREEN01.event(1)
                    EventLog.SCREEN01.event(1).param("a", "1").log()
                }
            } 
            """.trimIndent()
        ).within("src")

        lint().files(EventLogDetectorTestStubs.EventLogStub, src)
            .allowMissingSdk().issues(EventLogDetector2.ISSUE)
            .run()
            .expectErrorCount(1)
    }

    @Test
    fun `Multiple not logged`() {
        val src: LintDetectorTest.TestFile = LintDetectorTest.kotlin(
            """
            import com.demian.eventlog.EventLog
            class Main() {
                fun a() {
                    EventLog.SCREEN01.event(1)
                    EventLog.SCREEN01.event(2)
                    EventLog.SCREEN01.event(3)
                    EventLog.SCREEN01.event(4).param("a", "1")
                }
            } 
            """.trimIndent()
        ).within("src")

        lint().files(EventLogDetectorTestStubs.EventLogStub, src)
            .allowMissingSdk().issues(EventLogDetector2.ISSUE)
            .run()
            .expectErrorCount(4)
    }

    @Test
    fun `Kotlin If expression logged`() {
        val src: LintDetectorTest.TestFile = LintDetectorTest.kotlin(
            """
            import com.demian.eventlog.EventLog
            class Main() {
                fun a() {
                    var a = true
                    if (a) {
                        EventLog.SCREEN01.event(1)
                    } else {
                        EventLog.SCREEN01.event(2)
                    }.log()
                }
            } 
            """.trimIndent()
        ).within("src")

        lint().files(EventLogDetectorTestStubs.EventLogStub, src)
            .allowMissingSdk().issues(EventLogDetector2.ISSUE)
            .run()
            .expectClean()
    }

    @Test
    fun `Kotlin If expression not logged`() {
        val src: LintDetectorTest.TestFile = LintDetectorTest.kotlin(
            """
            import com.demian.eventlog.EventLog
            class Main() {
                fun a() {
                    var a = true
                    if (a) {
                        EventLog.SCREEN01.event(1)
                    } else {
                        EventLog.SCREEN01.event(2)
                    }
                }
            } 
            """.trimIndent()
        ).within("src")

        lint().files(EventLogDetectorTestStubs.EventLogStub, src)
            .allowMissingSdk().issues(EventLogDetector2.ISSUE)
            .run()
            .expectErrorCount(2)
    }

    @Test
    fun `Kotlin When expression`() {
        val src: LintDetectorTest.TestFile = LintDetectorTest.kotlin(
            """
            import com.demian.eventlog.EventLog
            class Main() {
                fun a() {
                    var a = 1
                    when (a) {
                        1 -> EventLog.SCREEN01.event(1)
                        2 -> EventLog.SCREEN01.event(2)
                    }.log() //logged
                    
                    when (a) {
                        1 -> EventLog.SCREEN01.event(1)
                        2 -> EventLog.SCREEN01.event(2)
                    } //not logged
                    
                    when (a) {
                        1 -> EventLog.SCREEN01.event(1).log() //logged
                        2 -> EventLog.SCREEN01.event(2)
                    }
                }
            } 
            """.trimIndent()
        ).within("src")

        lint().files(EventLogDetectorTestStubs.EventLogStub, src)
            .allowMissingSdk().issues(EventLogDetector2.ISSUE)
            .run()
            .expectErrorCount(3)
    }

    @Test
    fun `Kotlin When expression + assignment`() {
        val src: LintDetectorTest.TestFile = LintDetectorTest.kotlin(
            """
            import com.demian.eventlog.EventLog
            class Main() {
                fun a() {
                    var a = 1
                    
                    val b = when (a) {
                        1 -> EventLog.SCREEN01.event(1)
                        2 -> EventLog.SCREEN01.event(2)
                    }
                    
                    val c = when (a) {
                        1 -> EventLog.SCREEN01.event(1)
                        2 -> EventLog.SCREEN01.event(2)
                    }
                    c.log()

                }
            } 
            """.trimIndent()
        ).within("src")

        lint().files(EventLogDetectorTestStubs.EventLogStub, src)
            .allowMissingSdk().issues(EventLogDetector2.ISSUE)
            .run()
            .expectErrorCount(2)
    }

    @Test
    fun `Kotlin When expression + return`() {
        val src: LintDetectorTest.TestFile = LintDetectorTest.kotlin(
            """
            import com.demian.eventlog.EventLog
            class Main() {
                fun a(): EventLog.Builder {
                    var a = 1
                    return when (a) {
                        1 -> EventLog.SCREEN01.event(1)
                        2 -> EventLog.SCREEN01.event(2)
                    } 
                }
            } 
            """.trimIndent()
        ).within("src")

        lint().files(EventLogDetectorTestStubs.EventLogStub, src)
            .allowMissingSdk().issues(EventLogDetector2.ISSUE)
            .run()
            .expectErrorCount(0)
    }

    @Test
    fun `Kotlin When expression + argument`() {
        val src: LintDetectorTest.TestFile = LintDetectorTest.kotlin(
            """
            import com.demian.eventlog.EventLog
            class Main() {
                fun a() {
                    var a = 1
                    b(when (a) {
                        1 -> EventLog.SCREEN01.event(1)
                        2 -> EventLog.SCREEN01.event(2)
                    } )
                }
                
                fun b(builder: EventLog.Builder) {
                
                }
            } 
            """.trimIndent()
        ).within("src")

        lint().files(EventLogDetectorTestStubs.EventLogStub, src)
            .allowMissingSdk().issues(EventLogDetector2.ISSUE)
            .run()
            .expectErrorCount(0)
    }

    @Test
    fun `Kotlin If expression not logged other logged`() {
        val src: LintDetectorTest.TestFile = LintDetectorTest.kotlin(
            """
            import com.demian.eventlog.EventLog
            class Main() {
                fun a() {
                    var a = true
                    if (a) {
                        EventLog.SCREEN01.event(1)
                    } else {
                        EventLog.SCREEN01.event(2).param("a", "1")
                    }
                    EventLog.SCREEN02.event(3).log()
                }
            } 
            """.trimIndent()
        ).within("src")

        lint().files(EventLogDetectorTestStubs.EventLogStub, src)
            .allowMissingSdk().issues(EventLogDetector2.ISSUE)
            .run()
            .expectErrorCount(2)
    }

    @Test
    fun `Passed as argument`() {
        val src: LintDetectorTest.TestFile = LintDetectorTest.kotlin(
            """
            import com.demian.eventlog.EventLog
            class Main() {
                fun a() {
                    b(EventLog.SCREEN01.event(1).param("a", "1"))
                }
                
                fun b(event : EventLog.Builder) {
                    event.param("a", "1").log()
                }
            } 
            """.trimIndent()
        ).within("src")

        lint().files(EventLogDetectorTestStubs.EventLogStub, src)
            .allowMissingSdk().issues(EventLogDetector2.ISSUE)
            .run()
            .expectClean()
    }

    @Test
    fun `Java Ternary`() {
        val src: LintDetectorTest.TestFile = LintDetectorTest.java(
            """
            package com.demian.customlint;

            import com.demian.eventlog.EventLog;
            class JavaTest {
                void a() {
                    boolean b = false;
            
                    ((b ? EventLog.SCREEN01.event(1) : EventLog.SCREEN01.event(2))).log();
                }
            } 
            """.trimIndent()
        ).within("src")

        lint().files(EventLogDetectorTestStubs.EventLogStub, src)
            .allowMissingSdk().issues(EventLogDetector2.ISSUE)
            .run()
            .expectClean()
    }

    @Test
    fun `Java Ternary argument`() {
        val src: LintDetectorTest.TestFile = LintDetectorTest.java(
            """
            package com.demian.customlint;

            import com.demian.eventlog.EventLog;
            class JavaTest {
                void a() {
                    boolean b = false;
            
                    b(b ? EventLog.SCREEN01.event(1) : EventLog.SCREEN01.event(2));
                }
                
                void b(EventLog.Builder builder) {
                
                }
            } 
            """.trimIndent()
        ).within("src")

        lint().files(EventLogDetectorTestStubs.EventLogStub, src)
            .allowMissingSdk().issues(EventLogDetector2.ISSUE)
            .run()
            .expectClean()
    }

    @Test
    fun `Assigned as variable and logged`() {
        val src: LintDetectorTest.TestFile = LintDetectorTest.kotlin(
            """
            import com.demian.eventlog.EventLog
            class Main() {
                fun a() {
                    val a = EventLog.SCREEN01.event(1).param("a", "1")
                     a.log()
                }
            } 
            """.trimIndent()
        ).within("src")

        lint().files(EventLogDetectorTestStubs.EventLogStub, src)
            .allowMissingSdk().issues(EventLogDetector2.ISSUE)
            .run()
            .expectClean()
    }

    @Test
    fun `Assigned as variable and not logged`() {
        val src: LintDetectorTest.TestFile = LintDetectorTest.kotlin(
            """
            import com.demian.eventlog.EventLog
            class Main() {
                fun a() {
                    val a = EventLog.SCREEN01.event(1).param("a", "1")
                    EventLog.SCREEN01.event(1).param("a", "1").log()
                }
            } 
            """.trimIndent()
        ).within("src")

        lint().files(EventLogDetectorTestStubs.EventLogStub, src)
            .allowMissingSdk().issues(EventLogDetector2.ISSUE)
            .run()
            .expectErrorCount(1)
    }

    @Test
    fun `Returned`() {
        val src: LintDetectorTest.TestFile = LintDetectorTest.kotlin(
            """
            import com.demian.eventlog.EventLog
            class Main() {
                fun a(): EventLog.Builder {
                    return EventLog.SCREEN01.event(1).param("a", "1")
                }
            } 
            """.trimIndent()
        ).within("src")

        lint().files(EventLogDetectorTestStubs.EventLogStub, src)
            .allowMissingSdk().issues(EventLogDetector2.ISSUE)
            .run()
            .expectClean()
    }

    @Test
    fun `Assigned and returned`() {
        val src: LintDetectorTest.TestFile = LintDetectorTest.kotlin(
            """
            import com.demian.eventlog.EventLog
            class Main() {
                fun a(): EventLog.Builder {
                    val a = EventLog.SCREEN01.event(1).param("a", "1")
                    return a
                }
            } 
            """.trimIndent()
        ).within("src")

        lint().files(EventLogDetectorTestStubs.EventLogStub, src)
            .allowMissingSdk().issues(EventLogDetector2.ISSUE)
            .run()
            .expectClean()
    }
}