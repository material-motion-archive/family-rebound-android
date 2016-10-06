/*
 * Copyright (C) 2016 The Material Motion Authors. All Rights Reserved.
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
import com.google.android.material.motion.family.rebound.ConfigureSpring;
import com.google.android.material.motion.family.rebound.ReboundProperty;
import com.google.android.material.motion.family.rebound.SpringTo;
import com.google.android.material.motion.runtime.Scheduler;
import com.google.android.material.motion.runtime.Transaction;

/**
 * Material Motion Rebound Family sample Activity.
 */
public class MainActivity extends AppCompatActivity {

  private final Scheduler scheduler = new Scheduler();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.main_activity);

    View content = findViewById(android.R.id.content);
    final View target1 = findViewById(R.id.target1);
    final View target2 = findViewById(R.id.target2);

    content.setOnTouchListener(new OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        SpringTo scaleTo = new SpringTo<>(ReboundProperty.SCALE, 1f);
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
        float tension = SpringTo.DEFAULT_TENSION;
        float friction = (float) Math.sqrt(4 * SpringTo.DEFAULT_TENSION); // Critically damped.
        ConfigureSpring configureSpring =
          new ConfigureSpring(ReboundProperty.SCALE, tension, friction);

        Transaction transaction = new Transaction();

        transaction.addPlan(scaleTo, target1);
        transaction.addPlan(scaleTo, target2);

        transaction.addPlan(configureSpring, target1);
        transaction.addPlan(configureSpring, target2);

        scheduler.commitTransaction(transaction);

        return true;
      }
    });
  }
}
