package gui.jtablereflection.exceptions.structure.invalidfieldannotation;

import gui.jtablereflection.exceptions.structure.InvalidClassStructureException;

public class ClassMustHaveOneOrMoreAnnotatedTheColumnAnnotationException extends InvalidClassStructureException {

    public ClassMustHaveOneOrMoreAnnotatedTheColumnAnnotationException(Class providedClass) {
        super(providedClass);
    }
}
