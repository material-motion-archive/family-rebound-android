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

import android.support.annotation.VisibleForTesting;
import android.support.v4.util.SimpleArrayMap;

import com.facebook.rebound.BaseSpringSystem;
import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringSystem;
import com.google.android.material.motion.family.rebound.ReboundProperty.TypeConverterCompat;
import com.google.android.material.motion.gestures.GestureRecognizer;
import com.google.android.material.motion.gestures.GestureRecognizer.GestureStateChangeListener;
import com.google.android.material.motion.runtime.Performer;
import com.google.android.material.motion.runtime.PerformerFeatures.ContinuousPerforming;
import com.google.android.material.motion.runtime.Plan;

import java.util.HashSet;
import java.util.Set;

/**
 * A performer that instantiates and manages {@link Spring Rebound springs}. A separate spring
 * instance is used for every animating {@link ReboundProperty property}.
 */
public class ReboundPerformer<T> extends Performer<T> implements ContinuousPerforming {

  /**
   * Use a single spring system for all rebound performers. This allows all springs to use the
   * same integration loop.
   */
  @VisibleForTesting
  static BaseSpringSystem springSystem = SpringSystem.create();

  private static final double EPSILON = 0.01f;

  @VisibleForTesting
  final SimpleArrayMap<ReboundProperty<? super T, ?>, Spring> springs = new SimpleArrayMap<>();
  private final SimpleArrayMap<GestureRecognizer, Set<ReboundProperty<? super T, ?>>> pausesSpringMap =
    new SimpleArrayMap<>();
  private final SimpleArrayMap<ReboundProperty<? super T, ?>, Set<GestureRecognizer>> pausesSpringInverseMap =
    new SimpleArrayMap<>();

  private final SimpleArrayMap<Spring, Double> pausedEndFractions = new SimpleArrayMap<>();

  private final SimpleArrayMap<Spring, IsActiveToken> tokens = new SimpleArrayMap<>();
  private IsActiveTokenGenerator isActiveTokenGenerator;

  @Override
  public void setIsActiveTokenGenerator(IsActiveTokenGenerator isActiveTokenGenerator) {
    this.isActiveTokenGenerator = isActiveTokenGenerator;
  }

  @Override
  public void addPlan(Plan<T> plan) {
    if (plan instanceof ObjectSpringTo) {
      addSpringTo((ObjectSpringTo<T, ?>) plan);
    } else if (plan instanceof ObjectPausesSpring) {
      addPausesSpring((ObjectPausesSpring<T>) plan);
    } else {
      throw new IllegalArgumentException("Plan type not supported for " + plan);
    }
  }

  private void addSpringTo(ObjectSpringTo<T, ?> plan) {
    Spring spring = getSpring(plan.property);

    if (plan.configuration != null) {
      spring.getSpringConfig().tension = plan.configuration.tension;
      spring.getSpringConfig().friction = plan.configuration.friction;
    }

    TypeConverterCompat converter = plan.property.converter;
    //noinspection unchecked
    float destinationFraction = converter.convert(plan.destination);
    startSpring(spring, plan.property, destinationFraction);
  }

  private Spring getSpring(final ReboundProperty<? super T, ?> property) {
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

  private void startSpring(Spring spring, ReboundProperty<? super T, ?> property, double destinationFraction) {
    float currentFraction = property.getFraction(getTarget());
    if (!eq(spring.getCurrentValue(), currentFraction, EPSILON)) {
      boolean setAtRest = true;
      //noinspection ConstantConditions
      spring.setCurrentValue(currentFraction, setAtRest);
    }

    //noinspection unchecked
    if (!isPropertyPaused(property)) {
      spring.setEndValue(destinationFraction);
    } else {
      pausedEndFractions.put(spring, destinationFraction);
    }
  }

  @VisibleForTesting
  final SimpleSpringListener lifecycleListener = new SimpleSpringListener() {

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

  private void addPausesSpring(ObjectPausesSpring<T> plan) {
    // Add to map.
    Set<ReboundProperty<? super T, ?>> properties = pausesSpringMap.get(plan.gestureRecognizer);
    if (properties == null) {
      properties = new HashSet<>();
      pausesSpringMap.put(plan.gestureRecognizer, properties);
    }
    properties.add(plan.property);

    // Add to inverse map.
    Set<GestureRecognizer> gestureRecognizers = pausesSpringInverseMap.get(plan.property);
    if (gestureRecognizers == null) {
      gestureRecognizers = new HashSet<>();
      pausesSpringInverseMap.put(plan.property, gestureRecognizers);
    }
    gestureRecognizers.add(plan.gestureRecognizer);

    // Add state change listener.
    plan.gestureRecognizer.addStateChangeListener(pausesSpringListener);
  }

  private final GestureStateChangeListener pausesSpringListener = new GestureStateChangeListener() {
    @Override
    public void onStateChanged(GestureRecognizer gestureRecognizer) {
      Set<ReboundProperty<? super T, ?>> properties = pausesSpringMap.get(gestureRecognizer);
      for (ReboundProperty<? super T, ?> property : properties) {
        Spring spring = springs.get(property);
        if (spring != null) {
          switch (gestureRecognizer.getState()) {
            case GestureRecognizer.BEGAN:
              if (!pausedEndFractions.containsKey(spring)) {
                pausedEndFractions.put(spring, spring.getEndValue());
              }
              spring.setAtRest();
              break;
            case GestureRecognizer.RECOGNIZED:
            case GestureRecognizer.CANCELLED:
              startSpring(spring, property, pausedEndFractions.remove(spring));
              break;
          }
        }
      }
    }
  };

  private boolean isPropertyPaused(ReboundProperty<? super T, ?> property) {
    Set<GestureRecognizer> gestureRecognizers = pausesSpringInverseMap.get(property);
    if (gestureRecognizers != null) {
      for (GestureRecognizer gestureRecognizer : gestureRecognizers) {
        switch (gestureRecognizer.getState()) {
          case GestureRecognizer.BEGAN:
          case GestureRecognizer.CHANGED:
            return true;
        }
      }
    }

    return false;
  }

  /**
   * Fuzzy equal to for floats.
   * <p>
   * Returns true if {@code a} is equal to {@code b}, allowing for {@code epsilon} error due to
   * limitations in floating point accuracy.
   * <p>
   * Does not handle overflow, underflow, infinity, or NaN.
   */
  private static boolean eq(double a, double b, double epsilon) {
    return Math.abs(a - b) <= epsilon;
  }
}
