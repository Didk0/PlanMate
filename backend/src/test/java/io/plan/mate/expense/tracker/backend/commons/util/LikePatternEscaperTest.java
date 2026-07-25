package io.plan.mate.expense.tracker.backend.commons.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("LikePatternEscaper")
class LikePatternEscaperTest {

  @ParameterizedTest
  @CsvSource({
    "percent%sign,      percent\\%sign",
    "under_score,       under\\_score",
    "back\\slash,        back\\\\slash",
    "plain,              plain",
  })
  @DisplayName("escape backslash-escapes LIKE wildcard characters so they match literally")
  void escape_shouldBackslashEscapeWildcards_whenValueContainsPercentOrUnderscoreOrBackslash(
      String input, String expected) {

    assertThat(LikePatternEscaper.escape(input)).isEqualTo(expected);
  }

  @Test
  @DisplayName("escape leaves an already-safe value unchanged")
  void escape_shouldReturnValueUnchanged_whenNoWildcardCharactersPresent() {

    assertThat(LikePatternEscaper.escape("groceries")).isEqualTo("groceries");
  }
}
