package com.demian.customlint

import android.content.Context
import android.widget.Toast

fun test(context: Context) {
    Toast.makeText(context, "Not called toast", Toast.LENGTH_SHORT)
    show()
}

fun show() = "This is not Toast.show()"


