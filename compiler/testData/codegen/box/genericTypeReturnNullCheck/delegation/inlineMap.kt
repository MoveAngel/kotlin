// !LANGUAGE: +GenerateNullChecksForGenericTypeReturningFunctions
// TARGET_BACKEND: JVM
// WITH_RUNTIME

val x: String by mapOf("x" to null)

fun box(): String {
    try {
        x
    } catch (e: NullPointerException) {
        return "OK"
    }
    return "Fail: NullPointerException should have been thrown"
}