// !LANGUAGE: +CapturedInClosureSmartCasts

fun run(f: () -> Unit) = f()

fun foo(s: String?) {
    var x: String? = null
    if (s != null) {
        x = s
    }
    if (x != null) {
        run {
            x.hashCode()
        }
    }
}

fun bar(s: String?) {
    var x = s
    if (x != null) {
        run {
            x.hashCode()
        }
    }
}

fun baz(s: String?) {
    var x = s
    if (x != null) {
        run {
            x.hashCode()
        }
        run {
            x.hashCode()
            x = null
        }
    }
}

fun gaz(s: String?) {
    var x = s
    if (x != null) {
        run {
            x.hashCode()
            x = null
        }
        run {
            x.<!INAPPLICABLE_CANDIDATE!>hashCode<!>()
        }
    }
}

fun gav(s: String?) {
    var x = s
    if (x != null) {
        run {
            x.hashCode()
        }
        x = null
    }
}