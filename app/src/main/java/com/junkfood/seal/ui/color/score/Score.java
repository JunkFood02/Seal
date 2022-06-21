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

package com.junkfood.seal.ui.color.score;

import com.junkfood.seal.ui.color.hct.Cam16;
import com.junkfood.seal.ui.color.utils.ColorUtils;
import com.junkfood.seal.ui.color.utils.MathUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Given a large set of colors, remove colors that are unsuitable for a UI theme, and rank the rest
 * based on suitability.
 *
 * <p>Enables use of a high cluster count for image quantization, thus ensuring colors aren't
 * muddied, while curating the high cluster count to a much smaller number of appropriate choices.
 */
public final class Score {
  private static final double CUTOFF_CHROMA = 15.;
  private static final double CUTOFF_EXCITED_PROPORTION = 0.01;
  private static final double CUTOFF_TONE = 10.;
  private static final double TARGET_CHROMA = 48.;
  private static final double WEIGHT_PROPORTION = 0.7;
  private static final double WEIGHT_CHROMA_ABOVE = 0.3;
  private static final double WEIGHT_CHROMA_BELOW = 0.1;

  private Score() {}

  /**
   * Given a map with keys of colors and values of how often the color appears, rank the colors
   * based on suitability for being used for a UI theme.
   *
   * @param colorsToPopulation map with keys of colors and values of how often the color appears,
   *     usually from a source image.
   * @return Colors sorted by suitability for a UI theme. The most suitable color is the first item,
   *     the least suitable is the last. There will always be at least one color returned. If all
   *     the input colors were not suitable for a theme, a default fallback color will be provided,
   *     Google Blue.
   */
  public static List<Integer> score(Map<Integer, Integer> colorsToPopulation) {
    // Determine the total count of all colors.
    double populationSum = 0.;
    for (Map.Entry<Integer, Integer> entry : colorsToPopulation.entrySet()) {
      populationSum += entry.getValue();
    }

    // Turn the count of each color into a proportion by dividing by the total
    // count. Also, fill a cache of CAM16 colors representing each color, and
    // record the proportion of colors for each CAM16 hue.
    Map<Integer, Cam16> colorsToCam = new HashMap<>();
    double[] hueProportions = new double[361];
    for (Map.Entry<Integer, Integer> entry : colorsToPopulation.entrySet()) {
      int color = entry.getKey();
      double population = entry.getValue();
      double proportion = population / populationSum;

      Cam16 cam = Cam16.fromInt(color);
      colorsToCam.put(color, cam);

      int hue = (int) Math.round(cam.getHue());
      hueProportions[hue] += proportion;
    }

    // Determine the proportion of the colors around each color, by summing the
    // proportions around each color's hue.
    Map<Integer, Double> colorsToExcitedProportion = new HashMap<>();
    for (Map.Entry<Integer, Cam16> entry : colorsToCam.entrySet()) {
      int color = entry.getKey();
      Cam16 cam = entry.getValue();
      int hue = (int) Math.round(cam.getHue());

      double excitedProportion = 0.;
      for (int j = (hue - 15); j < (hue + 15); j++) {
        int neighborHue = MathUtils.sanitizeDegreesInt(j);
        excitedProportion += hueProportions[neighborHue];
      }

      colorsToExcitedProportion.put(color, excitedProportion);
    }

    // Score the colors by their proportion, as well as how chromatic they are.
    Map<Integer, Double> colorsToScore = new HashMap<>();
    for (Map.Entry<Integer, Cam16> entry : colorsToCam.entrySet()) {
      int color = entry.getKey();
      Cam16 cam = entry.getValue();

      double proportion = colorsToExcitedProportion.get(color);
      double proportionScore = proportion * 100.0 * WEIGHT_PROPORTION;

      double chromaWeight =
          cam.getChroma() < TARGET_CHROMA ? WEIGHT_CHROMA_BELOW : WEIGHT_CHROMA_ABOVE;
      double chromaScore = (cam.getChroma() - TARGET_CHROMA) * chromaWeight;

      double score = proportionScore + chromaScore;
      colorsToScore.put(color, score);
    }

    // Remove colors that are unsuitable, ex. very dark or unchromatic colors.
    // Also, remove colors that are very similar in hue.
    List<Integer> filteredColors = filter(colorsToExcitedProportion, colorsToCam);
    Map<Integer, Double> filteredColorsToScore = new HashMap<>();
    for (int color : filteredColors) {
      filteredColorsToScore.put(color, colorsToScore.get(color));
    }

    // Ensure the list of colors returned is sorted such that the first in the
    // list is the most suitable, and the last is the least suitable.
    List<Map.Entry<Integer, Double>> entryList = new ArrayList<>(filteredColorsToScore.entrySet());
    Collections.sort(entryList, new ScoredComparator());
    List<Integer> colorsByScoreDescending = new ArrayList<>();
    for (Map.Entry<Integer, Double> entry : entryList) {
      int color = entry.getKey();
      Cam16 cam = colorsToCam.get(color);
      boolean duplicateHue = false;

      for (Integer alreadyChosenColor : colorsByScoreDescending) {
        Cam16 alreadyChosenCam = colorsToCam.get(alreadyChosenColor);
        if (MathUtils.differenceDegrees(cam.getHue(), alreadyChosenCam.getHue()) < 15) {
          duplicateHue = true;
          break;
        }
      }

      if (duplicateHue) {
        continue;
      }
      colorsByScoreDescending.add(entry.getKey());
    }

    // Ensure that at least one color is returned.
    if (colorsByScoreDescending.isEmpty()) {
      colorsByScoreDescending.add(0xff4285F4); // Google Blue
    }
    return colorsByScoreDescending;
  }

  private static List<Integer> filter(
      Map<Integer, Double> colorsToExcitedProportion, Map<Integer, Cam16> colorsToCam) {
    List<Integer> filtered = new ArrayList<>();
    for (Map.Entry<Integer, Cam16> entry : colorsToCam.entrySet()) {
      int color = entry.getKey();
      Cam16 cam = entry.getValue();
      double proportion = colorsToExcitedProportion.get(color);

      if (cam.getChroma() >= CUTOFF_CHROMA
          && ColorUtils.lstarFromArgb(color) >= CUTOFF_TONE
          && proportion >= CUTOFF_EXCITED_PROPORTION) {
        filtered.add(color);
      }
    }
    return filtered;
  }

  static class ScoredComparator implements Comparator<Map.Entry<Integer, Double>> {
    public ScoredComparator() {}

    @Override
    public int compare(Map.Entry<Integer, Double> entry1, Map.Entry<Integer, Double> entry2) {
      return -entry1.getValue().compareTo(entry2.getValue());
    }
  }
}
