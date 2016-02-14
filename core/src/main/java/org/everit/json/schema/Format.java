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
