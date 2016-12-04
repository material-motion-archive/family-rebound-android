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
package com.google.android.material.motion.family.rebound.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;

import com.facebook.rebound.SpringConfig;
import com.google.android.libraries.remixer.annotation.RangeVariableMethod;
import com.google.android.libraries.remixer.annotation.RemixerBinder;
import com.google.android.libraries.remixer.ui.gesture.Direction;
import com.google.android.libraries.remixer.ui.view.RemixerFragment;
import com.google.android.material.motion.family.directmanipulation.Draggable;
import com.google.android.material.motion.family.rebound.PausesSpring;
import com.google.android.material.motion.family.rebound.ReboundProperty;
import com.google.android.material.motion.family.rebound.SpringTo;
import com.google.android.material.motion.gestures.DragGestureRecognizer;
import com.google.android.material.motion.runtime.MotionRuntime;

/**
 * Material Motion Rebound Family sample Activity.
 */
public class MainActivity extends AppCompatActivity {

  private final MotionRuntime runtime = new MotionRuntime();
  private int tension;
  private View target1;
  private View target2;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.main_activity);

    target1 = findViewById(R.id.target1);
    target2 = findViewById(R.id.target2);

    setupDemo1(target1);
    setupDemo2(target2);

    RemixerBinder.bind(this);
    RemixerFragment remixerFragment = RemixerFragment.newInstance();
    remixerFragment.attachToGesture(this, Direction.UP, 3);
    remixerFragment.attachToButton(this, (Button) findViewById(R.id.remixer_button));
  }

  private void setupDemo1(final View target) {
    target.setOnTouchListener(new OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        SpringTo<Float> scaleTo = new SpringTo<>(ReboundProperty.SCALE, 1f);
        switch (event.getActionMasked()) {
          case MotionEvent.ACTION_DOWN:
            scaleTo.destination = .5f;
            break;
          case MotionEvent.ACTION_UP:
            scaleTo.destination = 1f;
            break;
          default:
            return false;
        }

        float friction = (float) Math.sqrt(4 * tension); // Critically damped.
        scaleTo.configuration = new SpringConfig(tension, friction);

        runtime.addPlan(scaleTo, target);

        return true;
      }
    });
  }

  private void setupDemo2(View target) {
    DragGestureRecognizer gestureRecognizer = new DragGestureRecognizer();
    gestureRecognizer.dragSlop = 0;

    float friction = (float) Math.sqrt(4 * tension); // Critically damped.
    SpringConfig springConfig = new SpringConfig(tension, friction);
    SpringTo translationX = new SpringTo<>(ReboundProperty.TRANSLATION_X, 0f);
    SpringTo translationY = new SpringTo<>(ReboundProperty.TRANSLATION_Y, 0f);
    translationX.configuration = springConfig;
    translationY.configuration = springConfig;
    runtime.addPlan(translationX, target);
    runtime.addPlan(translationY, target);

    runtime.addPlan(new Draggable(gestureRecognizer), target);
    runtime.addPlan(new PausesSpring(ReboundProperty.TRANSLATION_X, gestureRecognizer), target);
    runtime.addPlan(new PausesSpring(ReboundProperty.TRANSLATION_Y, gestureRecognizer), target);
  }

  @RangeVariableMethod(maxValue = 1000, defaultValue = (int) SpringTo.DEFAULT_TENSION)
  public void setSpringTension(Integer tension) {
    this.tension = tension;
    setupDemo2(target2);
  }
}
