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

import android.support.v4.util.SimpleArrayMap;
import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringSystem;
import com.google.android.material.motion.runtime.Performer;
import com.google.android.material.motion.runtime.Performer.ContinuousPerformance;
import com.google.android.material.motion.runtime.Performer.PlanPerformance;
import com.google.android.material.motion.runtime.Plan;

/**
 * A performer that instantiates and manages {@link Spring Rebound springs}. A separate spring
 * instance is used for every animating {@link ReboundProperty property}.
 */
public class ReboundPerformer extends Performer implements PlanPerformance, ContinuousPerformance {

  /**
   * Use a single spring system for all rebound performers. This allows all springs to use the same
   * integration loop.
   */
  private static final SpringSystem springSystem = SpringSystem.create();

  private final SimpleArrayMap<Spring, IsActiveToken> tokens = new SimpleArrayMap<>();
  private final SimpleArrayMap<ReboundProperty, Spring> springs = new SimpleArrayMap<>();
  private IsActiveTokenGenerator isActiveTokenGenerator;

  @Override
  public void setIsActiveTokenGenerator(IsActiveTokenGenerator isActiveTokenGenerator) {
    this.isActiveTokenGenerator = isActiveTokenGenerator;
  }

  @Override
  public void addPlan(Plan plan) {
    if (plan instanceof SpringTo) {
      addSpringTo((SpringTo) plan);
    } else {
      throw new IllegalArgumentException("Plan type not supported for " + plan);
    }
  }

  private void addSpringTo(SpringTo plan) {
    float currentFraction = plan.property.getFraction(getTarget());
    Object destination = plan.destination;
    //noinspection unchecked
    float destinationFraction = plan.property.converter.convert(destination);

    Spring spring = getSpring(plan.property);

    if (plan.configuration != null) {
      spring.getSpringConfig().tension = plan.configuration.tension;
      spring.getSpringConfig().friction = plan.configuration.friction;
    }

    if (!eq(spring.getCurrentValue(), currentFraction, EPSILON)) {
      boolean setAtRest = true;
      //noinspection ConstantConditions
      spring.setCurrentValue(currentFraction, setAtRest);
    }

    spring.setEndValue(destinationFraction);
  }

  private Spring getSpring(final ReboundProperty property) {
    Spring spring = springs.get(property);

    if (spring == null) {
      spring = springSystem.createSpring();
      spring.getSpringConfig().tension = SpringTo.DEFAULT_TENSION;
      spring.getSpringConfig().friction = SpringTo.DEFAULT_FRICTION;
      spring.addListener(lifecycleListener);
      spring.addListener(new SimpleSpringListener() {
        @Override
        public void onSpringUpdate(Spring spring) {
          float fraction = (float) spring.getCurrentValue();
          property.setFraction(getTarget(), fraction);
        }
      });
      springs.put(property, spring);
    }

    return spring;
  }

  private final SimpleSpringListener lifecycleListener = new SimpleSpringListener() {

    @Override
    public void onSpringActivate(Spring spring) {
      if (tokens.put(spring, isActiveTokenGenerator.generate()) != null) {
        throw new IllegalStateException("Spring activated twice before it entered resting state.");
      }
    }

    @Override
    public void onSpringAtRest(Spring spring) {
      tokens.remove(spring).terminate();
    }
  };

  private static final double EPSILON = 0.01f;

  /**
   * Fuzzy equal to for floats.
   *
   * <p>Returns true if {@code a} is equal to {@code b}, allowing for {@code epsilon} error due to
   * limitations in floating point accuracy.
   *
   * <p>Does not handle overflow, underflow, infinity, or NaN.
   */
  private static boolean eq(double a, double b, double epsilon) {
    return Math.abs(a - b) <= epsilon;
  }
}
