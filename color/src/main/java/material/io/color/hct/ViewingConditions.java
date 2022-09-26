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

package material.io.color.hct;

import material.io.color.utils.ColorUtils;
import material.io.color.utils.MathUtils;

/**
 * In traditional color spaces, a color can be identified solely by the observer's measurement of
 * the color. Color appearance models such as CAM16 also use information about the environment where
 * the color was observed, known as the viewing conditions.
 *
 * <p>For example, white under the traditional assumption of a midday sun white point is accurately
 * measured as a slightly chromatic blue by CAM16. (roughly, hue 203, chroma 3, lightness 100)
 *
 * <p>This class caches intermediate values of the CAM16 conversion process that depend only on
 * viewing conditions, enabling speed ups.
 */
public final class ViewingConditions {
  /** sRGB-like viewing conditions. */
  public static final ViewingConditions DEFAULT =
      ViewingConditions.make(
          new double[] {
            ColorUtils.whitePointD65()[0],
            ColorUtils.whitePointD65()[1],
            ColorUtils.whitePointD65()[2]
          },
          (200.0 / Math.PI * ColorUtils.yFromLstar(50.0) / 100.f),
          50.0,
          2.0,
          false);

  private final double aw;
  private final double nbb;
  private final double ncb;
  private final double c;
  private final double nc;
  private final double n;
  private final double[] rgbD;
  private final double fl;
  private final double flRoot;
  private final double z;

  public double getAw() {
    return aw;
  }

  public double getN() {
    return n;
  }

  public double getNbb() {
    return nbb;
  }

  double getNcb() {
    return ncb;
  }

  double getC() {
    return c;
  }

  double getNc() {
    return nc;
  }

  public double[] getRgbD() {
    return rgbD;
  }

  double getFl() {
    return fl;
  }

  public double getFlRoot() {
    return flRoot;
  }

  double getZ() {
    return z;
  }

  /**
   * Create ViewingConditions from a simple, physically relevant, set of parameters.
   *
   * @param whitePoint White point, measured in the XYZ color space. default = D65, or sunny day
   *     afternoon
   * @param adaptingLuminance The luminance of the adapting field. Informally, how bright it is in
   *     the room where the color is viewed. Can be calculated from lux by multiplying lux by
   *     0.0586. default = 11.72, or 200 lux.
   * @param backgroundLstar The lightness of the area surrounding the color. measured by L* in
   *     L*a*b*. default = 50.0
   * @param surround A general description of the lighting surrounding the color. 0 is pitch dark,
   *     like watching a movie in a theater. 1.0 is a dimly light room, like watching TV at home at
   *     night. 2.0 means there is no difference between the lighting on the color and around it.
   *     default = 2.0
   * @param discountingIlluminant Whether the eye accounts for the tint of the ambient lighting,
   *     such as knowing an apple is still red in green light. default = false, the eye does not
   *     perform this process on self-luminous objects like displays.
   */
  static ViewingConditions make(
      double[] whitePoint,
      double adaptingLuminance,
      double backgroundLstar,
      double surround,
      boolean discountingIlluminant) {
    // Transform white point XYZ to 'cone'/'rgb' responses
    double[][] matrix = Cam16.XYZ_TO_CAM16RGB;
    double[] xyz = whitePoint;
    double rW = (xyz[0] * matrix[0][0]) + (xyz[1] * matrix[0][1]) + (xyz[2] * matrix[0][2]);
    double gW = (xyz[0] * matrix[1][0]) + (xyz[1] * matrix[1][1]) + (xyz[2] * matrix[1][2]);
    double bW = (xyz[0] * matrix[2][0]) + (xyz[1] * matrix[2][1]) + (xyz[2] * matrix[2][2]);
    double f = 0.8 + (surround / 10.0);
    double c =
        (f >= 0.9)
            ? MathUtils.lerp(0.59, 0.69, ((f - 0.9) * 10.0))
            : MathUtils.lerp(0.525, 0.59, ((f - 0.8) * 10.0));
    double d =
        discountingIlluminant
            ? 1.0
            : f * (1.0 - ((1.0 / 3.6) * Math.exp((-adaptingLuminance - 42.0) / 92.0)));
    d = MathUtils.clampDouble(0.0, 1.0, d);
    double nc = f;
    double[] rgbD =
        new double[] {
          d * (100.0 / rW) + 1.0 - d, d * (100.0 / gW) + 1.0 - d, d * (100.0 / bW) + 1.0 - d
        };
    double k = 1.0 / (5.0 * adaptingLuminance + 1.0);
    double k4 = k * k * k * k;
    double k4F = 1.0 - k4;
    double fl = (k4 * adaptingLuminance) + (0.1 * k4F * k4F * Math.cbrt(5.0 * adaptingLuminance));
    double n = (ColorUtils.yFromLstar(backgroundLstar) / whitePoint[1]);
    double z = 1.48 + Math.sqrt(n);
    double nbb = 0.725 / Math.pow(n, 0.2);
    double ncb = nbb;
    double[] rgbAFactors =
        new double[] {
          Math.pow(fl * rgbD[0] * rW / 100.0, 0.42),
          Math.pow(fl * rgbD[1] * gW / 100.0, 0.42),
          Math.pow(fl * rgbD[2] * bW / 100.0, 0.42)
        };

    double[] rgbA =
        new double[] {
          (400.0 * rgbAFactors[0]) / (rgbAFactors[0] + 27.13),
          (400.0 * rgbAFactors[1]) / (rgbAFactors[1] + 27.13),
          (400.0 * rgbAFactors[2]) / (rgbAFactors[2] + 27.13)
        };

    double aw = ((2.0 * rgbA[0]) + rgbA[1] + (0.05 * rgbA[2])) * nbb;
    return new ViewingConditions(n, aw, nbb, ncb, c, nc, rgbD, fl, Math.pow(fl, 0.25), z);
  }

  /**
   * Parameters are intermediate values of the CAM16 conversion process. Their names are shorthand
   * for technical color science terminology, this class would not benefit from documenting them
   * individually. A brief overview is available in the CAM16 specification, and a complete overview
   * requires a color science textbook, such as Fairchild's Color Appearance Models.
   */
  private ViewingConditions(
      double n,
      double aw,
      double nbb,
      double ncb,
      double c,
      double nc,
      double[] rgbD,
      double fl,
      double flRoot,
      double z) {
    this.n = n;
    this.aw = aw;
    this.nbb = nbb;
    this.ncb = ncb;
    this.c = c;
    this.nc = nc;
    this.rgbD = rgbD;
    this.fl = fl;
    this.flRoot = flRoot;
    this.z = z;
  }
}
