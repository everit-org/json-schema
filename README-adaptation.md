
A proposal for supporting the JSON-P types of the `javax.json` package.


## Motivation

The JSON-P types provide the underlying JSON abstraction for applications 
that utilize Java EE 7 or Java EE 8. In both EE 7 and 8 these types are 
commonly employed in building REST APIs using JAX-RS, where high quality 
JSON schema validation support for the Everit library would provide great 
value. Java SE applications using the Jersey REST server or client 
components would similarly benefit from JSON schema validation support 
provided by the Everit libary.

As JSON schema continues to make progress on the IETF standards track, its 
use with the Java platform will need to be standardized through the Java 
Community Process. Supporting the JSON-P types in this library will 
provide an example implementation of JSON schema support for Java that fits 
naturally within the existing support for JSON-P and JSON-B, and will 
position the author(s) of the Everit library to significantly influence 
the direction of the standardization process for JSON Schema support in 
Java.

## Goals

1. Provide a means for the Everit JSON Schema library to support 
   validation of JSON values (structures and scalars) using the JSON-P 
   types from JSR-374 (and its predecessor JSR-353) in the `javax.json`
   package.
2. Ensure full backwards compatibility for use of the library with 
   `org.json` types.
3. Minimize performance impacts of supporting more than one JSON types
   representation.
4. Provide an SPI that would facilitate later adaptation to future 
   specifications as well as other Java JSON implementations (e.g. Jackson)
5. Allow the use of alternative JSON-P type systems to be configurable 
   at runtime.

## Non-Goals

1. Replacement of the underlying JSON type system used to represent JSON 
   Schema structures in the library.
2. Implementation of support for type systems other than the existing 
   `org.json` and the JSON-P types.

## Analysis

Using the current releases of the Everit JSON Schema library, one
supported approach for using a JSON type system other than that provided 
by `org.json` is _translation_. In an application that uses the JSON-P 
types in `javax.json`, it is necessary to transform JSON structures from 
the JSON-P types to instances of `org.json.JSONObject` and/or 
`org.json.JSONArray`. While the translation itself is reasonably 
straightforward, it is a very expensive approach and may not be feasible
in applications that need to support JSON schema validation operations at 
a high volume per unit time.

Another potential approach would be to adapt the JSON-P data types to the
API provided by the `org.json` types. This approach is significantly 
complicated by the fact that these types are concrete classes rather than
interfaces. Any such approach would involve creating new concrete
subtypes that override virtually all of the functionality of the base types
without violating the contract of the base types. This is further 
exacerbated by the fact that these types expose a very large number of
public methods which would need overrides. It is not clear that these
types were designed to be subclassed in this manner, and there may well be
implicit assumptions that are inherent to the design of these types that
make such an approach infeasible.

After extensive study of the architecture and implementation of the Everit 
library, it becomes clear that there exists an (implicit) contract between 
the validation implementation and the JSON types provided by `org.json`;
i.e. that while the `org.json` types provide a large API surface, only a 
small subset of this is actually used in the validation implementation.
Moreover the scope of implementation classes that utilize this implicit
contract is relatively small due to the visitor-based relationship
between the JSON schema abstraction and the classes that implement 
validation.

In this contract, the JSON type system is assumed to provide a JSON object 
type with a very minimal `Map`-like interface. Similarly, the JSON array 
type is assumed to provide a minimal `List`-like interface. Lastly, it is 
assumed that the JSON type uses the _wrapper_ form of the intrinsic Java 
types for strings, booleans, numbers. With respect to nullity, it assumes 
that the JSON type system supports not only the intrinsic Java `null` but 
also a _null object_; i.e. an instance of some Java type that represents 
the null JSON value.

With this understanding it becomes clear that by creating a service 
provider interface (SPI) that describes the contract between the JSON type 
system and the validation components, it would then be possible to substitute
a different JSON type system. The SPI will act as a thin adaptation layer 
between the validation components and the JSON type system. At the 
expense of some additional method call dispatches through the adapter 
layer at runtime, the validation components can easily support a different
JSON type system.

It is important to note that in this approach, neither the interface nor
the implementation of the schema components of the library are changed.
The `Schema` type and its related components are treated as a black box 
and its underlying JSON type is irrelevant. Only the validation components
need to support adaptation to a different JSON type system.

## Proposal

The proposal represented in the code of this pull request is as follows.

1. Introduce a `JsonAdaptation` service provider interface (SPI), that
   represents the relatively small contract between validation components
   and the JSON type system.
2. Implement the SPI using the `org.json` types and use this adaptation
   as the default provider in the validation components. This ensures that
   existing uses of the library are unaffected.
3. Introduce an implementation of the SPI using the JSON-P types of
   the `javax.json` package and provide the means to configure the 
   `ValidatorBuilder` to use this as an alternative to the default 
   adaptation to the `org.json` types.

As can be observed in reviewing the pull request, the amount of change
required in the validation components is small and highly targeted. By
defining the SPI according to the implicit contract between validation
components and the `org.json` types, very little change is needed in most
of the validation components.

Moreover, using this approach the only required change to existing unit
tests for the library is to acknowledge the default `org.json` adaptation
in the `ValidatingVisitorTest`; i.e. passing an additional argument to 
to `ValidatingVisitor` constructor invocations in the test. With 
_no other changes_ to the test suite, **all unit tests pass**. Assuming 
that test coverage is reasonably complete, this should give some measure 
of confidence that the introduction of the adaptation layer does not 
change the externally visible behavior of the validation components.


## Performance Considerations

Adapting the library to support another type system will inevitably involve
some trade-off of performance for flexibility. But the approach taken here
aims to minimize this impact. In the default `org.json` adaptation additional
method call dispatches through the adaptation layer should introduce 
reasonably small additional overhead. These adapters are quite simple with 
minimal branching logic, and conditional expressions that involve few operands 
and corresponding fetches. Performance profiling could be used to validate 
these assertions.

An additional performance improvement could be made and has been marked with
a TODO in the implementation. In the `getFailureOfSchema` method of 
`ValidatingVisitor`, the adapted JSON value received as input from the caller
is passed to the adaptation layer to be inverted (un-adapted, so to speak).
This is done because there are a handful of test cases that make assumptions
that are violated by the presence of adapter types. This approach was used to
avoid modifying the tests at the same time the adaptation layer is introduced,
to minimize risks. However, after some successful experience with the 
adaptation layer, it would be desirable to revisit the design of these tests
to avoid the additional call back into the adaptation layer in this method.















   

