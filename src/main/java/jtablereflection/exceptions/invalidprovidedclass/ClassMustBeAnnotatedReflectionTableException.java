package jtablereflection.exceptions.invalidprovidedclass;


import jtablereflection.exceptions.ReflectionTableException;

public class ClassMustBeAnnotatedReflectionTableException extends ReflectionTableException {
    private final Class providedClass;
    public ClassMustBeAnnotatedReflectionTableException(Class providedClass) {
        this.providedClass = providedClass;
    }

    public Class getProvidedClass() {
        return providedClass;
    }
}
