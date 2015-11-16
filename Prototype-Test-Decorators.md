# Writing Extensions for JUnit 5

## Test Decorators as a General Principle

## Decorating Point: Parameter Resolution

If there is a method parameter, it needs to be _resolved_ at runtime by a [`MethodParameterResolver`]. A `MethodParameterResolver` can either be built-in or registered by the user (see the extension model for further details). Generally speaking, parameters may be resolved by *type* or by *annotation*. For concrete examples, consult the source code for [`CustomTypeParameterResolver`] and [`CustomAnnotationParameterResolver`], respectively.


## Other Planned Decorating Points
