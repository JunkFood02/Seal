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

package io.material.hct;

import io.material.utils.ColorUtils;

/**
 * A color system built using CAM16 hue and chroma, and L* from L*a*b*.
 *
 * <p>Using L* creates a link between the color system, contrast, and thus accessibility. Contrast
 * ratio depends on relative luminance, or Y in the XYZ color space. L*, or perceptual luminance can
 * be calculated from Y.
 *
 * <p>Unlike Y, L* is linear to human perception, allowing trivial creation of accurate color tones.
 *
 * <p>Unlike contrast ratio, measuring contrast in L* is linear, and simple to calculate. A
 * difference of 40 in HCT tone guarantees a contrast ratio >= 3.0, and a difference of 50
 * guarantees a contrast ratio >= 4.5.
 */

/**
 * HCT, hue, chroma, and tone. A color system that provides a perceptually accurate color
 * measurement system that can also accurately render what colors will appear as in different
 * lighting environments.
 */
public final class Hct {
  private double hue;
  private double chroma;
  private double tone;
  private int argb;

  /**
   * Create an HCT color from hue, chroma, and tone.
   *
   * @param hue 0 <= hue < 360; invalid values are corrected.
   * @param chroma 0 <= chroma < ?; Informally, colorfulness. The color returned may be lower than
   *     the requested chroma. Chroma has a different maximum for any given hue and tone.
   * @param tone 0 <= tone <= 100; invalid values are corrected.
   * @return HCT representation of a color in default viewing conditions.
   */
  public static Hct from(double hue, double chroma, double tone) {
    int argb = HctSolver.solveToInt(hue, chroma, tone);
    return new Hct(argb);
  }

  /**
   * Create an HCT color from a color.
   *
   * @param argb ARGB representation of a color.
   * @return HCT representation of a color in default viewing conditions
   */
  public static Hct fromInt(int argb) {
    return new Hct(argb);
  }

  private Hct(int argb) {
    setInternalState(argb);
  }

  public double getHue() {
    return hue;
  }

  public double getChroma() {
    return chroma;
  }

  public double getTone() {
    return tone;
  }

  public int toInt() {
    return argb;
  }

  /**
   * Set the hue of this color. Chroma may decrease because chroma has a different maximum for any
   * given hue and tone.
   *
   * @param newHue 0 <= newHue < 360; invalid values are corrected.
   */
  public void setHue(double newHue) {
    setInternalState(HctSolver.solveToInt(newHue, chroma, tone));
  }

  /**
   * Set the chroma of this color. Chroma may decrease because chroma has a different maximum for
   * any given hue and tone.
   *
   * @param newChroma 0 <= newChroma < ?
   */
  public void setChroma(double newChroma) {
    setInternalState(HctSolver.solveToInt(hue, newChroma, tone));
  }

  /**
   * Set the tone of this color. Chroma may decrease because chroma has a different maximum for any
   * given hue and tone.
   *
   * @param newTone 0 <= newTone <= 100; invalid valids are corrected.
   */
  public void setTone(double newTone) {
    setInternalState(HctSolver.solveToInt(hue, chroma, newTone));
  }

  /**
   * Translate a color into different ViewingConditions.
   *
   * <p>Colors change appearance. They look different with lights on versus off, the same color, as
   * in hex code, on white looks different when on black. This is called color relativity, most
   * famously explicated by Josef Albers in Interaction of Color.
   *
   * <p>In color science, color appearance models can account for this and calculate the appearance
   * of a color in different settings. HCT is based on CAM16, a color appearance model, and uses it
   * to make these calculations.
   *
   * <p>See ViewingConditions.make for parameters affecting color appearance.
   */
  public Hct inViewingConditions(ViewingConditions vc) {
    // 1. Use CAM16 to find XYZ coordinates of color in specified VC.
    Cam16 cam16 = Cam16.fromInt(toInt());
    double[] viewedInVc = cam16.xyzInViewingConditions(vc, null);

    // 2. Create CAM16 of those XYZ coordinates in default VC.
    Cam16 recastInVc =
        Cam16.fromXyzInViewingConditions(
            viewedInVc[0], viewedInVc[1], viewedInVc[2], ViewingConditions.DEFAULT);

    // 3. Create HCT from:
    // - CAM16 using default VC with XYZ coordinates in specified VC.
    // - L* converted from Y in XYZ coordinates in specified VC.
    return Hct.from(
        recastInVc.getHue(), recastInVc.getChroma(), ColorUtils.lstarFromY(viewedInVc[1]));
  }

  private void setInternalState(int argb) {
    this.argb = argb;
    Cam16 cam = Cam16.fromInt(argb);
    hue = cam.getHue();
    chroma = cam.getChroma();
    this.tone = ColorUtils.lstarFromArgb(argb);
  }
}
