package jtablereflection.exceptions.invalidprovidedclass;

import jtablereflection.exceptions.ReflectionTableException;

public class ClassMustHaveOneOrMoreAnnotatedTheColumnAnnotationException extends ReflectionTableException {
    private final Class providedClass;

    public ClassMustHaveOneOrMoreAnnotatedTheColumnAnnotationException(Class providedClass) {
        this.providedClass = providedClass;
    }
    public Class getProvidedClass() {
        return providedClass;
    }
}
