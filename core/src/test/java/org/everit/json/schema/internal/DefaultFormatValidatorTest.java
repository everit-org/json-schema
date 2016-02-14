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
  public void ipv4Failure() {
    assertFailure("asd", Format.IPV4, "[asd] is not a valid ipv4 address");
  }

  @Test
  public void ipv4LengthFailure() {
    assertFailure(IPV6_ADDR, Format.IPV4,
        "[2001:db8:85a3:0:0:8a2e:370:7334] is not a valid ipv4 address");
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
  public void ipv6Success() {
    assertSuccess(IPV6_ADDR, Format.IPV6);
  }

  @Test(expected = NullPointerException.class)
  public void nullFormat() {
    new DefaultFormatValidator().validate("asd", null);
  }

}
