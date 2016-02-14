/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
