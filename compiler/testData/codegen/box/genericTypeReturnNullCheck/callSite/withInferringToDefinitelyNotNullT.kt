// !LANGUAGE: +GenerateNullChecksForGenericTypeReturningFunctions
// TARGET_BACKEND: JVM
// WITH_RUNTIME

fun <T> foo(): T = null as T

fun <T> test(x: T) {
    var y = x!!
    y = foo()
}

fun box(): String {
    test<String?>("")
    return "OK"
}