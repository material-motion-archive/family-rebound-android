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

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.facebook.rebound.BaseSpringSystem;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SteppingLooper;
import com.google.android.material.motion.gestures.GestureRecognizer;
import com.google.android.material.motion.gestures.testing.SimulatedGestureRecognizer;
import com.google.android.material.motion.runtime.MotionRuntime;
import com.google.android.material.motion.runtime.Performer;
import com.google.android.material.motion.runtime.PerformerFeatures.ContinuousPerforming.IsActiveToken;
import com.google.android.material.motion.runtime.PerformerFeatures.ContinuousPerforming.IsActiveTokenGenerator;
import com.google.android.material.motion.runtime.Plan;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static com.google.common.truth.Truth.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class ReboundPerformerTests {

  private static final float EPSILON = 0.0001f;
  /**
   * An interval of time that represents one frame (~16ms).
   */
  private static final int FRAME = 16;

  private MotionRuntime runtime;
  private View target;
  private SteppingLooper springLooper;

  private BaseSpringSystem originalSpringSystem;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void setUp() {
    runtime = new MotionRuntime();
    Context context = Robolectric.setupActivity(Activity.class);
    target = new View(context);

    // Prevent springs from using the actual looper, which cripples robolectric.
    originalSpringSystem = ReboundPerformer.springSystem;
    springLooper = new SteppingLooper();
    ReboundPerformer.springSystem = new BaseSpringSystem(springLooper);
  }

  @After
  public void tearDown() {
    ReboundPerformer.springSystem = originalSpringSystem;
  }

  @Test
  public void didChangeTargetValue() {
    target.setAlpha(1f);
    SpringTo<Float> fadeOut = new SpringTo<>(ReboundProperty.ALPHA, 0f);

    runtime.addPlan(fadeOut, target);

    // No change in alpha yet.
    assertThat(target.getAlpha()).isWithin(0).of(1f);

    // Change in alpha after 1 frame.
    stepOnce();
    assertThat(target.getAlpha()).isLessThan(1f);

    // Alpha completely settles to 0f.
    stepUntilSettled();
    assertThat(target.getAlpha()).isWithin(EPSILON).of(0f);
  }

  @Test
  public void didChangeRuntimeState() {
    // Runtime is initially idle.
    assertThat(runtime.getState()).isEqualTo(MotionRuntime.IDLE);

    runtime.addPlan(new SpringTo<>(ReboundProperty.ALPHA, 0f), target);

    // Runtime is still idle - spring not yet activated.
    assertThat(runtime.getState()).isEqualTo(MotionRuntime.IDLE);

    // Runtime is now active.
    stepOnce();
    assertThat(runtime.getState()).isEqualTo(MotionRuntime.ACTIVE);

    // Runtime is idle again when settled.
    stepUntilSettled();
    assertThat(runtime.getState()).isEqualTo(MotionRuntime.IDLE);
  }

  @Test
  public void bouncySpringOvershoots() {
    target.setScaleX(0f);
    target.setScaleY(0f);
    // Set up a spring with reduced friction (more bouncy).
    SpringTo<Float> scaleUp = new SpringTo<>(ReboundProperty.SCALE, 1f);
    scaleUp.configuration =
      new SpringConfig(SpringTo.DEFAULT_TENSION, SpringTo.DEFAULT_FRICTION / 2);

    runtime.addPlan(scaleUp, target);

    // Track whether the target value ever shoots beyond 1f.
    boolean overshoot = false;
    boolean idle = false;
    while (!idle) {
      idle = springLooper.step(FRAME);
      if (target.getScaleX() > 1f) {
        overshoot = true;
      }
    }

    assertThat(overshoot).isTrue();
  }

  @Test
  public void overDampedSpringDoesNotOvershoot() {
    target.setScaleX(0f);
    target.setScaleY(0f);
    // Set up a spring with increased friction (less bouncy).
    SpringTo<Float> scaleUp = new SpringTo<>(ReboundProperty.SCALE, 1f);
    scaleUp.configuration =
      new SpringConfig(SpringTo.DEFAULT_TENSION, SpringTo.DEFAULT_FRICTION * 2);

    runtime.addPlan(scaleUp, target);

    // Track whether the target value ever shoots beyond 1f.
    boolean overshoot = false;
    boolean idle = false;
    while (!idle) {
      idle = springLooper.step(FRAME);
      if (target.getScaleX() > 1f) {
        overshoot = true;
      }
    }

    assertThat(overshoot).isFalse();
  }

  @Test
  public void reuseSpringForSameProperty() {
    ReboundPerformer performer = createReboundPerformer();

    assertThat(performer.springs.size()).isEqualTo(0);

    performer.addPlan(new SpringTo<>(ReboundProperty.ALPHA, 0f));
    assertThat(performer.springs.size()).isEqualTo(1);

    // Same property.
    performer.addPlan(new SpringTo<>(ReboundProperty.ALPHA, 1f));
    assertThat(performer.springs.size()).isEqualTo(1);
  }

  @Test
  public void createSpringForNewProperty() {
    ReboundPerformer performer = createReboundPerformer();

    assertThat(performer.springs.size()).isEqualTo(0);

    performer.addPlan(new SpringTo<>(ReboundProperty.ALPHA, 0f));
    assertThat(performer.springs.size()).isEqualTo(1);

    // Different property.
    performer.addPlan(new SpringTo<>(ReboundProperty.SCALE, 0f));
    assertThat(performer.springs.size()).isEqualTo(2);
  }

  @Test(expected = IllegalArgumentException.class)
  public void unsupportedPlanThrowsException() {
    ReboundPerformer performer = createReboundPerformer();

    performer.addPlan(new UnsupportedPlan());
  }

  @Test
  public void lifecycleListenerActivateTwiceThrowsException() throws IllegalStateException {
    ReboundPerformer performer = createReboundPerformer();
    Spring spring = ReboundPerformer.springSystem.createSpring();

    // No exceptions expected.
    performer.lifecycleListener.onSpringActivate(spring);

    // Different spring. No exceptions expected.
    performer.lifecycleListener.onSpringActivate(ReboundPerformer.springSystem.createSpring());

    // Same spring activated twice. Expect exception.
    thrown.expect(IllegalStateException.class);
    performer.lifecycleListener.onSpringActivate(spring);
  }

  @Test
  public void pausesExistingSpring() {
    target.setAlpha(1f);

    SpringTo<Float> fadeOut = new SpringTo<>(ReboundProperty.ALPHA, 0f);
    runtime.addPlan(fadeOut, target);

    // Change in alpha after 1 frame.
    stepOnce();
    float alpha = target.getAlpha();
    assertThat(alpha).isLessThan(1f);

    // Add PausesSpring plan.
    SimulatedGestureRecognizer gesture = new SimulatedGestureRecognizer(target);
    PausesSpring pause = new PausesSpring(ReboundProperty.ALPHA, gesture);
    runtime.addPlan(pause, target);

    gesture.setState(GestureRecognizer.BEGAN);
    // No change in alpha after 1 frame.
    stepOnce();
    assertThat(target.getAlpha()).isWithin(0f).of(alpha);

    gesture.setState(GestureRecognizer.RECOGNIZED);
    // Change in alpha after 1 frame.
    stepOnce();
    assertThat(target.getAlpha()).isLessThan(alpha);
  }

  @Test
  public void pausesFutureSpring() {
    target.setAlpha(1f);

    // Add PausesSpring plan for property with no spring.
    SimulatedGestureRecognizer gesture = new SimulatedGestureRecognizer(target);
    PausesSpring pause = new PausesSpring(ReboundProperty.ALPHA, gesture);
    runtime.addPlan(pause, target);

    gesture.setState(GestureRecognizer.BEGAN);

    // Add spring to same property.
    SpringTo<Float> fadeOut = new SpringTo<>(ReboundProperty.ALPHA, 0f);
    runtime.addPlan(fadeOut, target);

    // No change in alpha after 1 frame.
    stepOnce();
    assertThat(target.getAlpha()).isWithin(0f).of(1f);
  }

  @Test
  public void oneGesturePauseMultipleProperties() {
    target.setAlpha(1f);
    target.setScaleX(1f);

    SimulatedGestureRecognizer gesture = new SimulatedGestureRecognizer(target);
    gesture.setState(GestureRecognizer.BEGAN);

    runtime.addPlan(new PausesSpring(ReboundProperty.ALPHA, gesture), target);
    runtime.addPlan(new PausesSpring(ReboundProperty.SCALE_X, gesture), target);

    runtime.addPlan(new SpringTo<>(ReboundProperty.ALPHA, 0f), target);
    runtime.addPlan(new SpringTo<>(ReboundProperty.SCALE_X, .5f), target);

    stepOnce();
    assertThat(target.getAlpha()).isWithin(0f).of(1f);
    assertThat(target.getScaleX()).isWithin(0f).of(1f);
  }

  @Test
  public void onePropertyPausedByMultipleGestures() {
    target.setAlpha(1f);

    SimulatedGestureRecognizer gesture1 = new SimulatedGestureRecognizer(target);
    SimulatedGestureRecognizer gesture2 = new SimulatedGestureRecognizer(target);

    runtime.addPlan(new SpringTo<>(ReboundProperty.ALPHA, 0f), target);
    runtime.addPlan(new PausesSpring(ReboundProperty.ALPHA, gesture1), target);
    runtime.addPlan(new PausesSpring(ReboundProperty.ALPHA, gesture2), target);

    gesture1.setState(GestureRecognizer.BEGAN);
    gesture2.setState(GestureRecognizer.POSSIBLE);
    // No change.
    stepOnce();
    assertThat(target.getAlpha()).isWithin(0f).of(1f);

    gesture1.setState(GestureRecognizer.RECOGNIZED);
    gesture2.setState(GestureRecognizer.BEGAN);
    // No change.
    stepOnce();
    assertThat(target.getAlpha()).isWithin(0f).of(1f);

    gesture1.setState(GestureRecognizer.BEGAN);
    gesture2.setState(GestureRecognizer.CHANGED);
    // No change.
    stepOnce();
    assertThat(target.getAlpha()).isWithin(0f).of(1f);

    gesture1.setState(GestureRecognizer.CANCELLED);
    gesture2.setState(GestureRecognizer.RECOGNIZED);
    // Change.
    stepOnce();
    assertThat(target.getAlpha()).isLessThan(1f);
  }

  /**
   * Advance the spring simulation by one frame.
   */
  private void stepOnce() {
    springLooper.step(FRAME);
  }

  /**
   * Advance the spring simulation continuously until settled.
   */
  private void stepUntilSettled() {
    boolean idle = false;
    while (!idle) {
      idle = springLooper.step(FRAME);
    }
  }

  /**
   * Creates and initializes a ReboundPerformer manually, rather than letting the {@link Runtime}
   * do it.
   */
  private ReboundPerformer createReboundPerformer() {
    ReboundPerformer performer = new ReboundPerformer();
    performer.initialize(target);
    performer.setIsActiveTokenGenerator(new IsActiveTokenGenerator() {
      @Override
      public IsActiveToken generate() {
        return new IsActiveToken() {
          @Override
          public void terminate() {
            // No-op.
          }
        };
      }
    });
    return performer;
  }

  private static class UnsupportedPlan extends Plan {

    @Override
    public Class<? extends Performer> getPerformerClass() {
      return ReboundPerformer.class;
    }
  }
}
