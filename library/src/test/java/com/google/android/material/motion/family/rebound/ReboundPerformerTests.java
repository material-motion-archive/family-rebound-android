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

import static com.google.common.truth.Truth.assertThat;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import com.facebook.rebound.BaseSpringSystem;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SteppingLooper;
import com.google.android.material.motion.runtime.Performer;
import com.google.android.material.motion.runtime.Performer.ContinuousPerformance.IsActiveToken;
import com.google.android.material.motion.runtime.Performer.ContinuousPerformance.IsActiveTokenGenerator;
import com.google.android.material.motion.runtime.Plan;
import com.google.android.material.motion.runtime.Scheduler;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class ReboundPerformerTests {

  private static final float EPSILON = 0.0001f;
  /**
   * An interval of time that represents one frame (~16ms).
   */
  private static final int FRAME = 16;

  private Scheduler scheduler;
  private View target;
  private SteppingLooper springLooper;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void setUp() {
    scheduler = new Scheduler();
    Context context = Robolectric.setupActivity(Activity.class);
    target = new View(context);

    // Prevent springs from using the actual looper, which cripples robolectric.
    springLooper = new SteppingLooper();
    ReboundPerformer.springSystem = new BaseSpringSystem(springLooper);
  }

  @Test
  public void didChangeTargetValue() {
    target.setAlpha(1f);
    SpringTo<Float> fadeOut = new SpringTo<>(ReboundProperty.ALPHA, 0f);

    scheduler.addPlan(fadeOut, target);

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
  public void didChangeSchedulerState() {
    // Scheduler is initially idle.
    assertThat(scheduler.getState()).isEqualTo(Scheduler.IDLE);

    scheduler.addPlan(new SpringTo<>(ReboundProperty.ALPHA, 0f), target);

    // Scheduler is still idle - spring not yet activated.
    assertThat(scheduler.getState()).isEqualTo(Scheduler.IDLE);

    // Scheduler is now active.
    stepOnce();
    assertThat(scheduler.getState()).isEqualTo(Scheduler.ACTIVE);

    // Scheduler is idle again when settled.
    stepUntilSettled();
    assertThat(scheduler.getState()).isEqualTo(Scheduler.IDLE);
  }

  @Test
  public void bouncySpringOvershoots() {
    target.setScaleX(0f);
    target.setScaleY(0f);
    // Set up a spring with reduced friction (more bouncy).
    SpringTo<Float> scaleUp = new SpringTo<>(ReboundProperty.SCALE, 1f);
    scaleUp.configuration =
      new SpringConfig(SpringTo.DEFAULT_TENSION, SpringTo.DEFAULT_FRICTION / 2);

    scheduler.addPlan(scaleUp, target);

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

    scheduler.addPlan(scaleUp, target);

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
   * Creates and initializes a ReboundPerformer manually, rather than letting the {@link Scheduler}
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
