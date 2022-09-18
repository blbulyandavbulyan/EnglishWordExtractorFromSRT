package gui.jtablereflection.exceptions.structure.invalidfieldannotation;

public class SomeParentClassDoesNotHaveFieldsAnnotatedReflectionTableColumn extends ClassMustHaveOneOrMoreAnnotatedTheColumnAnnotationException {
    public SomeParentClassDoesNotHaveFieldsAnnotatedReflectionTableColumn(Class parentClass) {
        super(parentClass);
    }
}
