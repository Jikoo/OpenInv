package com.lishid.openinv.util;

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * A modified Jaro-Winkler similarity that includes a cost for ignoring case.
 */
@NullMarked
public class FuzzyJaroWinkler {

  // Bitmask for converting ASCII characters to upper case.
  private static final int TO_UPPER_BITMASK = ~0x20;

  /**
   * This is specifically not optimized for equal strings because it is intended for internal use in an area where
   * the inputs are never equal.
   *
   * @param val1 the first string for comparison
   * @param val2 the second string for comparison
   * @return the result of the comparison, where higher is better
   */
  @ApiStatus.Internal
  public static double getSimilarity(@Nullable String val1, @Nullable String val2) {
    // Inputs should never be null or empty, but who knows what people might do to their databases.
    if (val1 == null || val1.isEmpty() || val2 == null || val2.isEmpty()) {
      return 0.0;
    }

    int[] chars1 = val1.codePoints().toArray();
    int[] chars2 = val2.codePoints().toArray();
    double jaro = getJaroSimilarity(chars1, chars2);

    // Winkler prefix
    int prefix = 0;
    for (int i = 0; i < 4; ++i) {
      if (chars1[i] != chars2[i] && !isEqualNormalized(chars1[i], chars2[i])) {
        break;
      }
      ++prefix;
    }

    return jaro + (prefix * 0.1 * (1.0 - jaro));
  }

  private static double getJaroSimilarity(int[] chars1, int[] chars2) {
    boolean[] matched1 = new boolean[chars1.length];
    boolean[] matched2 = new boolean[chars2.length];

    double matches = getMatches(chars1, chars2, matched1, matched2);

    if (matches == 0) {
      return 0.0;
    }

    double fuzz = getFuzzCost(chars1, chars2, matched1, matched2);

    return (matches / chars1.length + matches / chars2.length + (matches - fuzz) / matches) / 3.0;
  }

  private static int getMatches(int[] chars1, int[] chars2, boolean[] matched1, boolean[] matched2) {
    int similarityWindow = Math.max(0, Math.max(chars1.length, chars2.length) / 2 - 1);
    int matches = 0;

    for (int index1 = 0; index1 < chars1.length; index1++) {
      int start = Math.max(0, index1 - similarityWindow);
      int end = Math.min(chars2.length, index1 + similarityWindow + 1);
      for (int index2 = start; index2 < end; index2++) {
        if (matched2[index2]) {
          continue;
        }
        if (chars1[index1] == chars2[index2] || isEqualNormalized(chars1[index1], chars2[index2])) {
          matched1[index1] = true;
          matched2[index2] = true;
          ++matches;
          break;
        }
      }
    }

    return matches;
  }

  private static boolean isEqualNormalized(int char1, int char2) {
    // This is just fast ascii uppercase. Better normalization is expensive and would require that we compare twice,
    // once against normalized and once against raw, because normalized does not necessarily match raw.
    return (char1 & TO_UPPER_BITMASK) == (char2 & TO_UPPER_BITMASK) && char1 >= 'A' && char1 <= 'z';
  }

  /**
   * This is a slightly-modified version of transposition cost in a traditional Jaro-Winkler implementation.
   * Instead of counting all mismatches as 0.5, mismatches that normalize to the same character are counted as 0.05.
   *
   * @param chars1 the characters of the first string
   * @param chars2 the characters of the second string
   * @param matched1 the indices of matched characters in the first string
   * @param matched2 the indices of matched characters in the second string
   * @return the fuzz cost of 0.05 per normalization and 0.5 per transposition
   */
  private static double getFuzzCost(int[] chars1, int[] chars2, boolean[] matched1, boolean[] matched2) {
    double fuzzFactor = 0;

    int index2 = 0;
    for (int index1 = 0; index1 < chars1.length; index1++) {
      if (!matched1[index1]) {
        continue;
      }
      // We know there are an equal number of matches in both strings. Since we found a match in the first,
      // we can safely increment forward to the next match in the second without risking running out of bounds.
      while (!matched2[index2]) {
        ++index2;
      }

      int char2 = chars2[index2];
      ++index2;

      // Full equals, move on.
      if (chars1[index1] == char2) {
        continue;
      }

      // Fuzzy equals, add small cost.
      if (isEqualNormalized(chars1[index1], char2)) {
        fuzzFactor += 0.05;
        continue;
      }

      // Not equal, transposition occurred.
      fuzzFactor += 0.5;
    }

    return fuzzFactor;
  }

}
