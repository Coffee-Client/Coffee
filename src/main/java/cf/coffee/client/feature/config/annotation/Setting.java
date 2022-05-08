package cf.coffee.client.feature.config.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Setting {
    String name();

    String description() default "No description";

    double min() default -1d;

    double max() default -1d;

    int precision() default -1;
}
