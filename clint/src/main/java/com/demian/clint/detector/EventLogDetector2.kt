package com.demian.clint.detector

import com.android.tools.lint.checks.DataFlowAnalyzer
import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.*
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiVariable
import org.jetbrains.uast.*
import java.util.concurrent.atomic.AtomicBoolean


class EventLogDetector2 : Detector(), SourceCodeScanner {

    override fun createUastHandler(context: JavaContext): UElementHandler? {
        println(context.uastFile?.asRecursiveLogString())
        return super.createUastHandler(context)
    }

    override fun getApplicableMethodNames(): List<String>? {
        return listOf("event", "a")
    }

    fun UElement.lastParenthesize(): UElement? {
        var current: UElement? = this
        while (current?.uastParent is UParenthesizedExpression) {
            current = current.uastParent
        }
        return current
    }

    override fun visitMethodCall(context: JavaContext, call: UCallExpression, method: PsiMethod) {
        if (!context.evaluator.isMemberInClass(method, "com.demian.eventlog.EventLog"))
            return

        println(context.uastFile?.asRecursiveLogString())

        printDebug("visitMethodCall ${call.asSourceString()}")


        val surroundingDeclaration: UElement = call.getParentOfType(true, UMethod::class.java)
            ?: return

        val surroundingBinaryExpression: UBinaryExpression? =
            call.getParentOfType(true, UBinaryExpression::class.java)
        surroundingBinaryExpression?.let {
            if (it.operator == UastBinaryOperator.EQUALS || it.operator == UastBinaryOperator.NOT_EQUALS)
                return@visitMethodCall
        }

//        val surroundingSwitch: UElement? = call.getParentOfType(true, KotlinUSwitchExpression::class.java)
//        val surroundingIf: UElement? = call.getParentOfType(true, KotlinUIfExpression::class.java) 더이상 필요 없음
        var surroundingTernary: UElement? =
            call.getParentOfType(true, UIfExpression::class.java)?.lastParenthesize()

        var initialElement = listOfNotNull(
            surroundingTernary,
            call
        )

        val finder = LogFinder(initialElement, emptyList())
        surroundingDeclaration.accept(finder)
        if (!finder.tracked.get() && !finder.escapes.get()) {
            printDebug("report")
            val quickFix = LintFix.create()
                .name("call .log()")
                .replace()
                .end()
                .with(".log()")
                .robot(true)
                .independent(true)
                .build()

            context.report(
                ISSUE, call, context.getCallLocation(
                    finder.call ?: call,
                    includeReceiver = true,
                    includeArguments = true
                ), "EventLog created but not logged: did you forget to call `log()` ?",
                quickFix
            )
        }
    }

    private class LogFinder(
        initial: Collection<UElement>,
        initialReferences: Collection<PsiVariable>
    ) : DataFlowAnalyzer(initial, initialReferences) {
        val tracked = AtomicBoolean(false)
        val escapes = AtomicBoolean(false)
        var call: UCallExpression? = null

        override fun receiver(call: UCallExpression) {
            if (getMethodName(call) == "log") {
                printDebug("tracked ${getMethodName(call)}")
                tracked.set(true)
            }

            super.receiver(call)
        }

        override fun returns(expression: UReturnExpression) {
            printDebug("return $expression")
            escapes.set(true)
        }

        override fun argument(call: UCallExpression, reference: UElement) {
            printDebug("argument call : ${getMethodName(call)}, reference : ${reference.asSourceString()}")
            escapes.set(true)
        }

        override fun field(field: UElement) {
            printDebug("field : $field")
        }

        //DataFlowAnalyzer 구현은 returnType과 containingClass를 비교하는데 extension fun의 경우에는 containing class가 달라져서 체크가 안됨.
        override fun returnsSelf(call: UCallExpression): Boolean {
            return (call.returnType as? PsiClassType)?.resolve()?.qualifiedName == EVENTLOG_BUILDER_NAME
        }

        //returnSelf한게 Argument로 넘어갈때의 처리.
        override fun afterVisitCallExpression(node: UCallExpression) {
            for (expression in node.valueArguments) {
                if (instances.contains(expression)) {
                    argument(node, expression)
                } else if (expression is UReferenceExpression) {
                    val resolved = expression.resolve()

                    if (resolved != null && references.contains(resolved)) {
                        argument(node, expression)
                        break
                    }
                }
            }
            super.afterVisitCallExpression(node)
        }
    }

    companion object {
        const val EVENTLOG_BUILDER_NAME = "com.demian.eventlog.EventLog.Builder"

        var printDebug = false

        fun printDebug(message: String) {
            if (printDebug)
                println(message)
        }

        val ISSUE: Issue = Issue.create(
            id = "EventLogLogged2",
            briefDescription = "EventLog is not logged",
            explanation = "`EventLog.event()` creates a `EventLog` but does **not** log it. You must call "
                    + "`log()` on the resulting object to actually make the `EventLog` logged.",
            category = Category.CORRECTNESS,
            priority = 6,
            severity = Severity.ERROR,
            implementation = Implementation(EventLogDetector2::class.java, Scope.JAVA_FILE_SCOPE)
        )
    }
}