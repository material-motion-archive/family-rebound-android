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

import android.util.Property;
import android.view.View;

/**
 * Defines the properties that can be animated with {@link SpringTo}.
 *
 * @param <T> The type of the target this property acts on.
 * @param <V> The type of the value this property acts on.
 */
public class ReboundProperty<T, V> {

  private static final TypeConverterCompat<Float> NO_OP = new NoOpConverter();

  public static final ReboundProperty<View, Float> ALPHA =
    new ReboundProperty<>(View.ALPHA, NO_OP);
  public static final ReboundProperty<View, Float> TRANSLATION_X =
    new ReboundProperty<>(View.TRANSLATION_X, NO_OP);
  public static final ReboundProperty<View, Float> TRANSLATION_Y =
    new ReboundProperty<>(View.TRANSLATION_Y, NO_OP);
  public static final ReboundProperty<View, Float> TRANSLATION_Z =
    new ReboundProperty<>(View.TRANSLATION_Z, NO_OP);
  public static final ReboundProperty<View, Float> X =
    new ReboundProperty<>(View.X, NO_OP);
  public static final ReboundProperty<View, Float> Y =
    new ReboundProperty<>(View.Y, NO_OP);
  public static final ReboundProperty<View, Float> Z =
    new ReboundProperty<>(View.Z, NO_OP);
  public static final ReboundProperty<View, Float> ROTATION =
    new ReboundProperty<>(View.ROTATION, NO_OP);
  public static final ReboundProperty<View, Float> ROTATION_X =
    new ReboundProperty<>(View.ROTATION_X, NO_OP);
  public static final ReboundProperty<View, Float> ROTATION_Y =
    new ReboundProperty<>(View.ROTATION_Y, NO_OP);
  public static final ReboundProperty<View, Float> SCALE_X =
    new ReboundProperty<>(View.SCALE_X, NO_OP);
  public static final ReboundProperty<View, Float> SCALE_Y =
    new ReboundProperty<>(View.SCALE_Y, NO_OP);
  public static final ReboundProperty<View, Float> SCALE =
    new ReboundProperty<>(new CombinedProperty<>(View.SCALE_X, View.SCALE_Y), NO_OP);

  final TypeConverterCompat<V> converter;
  final Property<T, V> property;

  public ReboundProperty(Property<T, V> property, TypeConverterCompat<V> converter) {
    this.property = property;
    this.converter = converter;
  }

  public float getFraction(T target) {
    return converter.convert(property.get(target));
  }

  public void setFraction(T target, float fraction) {
    property.set(target, converter.convertBack(fraction));
  }

  /**
   * A class used to convert type T to a float and back again. This is necessary when the value
   * types of in animation are different from the property type.
   *
   * @see android.animation.BidirectionalTypeConverter
   */
  public interface TypeConverterCompat<T> {

    /**
     * Converts a value from type T to float.
     */
    float convert(T value);

    /**
     * Converts a value back from float to type T.
     */
    T convertBack(float value);
  }

  /**
   * A utility class to treat multiple properties as one.
   */
  private static class CombinedProperty<T, V> extends Property<T, V> {

    private final Property<T, V>[] properties;

    @SafeVarargs
    private CombinedProperty(Property<T, V>... properties) {
      super(
        properties[0].getType(),
        "combined " + properties[0].getName() + "+" + properties.length);
      this.properties = properties;
    }

    @Override
    public V get(T object) {
      return properties[0].get(object);
    }

    @Override
    public void set(T object, V value) {
      for (Property<T, V> property : properties) {
        property.set(object, value);
      }
    }
  }

  /**
   * A type converter from float to float.
   */
  private static class NoOpConverter implements TypeConverterCompat<Float> {

    @Override
    public float convert(Float value) {
      return value;
    }

    @Override
    public Float convertBack(float value) {
      return value;
    }
  }
}
