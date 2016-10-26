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

import com.facebook.rebound.SpringConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class ReboundPlanTests {

  @Test
  public void cloneHasEqualProperties() {
    SpringTo<Float> original = new SpringTo<>(ReboundProperty.SCALE, 0.5f);
    original.configuration = new SpringConfig(5, 7);

    SpringTo clone = (SpringTo) original.clone();

    assertThat(clone.configuration.tension).isWithin(0).of(original.configuration.tension);
    assertThat(clone.configuration.friction).isWithin(0).of(original.configuration.friction);
    assertThat(clone.destination).isEqualTo(original.destination);
    assertThat(clone.property).isEqualTo(original.property);
  }
}
