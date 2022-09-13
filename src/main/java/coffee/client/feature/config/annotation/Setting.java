/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. Some rights reserved.
 */

package coffee.client.feature.config.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Setting {
    String name();

    String description() default "No description";

    double min() default -1d;

    double max() default -1d;

    double upperMin() default -1d;

    double upperMax() default -1d;

    int precision() default -1;
}
