/*
 * Copyright 2016-present The Material Motion Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.material.motion.family.rebound;

import android.support.annotation.Nullable;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.google.android.material.motion.runtime.Performer;
import com.google.android.material.motion.runtime.Plan;

/**
 * Pull a {@link ReboundProperty property} towards a specific value using a {@link Spring Rebound
 * spring}. When multiple plans are added to the same property, the last-registered plan's {@link
 * #destination} will be used.
 */
public class SpringTo<V> extends Plan {

  /**
   * The default tension.
   *
   * Default extracted from a {@link SpringConfig rebound spring config} with origami tension = 40
   * and origami friction = 7.
   */
  public static final float DEFAULT_TENSION = 342f;
  /**
   * The default friction.
   *
   * Default extracted from a {@link SpringConfig rebound spring config} with origami tension = 40
   * and origami friction = 7.
   */
  public static final float DEFAULT_FRICTION = 30f;

  /**
   * The property whose value should be pulled towards the destination.
   */
  public final ReboundProperty<V> property;
  /**
   * The value to which the property should be pulled.
   */
  public V destination;

  /**
   * The spring's desired configuration.
   *
   * If null then the spring's configuration will not be affected.
   */
  @Nullable
  public SpringConfig configuration;

  /**
   * Initializes a SpringTo plan for the property with a destination.
   */
  public SpringTo(ReboundProperty<V> property, V destination) {
    this.property = property;
    this.destination = destination;
  }

  @Override
  public Class<? extends Performer> getPerformerClass() {
    return ReboundPerformer.class;
  }

  @Override
  public Plan clone() {
    //noinspection unchecked
    SpringTo<V> clone = (SpringTo<V>) super.clone();
    if (configuration != null) {
      clone.configuration = new SpringConfig(configuration.tension, configuration.friction);
    }
    return clone;
  }
}
