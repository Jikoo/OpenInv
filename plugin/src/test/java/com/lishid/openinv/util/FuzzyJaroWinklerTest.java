package com.lishid.openinv.util;

import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@NullMarked
class FuzzyJaroWinklerTest {

  @ParameterizedTest
  @MethodSource("getLookups")
  void checkBestName(String lookup, String far, String near) {
    double similarityFar = FuzzyJaroWinkler.getSimilarity(lookup, far);
    double similarityNear = FuzzyJaroWinkler.getSimilarity(lookup, near);

    assertThat("Near must be better match than far", similarityNear, is(greaterThan(similarityFar)));
  }

  private List<Arguments> getLookups() {
    return Arrays.asList(
        Arguments.of("johnminecraft", "frankminecraft", "johnminecraft123"),
        Arguments.of("johnminecraft", "johnENIMCRAFT", "johnMINECRAFT"),
        Arguments.of("johnminecraft", "johncavecraft", "JOHNMINECRAFT"),
        Arguments.of("alice1234", "aleci1234", "ALICE1234"),
        Arguments.of("ALICEMC", "ALICECM", "alicemc"),
        Arguments.of("ALICEMINECRAFT", "aliceminceraft", "aliceminecraft"),
        Arguments.of("aliceminecraf", "aliceminecraft", "ALICEMINECRAF")
    );
  }

}
