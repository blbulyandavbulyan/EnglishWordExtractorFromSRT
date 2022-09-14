package jtablereflection.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ReflectionTable {
    enum ColumnOrder {AS_DESCRIBED_IN_THE_CLASS, CUSTOM_ORDER};
    ColumnOrder columnOrder() default  ColumnOrder.AS_DESCRIBED_IN_THE_CLASS;
}
