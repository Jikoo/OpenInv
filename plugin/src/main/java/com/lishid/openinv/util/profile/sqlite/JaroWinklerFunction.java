package com.lishid.openinv.util.profile.sqlite;

import com.lishid.openinv.util.StringMetric;
import org.sqlite.Function;

import java.sql.SQLException;

public class JaroWinklerFunction extends Function {

  @Override
  protected void xFunc() throws SQLException {
    if (args() != 2) {
      throw new SQLException("JaroWinkler(str, str) requires 2 arguments but got " + args());
    }
    String val1 = value_text(0);
    String val2 = value_text(1);

    result(StringMetric.compareJaroWinkler(val1 == null ? "" : val1, val2 == null ? "" : val2));
  }

}
