package cxhttp.annotation

@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
    message = "This API is internal in CxHttp and should not be used. It could be removed or changed without notice."
)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.TYPEALIAS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FIELD,
    AnnotationTarget.CONSTRUCTOR
)
internal annotation class InternalAPI
