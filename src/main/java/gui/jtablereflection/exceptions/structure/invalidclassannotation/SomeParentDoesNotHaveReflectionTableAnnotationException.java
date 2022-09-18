package gui.jtablereflection.exceptions.structure.invalidclassannotation;

public class SomeParentDoesNotHaveReflectionTableAnnotationException extends ClassMustBeAnnotatedReflectionTableException {
    public SomeParentDoesNotHaveReflectionTableAnnotationException(Class parentForProvidedClass) {
        super(parentForProvidedClass);
    }
}
