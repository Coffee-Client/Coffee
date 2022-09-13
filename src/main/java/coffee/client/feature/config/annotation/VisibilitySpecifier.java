/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. Some rights reserved.
 */

package coffee.client.feature.config.annotation;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Repeatable(VisibilitySpecifiers.class)
public @interface VisibilitySpecifier {
    String value();
}
