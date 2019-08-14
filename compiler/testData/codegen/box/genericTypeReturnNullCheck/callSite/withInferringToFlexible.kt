// !LANGUAGE: +GenerateNullChecksForGenericTypeReturningFunctions
// TARGET_BACKEND: JVM
// WITH_RUNTIME

// FILE: J.java
public class J<T> {
    public T bar() { return null; }
}

// FILE: K.kt
fun <T> foo(): T = null as T

fun test() {
    var y = J<Int>().bar()
    y = foo()
}

fun box(): String {
    test()
    return "OK"
}