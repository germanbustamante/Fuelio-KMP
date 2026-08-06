import SwiftUI

/// `@State` for values that are expensive to create.
///
/// `@State`'s initial value is evaluated on **every** `View` struct initialization; SwiftUI keeps the
/// first one and throws the rest away. That is harmless for a `String`, but a screen store resolves a
/// Kotlin ViewModel out of Koin, starts its `viewModelScope` and fires a screen-view analytics event
/// — so with a plain `@State` every re-render of the parent (each navigation push, for instance)
/// would build and immediately discard a full ViewModel.
///
/// This wrapper puts only a factory closure in `@State` and creates the value the first time it is
/// read, which happens at most once per view identity.
///
/// (iOS 27 makes `@State` a macro with exactly this lazy behaviour for stored classes; this wrapper
/// is what gets the same guarantee on the iOS 18.2 deployment target.)
@propertyWrapper
struct LazyStore<Value: AnyObject>: DynamicProperty {

    @State private var box: Box

    init(_ make: @escaping @MainActor () -> Value) {
        _box = State(wrappedValue: Box(make))
    }

    @MainActor
    var wrappedValue: Value { box.value }

    private final class Box {

        private let make: @MainActor () -> Value
        private var stored: Value?

        init(_ make: @escaping @MainActor () -> Value) {
            self.make = make
        }

        @MainActor
        var value: Value {
            if let stored { return stored }
            let created = make()
            stored = created
            return created
        }
    }
}
