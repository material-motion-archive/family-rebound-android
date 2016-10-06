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
   * The property whose spring traits should be configured.
   */
  public final ReboundProperty property;

  /**
   * The tension coefficient for the property's spring.
   */
  public float tension;
  /**
   * The friction coefficient for the property's spring.
   */
  public float friction;

  /**
   * Initializes a ConfigureSpring plan for the given property.
   */
  public ConfigureSpring(ReboundProperty property, float tension, float friction) {
    this.property = property;
    this.tension = tension;
    this.friction = friction;
  }

  @Override
  public Class<? extends Performer> getPerformerClass() {
    return ReboundPerformer.class;
  }
}
