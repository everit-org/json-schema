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

public enum Format {

  DATE_TIME {

    @Override
    public String toString() {
      return "date-time";
    }

  },
  EMAIL {

    @Override
    public String toString() {
      return "email";
    }

  },
  HOSTNAME {

    @Override
    public String toString() {
      return "hostname";
    }

  },
  IPV4 {

    @Override
    public String toString() {
      return "ipv4";
    }

  },
  IPV6 {

    @Override
    public String toString() {
      return "ipv6";
    }

  },
  URI {

    @Override
    public String toString() {
      return "uri";
    }

  };

  public static Format forName(final String name) {
    return Arrays.stream(Format.values())
        .filter(fmt -> fmt.toString().equals(name))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("format [" + name + "] is not supported"));
  }

}
