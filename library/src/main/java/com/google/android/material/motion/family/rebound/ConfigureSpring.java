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

import com.google.android.material.motion.runtime.Performer;
import com.google.android.material.motion.runtime.Plan;

/**
 * Configure the spring traits for a given property. Affects the spring behavior of the {@link
 * SpringTo} plan.
 */
public class ConfigureSpring extends Plan {

  /**
   * Denotes that the tension or friction is not set and should not change the spring's
   * corresponding value.
   */
  public static final float UNSET = -1f;

  /**
   * The property whose spring traits should be configured.
   */
  public final ReboundProperty property;

  /**
   * The tension coefficient for the property's spring.
   *
   * If {@link #UNSET}, the spring's tension will not be changed.
   */
  public float tension = UNSET;
  /**
   * The friction coefficient for the property's spring.
   *
   * If {@link #UNSET}, the spring's friction will not be changed.
   */
  public float friction = UNSET;

  /**
   * Initializes a ConfigureSpring plan for the given property.
   */
  public ConfigureSpring(ReboundProperty property) {
    this.property = property;
  }

  @Override
  public Class<? extends Performer> getPerformerClass() {
    return ReboundPerformer.class;
  }
}
