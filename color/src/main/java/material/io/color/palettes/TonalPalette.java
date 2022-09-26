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

package material.io.color.palettes;

import material.io.color.hct.Hct;
import java.util.HashMap;
import java.util.Map;

/**
 * A convenience class for retrieving colors that are constant in hue and chroma, but vary in tone.
 */
public final class TonalPalette {
  Map<Integer, Integer> cache;
  double hue;
  double chroma;

  /**
   * Create tones using the HCT hue and chroma from a color.
   *
   * @param argb ARGB representation of a color
   * @return Tones matching that color's hue and chroma.
   */
  public static final TonalPalette fromInt(int argb) {
    Hct hct = Hct.fromInt(argb);
    return TonalPalette.fromHueAndChroma(hct.getHue(), hct.getChroma());
  }

  /**
   * Create tones from a defined HCT hue and chroma.
   *
   * @param hue HCT hue
   * @param chroma HCT chroma
   * @return Tones matching hue and chroma.
   */
  public static final TonalPalette fromHueAndChroma(double hue, double chroma) {
    return new TonalPalette(hue, chroma);
  }

  private TonalPalette(double hue, double chroma) {
    cache = new HashMap<>();
    this.hue = hue;
    this.chroma = chroma;
  }

  /**
   * Create an ARGB color with HCT hue and chroma of this Tones instance, and the provided HCT tone.
   *
   * @param tone HCT tone, measured from 0 to 100.
   * @return ARGB representation of a color with that tone.
   */
  // AndroidJdkLibsChecker is higher priority than ComputeIfAbsentUseValue (b/119581923)
  @SuppressWarnings("ComputeIfAbsentUseValue")
  public int tone(int tone) {
    Integer color = cache.get(tone);
    if (color == null) {
      color = Hct.from(this.hue, this.chroma, tone).toInt();
      cache.put(tone, color);
    }
    return color;
  }
}
