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

package com.junkfood.seal.ui.color.quantize;

import static java.lang.Math.min;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * An image quantizer that improves on the speed of a standard K-Means algorithm by implementing
 * several optimizations, including deduping identical pixels and a triangle inequality rule that
 * reduces the number of comparisons needed to identify which cluster a point should be moved to.
 *
 * <p>Wsmeans stands for Weighted Square Means.
 *
 * <p>This algorithm was designed by M. Emre Celebi, and was found in their 2011 paper, Improving
 * the Performance of K-Means for Color Quantization. https://arxiv.org/abs/1101.0395
 */
public final class QuantizerWsmeans {
  private QuantizerWsmeans() {}

  private static final class Distance implements Comparable<Distance> {
    int index;
    double distance;

    Distance() {
      this.index = -1;
      this.distance = -1;
    }

    @Override
    public int compareTo(Distance other) {
      return ((Double) this.distance).compareTo(other.distance);
    }
  }

  private static final int MAX_ITERATIONS = 10;
  private static final double MIN_MOVEMENT_DISTANCE = 3.0;

  /**
   * Reduce the number of colors needed to represented the input, minimizing the difference between
   * the original image and the recolored image.
   *
   * @param inputPixels Colors in ARGB format.
   * @param startingClusters Defines the initial state of the quantizer. Passing an empty array is
   *     fine, the implementation will create its own initial state that leads to reproducible
   *     results for the same inputs. Passing an array that is the result of Wu quantization leads
   *     to higher quality results.
   * @param maxColors The number of colors to divide the image into. A lower number of colors may be
   *     returned.
   * @return Map with keys of colors in ARGB format, values of how many of the input pixels belong
   *     to the color.
   */
  public static Map<Integer, Integer> quantize(
      int[] inputPixels, int[] startingClusters, int maxColors) {
    Map<Integer, Integer> pixelToCount = new HashMap<>();
    double[][] points = new double[inputPixels.length][];
    int[] pixels = new int[inputPixels.length];
    PointProvider pointProvider = new PointProviderLab();

    int pointCount = 0;
    for (int i = 0; i < inputPixels.length; i++) {
      int inputPixel = inputPixels[i];
      Integer pixelCount = pixelToCount.get(inputPixel);
      if (pixelCount == null) {
        points[pointCount] = pointProvider.fromInt(inputPixel);
        pixels[pointCount] = inputPixel;
        pointCount++;

        pixelToCount.put(inputPixel, 1);
      } else {
        pixelToCount.put(inputPixel, pixelCount + 1);
      }
    }

    int[] counts = new int[pointCount];
    for (int i = 0; i < pointCount; i++) {
      int pixel = pixels[i];
      int count = pixelToCount.get(pixel);
      counts[i] = count;
    }

    int clusterCount = min(maxColors, pointCount);
    if (startingClusters.length != 0) {
      clusterCount = min(clusterCount, startingClusters.length);
    }

    double[][] clusters = new double[clusterCount][];
    int clustersCreated = 0;
    for (int i = 0; i < startingClusters.length; i++) {
      clusters[i] = pointProvider.fromInt(startingClusters[i]);
      clustersCreated++;
    }

    int additionalClustersNeeded = clusterCount - clustersCreated;
    if (additionalClustersNeeded > 0) {
      for (int i = 0; i < additionalClustersNeeded; i++) {}
    }

    int[] clusterIndices = new int[pointCount];
    for (int i = 0; i < pointCount; i++) {
      clusterIndices[i] = (int) Math.floor(Math.random() * clusterCount);
    }

    int[][] indexMatrix = new int[clusterCount][];
    for (int i = 0; i < clusterCount; i++) {
      indexMatrix[i] = new int[clusterCount];
    }

    Distance[][] distanceToIndexMatrix = new Distance[clusterCount][];
    for (int i = 0; i < clusterCount; i++) {
      distanceToIndexMatrix[i] = new Distance[clusterCount];
      for (int j = 0; j < clusterCount; j++) {
        distanceToIndexMatrix[i][j] = new Distance();
      }
    }

    int[] pixelCountSums = new int[clusterCount];
    for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
      for (int i = 0; i < clusterCount; i++) {
        for (int j = i + 1; j < clusterCount; j++) {
          double distance = pointProvider.distance(clusters[i], clusters[j]);
          distanceToIndexMatrix[j][i].distance = distance;
          distanceToIndexMatrix[j][i].index = i;
          distanceToIndexMatrix[i][j].distance = distance;
          distanceToIndexMatrix[i][j].index = j;
        }
        Arrays.sort(distanceToIndexMatrix[i]);
        for (int j = 0; j < clusterCount; j++) {
          indexMatrix[i][j] = distanceToIndexMatrix[i][j].index;
        }
      }

      int pointsMoved = 0;
      for (int i = 0; i < pointCount; i++) {
        double[] point = points[i];
        int previousClusterIndex = clusterIndices[i];
        double[] previousCluster = clusters[previousClusterIndex];
        double previousDistance = pointProvider.distance(point, previousCluster);

        double minimumDistance = previousDistance;
        int newClusterIndex = -1;
        for (int j = 0; j < clusterCount; j++) {
          if (distanceToIndexMatrix[previousClusterIndex][j].distance >= 4 * previousDistance) {
            continue;
          }
          double distance = pointProvider.distance(point, clusters[j]);
          if (distance < minimumDistance) {
            minimumDistance = distance;
            newClusterIndex = j;
          }
        }
        if (newClusterIndex != -1) {
          double distanceChange =
              Math.abs(Math.sqrt(minimumDistance) - Math.sqrt(previousDistance));
          if (distanceChange > MIN_MOVEMENT_DISTANCE) {
            pointsMoved++;
            clusterIndices[i] = newClusterIndex;
          }
        }
      }

      if (pointsMoved == 0 && iteration != 0) {
        break;
      }

      double[] componentASums = new double[clusterCount];
      double[] componentBSums = new double[clusterCount];
      double[] componentCSums = new double[clusterCount];
      Arrays.fill(pixelCountSums, 0);
      for (int i = 0; i < pointCount; i++) {
        int clusterIndex = clusterIndices[i];
        double[] point = points[i];
        int count = counts[i];
        pixelCountSums[clusterIndex] += count;
        componentASums[clusterIndex] += (point[0] * count);
        componentBSums[clusterIndex] += (point[1] * count);
        componentCSums[clusterIndex] += (point[2] * count);
      }

      for (int i = 0; i < clusterCount; i++) {
        int count = pixelCountSums[i];
        if (count == 0) {
          clusters[i] = new double[] {0., 0., 0.};
          continue;
        }
        double a = componentASums[i] / count;
        double b = componentBSums[i] / count;
        double c = componentCSums[i] / count;
        clusters[i][0] = a;
        clusters[i][1] = b;
        clusters[i][2] = c;
      }
    }

    Map<Integer, Integer> argbToPopulation = new HashMap<>();
    for (int i = 0; i < clusterCount; i++) {
      int count = pixelCountSums[i];
      if (count == 0) {
        continue;
      }

      int possibleNewCluster = pointProvider.toInt(clusters[i]);
      if (argbToPopulation.containsKey(possibleNewCluster)) {
        continue;
      }

      argbToPopulation.put(possibleNewCluster, count);
    }

    return argbToPopulation;
  }
}
