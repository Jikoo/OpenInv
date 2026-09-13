package com.lishid.openinv.util.profile.sqlite;

import com.lishid.openinv.util.FuzzyJaroWinkler;
import org.sqlite.Function;

import java.sql.SQLException;

public class JaroWinklerFunction extends Function {

  @Override
  protected void xFunc() throws SQLException {
    if (args() != 2) {
      throw new SQLException("JaroWinkler(str, str) requires 2 arguments but got " + args());
    }

    result(FuzzyJaroWinkler.getSimilarity(value_text(0), value_text(1)));
  }

}
