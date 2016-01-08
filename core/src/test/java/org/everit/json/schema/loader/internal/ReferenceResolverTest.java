package org.everit.json.schema.loader.internal;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ReferenceResolverTest {

  @Parameters(name = "{0}")
  public static List<Object[]> params() {
    return Arrays.asList(
        parList("fragment id", "http://x.y.z/root.json#foo", "http://x.y.z/root.json", "#foo"),
        parList("rel path", "http://example.org/foo", "http://example.org/bar", "foo"),
        parList("new root", "http://bserver.com", "http://aserver.com/",
            "http://bserver.com"));
  }

  private static Object[] parList(final String... params) {
    return params;
  }

  private final String expectedOutput;

  private final String parentScope;

  private final String encounteredSegment;

  public ReferenceResolverTest(final String testcaseName, final String expectedOutput,
      final String parentScope,
      final String encounteredSegment) {
    this.expectedOutput = expectedOutput;
    this.parentScope = parentScope;
    this.encounteredSegment = encounteredSegment;
  }

  @Test
  public void test() {
    String actual = ReferenceResolver.resolve(parentScope, encounteredSegment);
    Assert.assertEquals(expectedOutput, actual);
  }

}
