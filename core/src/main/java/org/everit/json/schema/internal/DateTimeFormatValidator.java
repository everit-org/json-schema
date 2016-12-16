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

/**
 * Implementation of the "date-time" format value.
 */
public class DateTimeFormatValidator extends DateTimeSpecFormatValidator {

    private static final String[] DATETIME_FORMAT_STRINGS = {"yyyy-MM-dd'T'HH:mm:ssXXX", "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"};

    @Override
    public String formatName() {
        return "date-time";
    }

    @Override
    protected String[] getValidDatetimeFormats() {
        return DATETIME_FORMAT_STRINGS;
    }
}
