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

import com.google.common.base.Optional;
import org.everit.json.schema.FormatValidator;
import org.junit.Assert;
import org.junit.Test;

public class DefaultFormatValidatorTest {

    private static final String THERE_IS_NO_PLACE_LIKE = "127.0.0.1";

    private static final String IPV6_ADDR = "2001:db8:85a3:0:0:8a2e:370:7334";

    private void assertFailure(final String subject, final FormatValidator format,
            final String expectedFailure) {
        Optional<String> opt = format.validate(subject);
        Assert.assertNotNull("the optional is not null", opt);
        Assert.assertTrue("failure exists", opt.isPresent());
        Assert.assertEquals(expectedFailure, opt.get());
    }

    private void assertSuccess(final String subject, final FormatValidator format) {
        Optional<String> opt = format.validate(subject);
        Assert.assertNotNull("the optional is not null", opt);
        Assert.assertFalse("failure not exist", opt.isPresent());
    }

    @Test
    public void dateTimeExceedingLimits() {
        assertFailure("1996-60-999T16:39:57-08:00", new DateTimeFormatValidator(),
                "[1996-60-999T16:39:57-08:00] is not a valid date-time. Expected [yyyy-MM-dd'T'HH:mm:ssZ, yyyy-MM-dd'T'HH:mm:ss.[0-9]{1,9}Z]");
    }

    @Test
    public void dateTimeFormatFailure() {
        assertFailure("2015-03-13T11:00:000", new DateTimeFormatValidator(),
                "[2015-03-13T11:00:000] is not a valid date-time. Expected [yyyy-MM-dd'T'HH:mm:ssZ, yyyy-MM-dd'T'HH:mm:ss.[0-9]{1,9}Z]");
    }

    @Test
    public void dateTimeWithSingleDigitInSecFracSuccess() {
        assertSuccess("2015-02-28T11:00:00.1Z", new DateTimeFormatValidator());
    }

    @Test
    public void dateTimeWithTwoDigitsInSecFracSuccess() {
        assertSuccess("2015-02-28T11:00:00.12Z", new DateTimeFormatValidator());
    }

    @Test
    public void dateTimeWithThreeDigitsInSecFracSuccess() {
        assertSuccess("2015-02-28T11:00:00.123Z", new DateTimeFormatValidator());
    }

    @Test
    public void dateTimeWithFourDigitsInSecFracSuccess() {
        assertSuccess("2015-02-28T11:00:00.1234Z", new DateTimeFormatValidator());
    }

    @Test
    public void dateTimeWithFiveDigitsInSecFracSuccess() {
        assertSuccess("2015-02-28T11:00:00.12345Z", new DateTimeFormatValidator());
    }

    @Test
    public void dateTimeWithSixDigitsInSecFracSuccess() {
        assertSuccess("2015-02-28T11:00:00.123456Z", new DateTimeFormatValidator());
    }

    @Test
    public void dateTimeWithSevenDigitsInSecFracSuccess() {
        assertSuccess("2015-02-28T11:00:00.1234567Z", new DateTimeFormatValidator());
    }

    @Test
    public void dateTimeWithEightDigitsInSecFracSuccess() {
        assertSuccess("2015-02-28T11:00:00.12345678Z", new DateTimeFormatValidator());
    }

    @Test
    public void dateTimeWithNineDigitsInSecFracSuccess() {
        assertSuccess("2015-02-28T11:00:00.123456789Z", new DateTimeFormatValidator());
    }

    @Test
    public void dateTimeWithTenDigitsInSecFracFailure() {
        assertFailure("2015-02-28T11:00:00.1234567890Z", new DateTimeFormatValidator(),
                "[2015-02-28T11:00:00.1234567890Z] is not a valid date-time. Expected [yyyy-MM-dd'T'HH:mm:ssZ, yyyy-MM-dd'T'HH:mm:ss.[0-9]{1,9}Z]");
    }

    @Test
    public void dateTimeSuccess() {
        assertSuccess("2015-03-13T11:00:00+00:00", new DateTimeFormatValidator());
    }

    @Test
    public void dateTimeZSuccess() {
        assertSuccess("2015-02-28T11:00:00Z", new DateTimeFormatValidator());
    }

    @Test
    public void emailFailure() {
        assertFailure("a.@b.com", new EmailFormatValidator(), "[a.@b.com] is not a valid email address");
    }

    @Test
    public void emailSuccess() {
        assertSuccess("a@b.com", new EmailFormatValidator());
    }

    @Test
    public void hostnameLengthFailure() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 256; ++i) {
            sb.append('a');
        }
        String subject = sb.toString();
        assertFailure(subject, new HostnameFormatValidator(), "[" + subject
                + "] is not a valid hostname");
    }

    @Test
    public void hostnameNullFailure() {
        assertFailure(null, new HostnameFormatValidator(), "[null] is not a valid hostname");
    }

    @Test
    public void hostnameSuccess() {
        assertSuccess("localhost", new HostnameFormatValidator());
    }

    @Test
    public void ipv4Failure() {
        assertFailure("asd", new IPV4Validator(), "[asd] is not a valid ipv4 address");
    }

    @Test
    public void ipv4LengthFailure() {
        assertFailure(IPV6_ADDR, new IPV4Validator(),
                "[2001:db8:85a3:0:0:8a2e:370:7334] is not a valid ipv4 address");
    }

    @Test
    public void ipv4NullFailure() {
        assertFailure(null, new IPV4Validator(), "[null] is not a valid ipv4 address");
    }

    @Test
    public void ipv4Success() {
        assertSuccess(THERE_IS_NO_PLACE_LIKE, new IPV4Validator());
    }

    @Test
    public void ipv6Failure() {
        assertFailure("asd", new IPV6Validator(), "[asd] is not a valid ipv6 address");
    }

    @Test
    public void ipv6LengthFailure() {
        assertFailure(THERE_IS_NO_PLACE_LIKE, new IPV6Validator(),
                "[127.0.0.1] is not a valid ipv6 address");
    }

    @Test
    public void ipv6NullFailure() {
        assertFailure(null, new IPV6Validator(), "[null] is not a valid ipv6 address");
    }

    @Test
    public void ipv6Success() {
        assertSuccess(IPV6_ADDR, new IPV6Validator());
    }

    @Test
    public void uriFailure() {
        assertFailure("12 34", new URIFormatValidator(), "[12 34] is not a valid URI");
    }

    @Test
    public void uriNullFailure() {
        assertFailure(null, new URIFormatValidator(), "[null] is not a valid URI");
    }

    @Test
    public void uriSuccess() {
        assertSuccess("http://example.org:8080/example.html", new URIFormatValidator());
    }

}
