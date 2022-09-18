package gui.jtablereflection.exceptions.structure.invalidclassannotation;

import gui.jtablereflection.exceptions.structure.InvalidClassStructureException;

public class InvalidClassAnnotationException extends InvalidClassStructureException {
    public InvalidClassAnnotationException(Class providedClass) {
        super(providedClass);
    }
}
