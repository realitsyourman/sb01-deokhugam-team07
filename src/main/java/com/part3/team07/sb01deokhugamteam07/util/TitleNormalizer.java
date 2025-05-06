package com.part3.team07.sb01deokhugamteam07.util;

import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.Expressions;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class TitleNormalizer {

  private final Environment environment;

  public TitleNormalizer(Environment environment) {
    this.environment = environment;
  }

  public StringExpression getNormalizedTitle(StringExpression title) {
    if (isTestProfile()) {
      return Expressions.stringTemplate(
          "REPLACE(REPLACE(LOWER({0}), ' ', ''), '-', '')",
          title
      );
    }
    return Expressions.stringTemplate(
        "REGEXP_REPLACE(LOWER({0}), '[^가-힣a-z0-9]', '', 'g')",
        title
    );
  }

  private boolean isTestProfile() {
    for (String profile : environment.getActiveProfiles()) {
      if (profile.equalsIgnoreCase("test")) {
        return true;
      }
    }
    return false;
  }
}

