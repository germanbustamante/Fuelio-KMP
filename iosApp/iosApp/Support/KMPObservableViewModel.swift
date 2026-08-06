import CorePresentation
import KMPObservableViewModelCore

// KMP-ObservableViewModel ships in two halves: the Kotlin base class, which arrives inside
// `CorePresentation.framework`, and the Swift `ViewModel` protocol that `@StateViewModel` requires,
// which comes from the SPM package. Neither half can declare the conformance between them, so the
// library asks every consumer to state it once — this file is that statement.
//
// Both types are literally named `ViewModel`, hence the module qualifiers.
extension CorePresentation.ViewModel: @retroactive KMPObservableViewModelCore.ViewModel {}
