package com.demian.clint.detector

import com.android.tools.lint.detector.api.*
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.*
import org.jetbrains.uast.visitor.AbstractUastVisitor


class EventLogDetector1 : Detector(), SourceCodeScanner {

    override fun getApplicableMethodNames(): List<String>? {
        return listOf("event")
    }

    override fun visitMethodCall(context: JavaContext, call: UCallExpression, method: PsiMethod) {
        if (!context.evaluator.isMemberInClass(method, "com.demian.customlint.logger.EventLog"))
            return

        println("${call.asSourceString()}")


        val surroundingDeclaration: UElement = call.getParentOfType(
            true,
            UMethod::class.java,
            UBlockExpression::class.java,
            ULambdaExpression::class.java
        )
            ?: return

        println("surroundingDeclaration : $surroundingDeclaration")

        val parent: UElement? = call.uastParent

        println("parent : ${parent?.javaClass?.kotlin}")
        println("parent : ${parent?.uastParent?.javaClass?.kotlin}")

        if (parent?.uastParent is UCallExpression)
            return

//        if (parent is UMethod || parent is UReferenceExpression || parent?.uastParent is UMethod) {
////        kotlin expression body
//            return
//        }

        val finder = LogFinder(call)
        surroundingDeclaration.accept(finder)
        if (!finder.isLogCalled()) {
            println("report")
            context.report(
                ISSUE, call, context.getCallLocation(
                    call,
                    includeReceiver = true,
                    includeArguments = true
                ), "EventLog created but not logged: did you forget to call `log()` ?"
            )
        }
    }

    private class LogFinder(val target: UCallExpression) : AbstractUastVisitor() {
        var found = false
        var seenTarget = false

        init {
            println("target : " + target.uastParent?.asSourceString())
        }

        override fun visitCallExpression(node: UCallExpression): Boolean {
            println("visit : ${node.uastParent?.asSourceString()} ${getMethodName(node)}")
            if (node == target || node.psi != null && node.psi == target.psi) {
                seenTarget = true
            } else {
                if ((seenTarget || target.equals(node.receiver)) && "log".equals(
                        getMethodName(
                            node
                        )
                    )
                ) {
                    println("found : ${getMethodName(node)}")
                    found = true
                }
            }
            return super.visitCallExpression(node)
        }

        override fun visitReturnExpression(node: UReturnExpression): Boolean {
            if (target.isUastChildOf(node.returnExpression, true)) {
                println("found : return $node")
                found = true
            }
            return super.visitReturnExpression(node)
        }

        fun isLogCalled(): Boolean {
            return found
        }
    }

    companion object {
        val ISSUE: Issue = Issue.create(
            id = "EventLogLogged",
            briefDescription = "EventLog is not logged",
            explanation = "`EventLog.event()` creates a `EventLog` but does **not** log it. You must call "
                    + "`log()` on the resulting object to actually make the `EventLog` logged.",
            category = Category.CORRECTNESS,
            priority = 6,
            severity = Severity.ERROR,
            implementation = Implementation(EventLogDetector1::class.java, Scope.JAVA_FILE_SCOPE)
        )
    }
}