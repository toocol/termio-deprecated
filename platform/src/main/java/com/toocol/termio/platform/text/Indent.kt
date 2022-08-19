package com.toocol.termio.platform.text

class Indent {
    @JvmField
    var width = 15.0
    @JvmField
    var level = 1

    internal constructor() {}
    internal constructor(level: Int) {
        if (level > 0) this.level = level
    }

    fun increase(): Indent {
        return Indent(level + 1)
    }

    fun decrease(): Indent {
        return Indent(level - 1)
    }

    override fun toString(): String {
        return "indent: $level"
    }
}