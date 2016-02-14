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
package org.everit.json.schema.internal;

import java.util.Optional;

import org.everit.json.schema.Format;
import org.junit.Assert;
import org.junit.Test;

public class DefaultFormatValidatorTest {

  private static final String THERE_IS_NO_PLACE_LIKE = "127.0.0.1";

  private static final String IPV6_ADDR = "2001:db8:85a3:0:0:8a2e:370:7334";

  private void assertFailure(final String subject, final Format format, final String expectedFailure) {
    Optional<String> opt = new DefaultFormatValidator().validate(subject, format);
    Assert.assertNotNull("the optional is not null", opt);
    Assert.assertTrue("failure exists", opt.isPresent());
    Assert.assertEquals(expectedFailure, opt.get());
  }

  private void assertSuccess(final String subject, final Format format) {
    Optional<String> opt = new DefaultFormatValidator().validate(subject, format);
    Assert.assertNotNull("the optional is not null", opt);
    Assert.assertFalse("failure not exist", opt.isPresent());
  }

  @Test
  public void dateTimeFormatFailure() {
    assertFailure("2015-03-13T11:00:000", Format.DATE_TIME,
        "[2015-03-13T11:00:000] is not a valid date-time");
  }

  @Test
  public void dateTimeSecFracSuccess() {
    assertSuccess("2015-02-30T11:00:00.111Z", Format.DATE_TIME);
  }

  @Test
  public void dateTimeSuccess() {
    assertSuccess("2015-03-13T11:00:00+00:00", Format.DATE_TIME);
  }

  @Test
  public void dateTimeZSuccess() {
    assertSuccess("2015-02-30T11:00:00Z", Format.DATE_TIME);
  }

  @Test
  public void emailFailure() {
    assertFailure("a.@b.com", Format.EMAIL, "[a.@b.com] is not a valid email address");
  }

  @Test
  public void emailSuccess() {
    assertSuccess("a@b.com", Format.EMAIL);
  }

  @Test
  public void hostnameLengthFailure() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 256; ++i) {
      sb.append('a');
    }
    String subject = sb.toString();
    assertFailure(subject, Format.HOSTNAME, "[" + subject + "] is not a valid hostname");
  }

  @Test
  public void hostnameNullFailure() {
    assertFailure(null, Format.HOSTNAME, "[null] is not a valid hostname");
  }

  @Test
  public void hostnameSuccess() {
    assertSuccess("localhost", Format.HOSTNAME);
  }

  @Test
  public void ipv4Failure() {
    assertFailure("asd", Format.IPV4, "[asd] is not a valid ipv4 address");
  }

  @Test
  public void ipv4LengthFailure() {
    assertFailure(IPV6_ADDR, Format.IPV4,
        "[2001:db8:85a3:0:0:8a2e:370:7334] is not a valid ipv4 address");
  }

  @Test
  public void ipv4NullFailure() {
    assertFailure(null, Format.IPV4, "[null] is not a valid ipv4 address");
  }

  @Test
  public void ipv4Success() {
    assertSuccess(THERE_IS_NO_PLACE_LIKE, Format.IPV4);
  }

  @Test
  public void ipv6Failure() {
    assertFailure("asd", Format.IPV6, "[asd] is not a valid ipv6 address");
  }

  @Test
  public void ipv6LengthFailure() {
    assertFailure(THERE_IS_NO_PLACE_LIKE, Format.IPV6, "[127.0.0.1] is not a valid ipv6 address");
  }

  @Test
  public void ipv6NullFailure() {
    assertFailure(null, Format.IPV6, "[null] is not a valid ipv6 address");
  }

  @Test
  public void ipv6Success() {
    assertSuccess(IPV6_ADDR, Format.IPV6);
  }

  @Test(expected = NullPointerException.class)
  public void nullFormat() {
    new DefaultFormatValidator().validate("asd", null);
  }

  @Test
  public void uriFailure() {
    assertFailure("12 34", Format.URI, "[12 34] is not a valid URI");
  }

  @Test
  public void uriNullFailure() {
    assertFailure(null, Format.URI, "[null] is not a valid URI");
  }

  @Test
  public void uriSuccess() {
    assertSuccess("http://example.org:8080/example.html", Format.URI);
  }

}
