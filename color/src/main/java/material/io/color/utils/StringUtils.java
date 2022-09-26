/*
 * Copyright 2021 Google LLC
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

package material.io.color.utils;

/** Utility methods for string representations of colors. */
final class StringUtils {
  private StringUtils() {}

  /**
   * Hex string representing color, ex. #ff0000 for red.
   *
   * @param argb ARGB representation of a color.
   */
  public static String hexFromArgb(int argb) {
    int red = ColorUtils.redFromArgb(argb);
    int blue = ColorUtils.blueFromArgb(argb);
    int green = ColorUtils.greenFromArgb(argb);
    return String.format("#%02x%02x%02x", red, green, blue);
  }
}
