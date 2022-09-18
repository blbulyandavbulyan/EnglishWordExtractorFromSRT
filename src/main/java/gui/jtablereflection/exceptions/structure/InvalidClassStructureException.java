package gui.jtablereflection.exceptions.structure;

import gui.jtablereflection.exceptions.ReflectionTableException;

public class InvalidClassStructureException extends ReflectionTableException {
    private final Class providedClass;

    public InvalidClassStructureException(Class providedClass) {
        this.providedClass = providedClass;
    }

    public Class getProvidedClass() {
        return providedClass;
    }
}
