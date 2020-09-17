package com.demian.clint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.checks.infrastructure.LintDetectorTest.kotlin

object EventLogDetectorTestStubs {
    val EventLogStub: LintDetectorTest.TestFile = kotlin(
        """
package com.demian.eventlog

enum class EventLog {
    SCREEN01,
    SCREEN02;

    fun event(event: Int): Builder {
        return Builder(name, event)
    }

    inner class Builder(val screen: String, val event: Int) {
        fun param(key: String, value: String?): Builder {
            return this
        }

        fun log() {
        }
    }
}

        """
    ).indented().within("src")

    val TrackerExtension: LintDetectorTest.TestFile = kotlin(
        """
            package com.kakao.talk.tracker

            import com.kakao.talk.singleton.Tracker

            interface ExtraMetaTrackable {
                fun Tracker.TrackerBuilder.metaDummy(): Tracker.TrackerBuilder {
                    return this.meta("dummy", "1")
                }
            }

        """
    ).indented().within("src")
}