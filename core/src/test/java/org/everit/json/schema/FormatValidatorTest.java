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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.List;

@RunWith(Parameterized.class)
public class FormatValidatorTest {

    @Parameters(name = "{0}")
    public static List<Object[]> params() {
        return Arrays.asList(
                new Object[] { "date-time" },
                new Object[] { "email" },
                new Object[] { "hostname" },
                new Object[] { "ipv6" },
                new Object[] { "ipv4" },
                new Object[] { "uri" }
        );
    }

    private final String formatName;

    public FormatValidatorTest(final String formatName) {
        this.formatName = formatName;
    }

    @Test
    public void check() {
        AbstractFormatValidator.forFormat(formatName);
    }

    @Test(expected = NullPointerException.class)
    public void nullFormat() {
        AbstractFormatValidator.forFormat(null);
    }
}
