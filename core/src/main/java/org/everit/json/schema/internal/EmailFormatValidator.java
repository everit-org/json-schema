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
import org.apache.commons.validator.routines.EmailValidator;
import org.everit.json.schema.AbstractFormatValidator;

/**
 * Implementation of the "email" format value.
 */
public class EmailFormatValidator extends AbstractFormatValidator {

    @Override
    public Optional<String> validate(final String subject) {
        if (EmailValidator.getInstance(false, true).isValid(subject)) {
            return Optional.absent();
        }
        return Optional.of(String.format("[%s] is not a valid email address", subject));
    }

    @Override
    public String formatName() {
        return "email";
    }
}
