/*
 * Copyright (C) 2011-2021 lishid. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.lishid.openinv.util;

public class StringMetric {

    public static float compareJaroWinkler(String a, String b) {
        final float jaroScore = compareJaro(a, b);

        if (jaroScore < (float) 0.7) {
            return jaroScore;
        }

        String prefix = commonPrefix(a, b);
        int prefixLength = Math.min(prefix.codePointCount(0, prefix.length()), 4);

        return jaroScore + (prefixLength * (float) 0.1 * (1.0f - jaroScore));

    }

    private static float compareJaro(String a, String b) {
        if (a.isEmpty() && b.isEmpty()) {
            return 1.0f;
        }

        if (a.isEmpty() || b.isEmpty()) {
            return 0.0f;
        }

        final int[] charsA = a.codePoints().toArray();
        final int[] charsB = b.codePoints().toArray();

        // Intentional integer division to round down.
        final int halfLength = Math.max(0, Math.max(charsA.length, charsB.length) / 2 - 1);

        final int[] commonA = getCommonCodePoints(charsA, charsB, halfLength);
        final int[] commonB = getCommonCodePoints(charsB, charsA, halfLength);

        // commonA and commonB will always contain the same multi-set of
        // characters. Because getCommonCharacters has been optimized, commonA
        // and commonB are -1-padded. So in this loop we count transposition
        // and use commonCharacters to determine the length of the multi-set.
        float transpositions = 0;
        int commonCharacters = 0;
        for (int length = commonA.length; commonCharacters < length
                && commonA[commonCharacters] > -1; commonCharacters++) {
            if (commonA[commonCharacters] != commonB[commonCharacters]) {
                transpositions++;
            }
        }

        if (commonCharacters == 0) {
            return 0.0f;
        }

        float aCommonRatio = commonCharacters / (float) charsA.length;
        float bCommonRatio = commonCharacters / (float) charsB.length;
        float transpositionRatio = (commonCharacters - transpositions / 2.0f) / commonCharacters;

        return (aCommonRatio + bCommonRatio + transpositionRatio) / 3.0f;
    }

    /*
     * Returns an array of code points from a within b. A character in b is
     * counted as common when it is within separation distance from the position
     * in a.
     */
    private static int[] getCommonCodePoints(final int[] charsA, final int[] charsB, final int separation) {
        final int[] common = new int[Math.min(charsA.length, charsB.length)];
        final boolean[] matched = new boolean[charsB.length];

        // Iterate of string a and find all characters that occur in b within
        // the separation distance. Mark any matches found to avoid
        // duplicate matchings.
        int commonIndex = 0;
        for (int i = 0, length = charsA.length; i < length; i++) {
            final int character = charsA[i];
            final int index = indexOf(character, charsB, i - separation, i
                    + separation + 1, matched);
            if (index > -1) {
                common[commonIndex++] = character;
                matched[index] = true;
            }
        }

        if (commonIndex < common.length) {
            common[commonIndex] = -1;
        }

        // Both invocations will yield the same multi-set terminated by -1, so
        // they can be compared for transposition without making a copy.
        return common;
    }

    /*
     * Search for code point in buffer starting at fromIndex to toIndex - 1.
     *
     * Returns -1 when not found.
     */
    private static int indexOf(int character, int[] buffer, int fromIndex, int toIndex, boolean[] matched) {

        // compare char with range of characters to either side
        for (int j = Math.max(0, fromIndex), length = Math.min(toIndex, buffer.length); j < length; j++) {
            // check if found
            if (buffer[j] == character && !matched[j]) {
                return j;
            }
        }

        return -1;
    }

    private static String commonPrefix(CharSequence a, CharSequence b) {
        int maxPrefixLength = Math.min(a.length(), b.length());

        int p;

        p = 0;
        while (p < maxPrefixLength && a.charAt(p) == b.charAt(p)) {
            ++p;
        }

        if (validSurrogatePairAt(a, p - 1) || validSurrogatePairAt(b, p - 1)) {
            --p;
        }

        return a.subSequence(0, p).toString();
    }

    private static boolean validSurrogatePairAt(CharSequence string, int index) {
        return index >= 0 && index <= string.length() - 2 && Character.isHighSurrogate(string.charAt(index)) && Character.isLowSurrogate(string.charAt(index + 1));
    }

    private StringMetric(){}

}
