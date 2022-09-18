package gui.jtablereflection.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ReflectionTable {
    enum ColumnOrderInClass {AS_DESCRIBED_IN_THE_CLASS, CUSTOM_ORDER};
    enum HierarchicalColumnOrder {FROM_PARENT_TO_CHILD, FROM_CHILD_TO_PARENT, IGNORE_THIS_PARAMETER}
    ColumnOrderInClass columnOrderInClass() default  ColumnOrderInClass.AS_DESCRIBED_IN_THE_CLASS;
    // данный параметр может быть установлен только у
    HierarchicalColumnOrder hierarchicalColumnOrder() default HierarchicalColumnOrder.IGNORE_THIS_PARAMETER;
}
