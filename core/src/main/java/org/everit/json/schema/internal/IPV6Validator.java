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

/**
 * Implementation of the "ipv6" format value.
 */
public class IPV6Validator extends IPAddressValidator implements FormatValidator {

    private static final int IPV6_LENGTH = 16;

    @Override
    public Optional<String> validate(final String subject) {
        return checkIpAddress(subject, IPV6_LENGTH, "[%s] is not a valid ipv6 address");
    }

    @Override
    public String formatName() {
        return "ipv6";
    }
}
