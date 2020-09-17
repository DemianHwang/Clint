package com.demian.clint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.detector.api.Issue
import com.demian.clint.detector.EventLogDetector2

class IssueRegistry : IssueRegistry() {

    override val issues: List<Issue>
        get() = listOf(
            EventLogDetector2.ISSUE
        )

    override val api: Int = CURRENT_API
}