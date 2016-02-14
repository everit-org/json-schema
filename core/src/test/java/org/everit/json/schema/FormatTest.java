package org.everit.json.schema;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class FormatTest {

  @Parameters
  public static List<Object[]> params() {
    return Arrays.asList(
        new Object[] { Format.DATE_TIME, "date-time" },
        new Object[] { Format.EMAIL, "email" },
        new Object[] { Format.HOSTNAME, "hostname" },
        new Object[] { Format.IPV6, "ipv6" },
        new Object[] { Format.IPV4, "ipv4" },
        new Object[] { Format.URI, "uri" }
        );
  }

  private final Format format;

  private final String formatName;

  public FormatTest(final Format format, final String formatName) {
    this.format = format;
    this.formatName = formatName;
  }

  @Test
  public void check() {
    Assert.assertEquals(format, Format.forName(formatName));
  }

  @Test(expected = IllegalArgumentException.class)
  public void nonexistent() {
    Format.forName("nonexistent");
  }

}
