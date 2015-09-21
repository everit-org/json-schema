package org.everit.jsonvalidator;

import java.io.InputStream;

import org.everit.jsonvalidator.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Test;

public class IntegrationTest {

  private JSONArray loadTests(final InputStream input) {
    return new JSONArray(new JSONTokener(input));
  }

  private void printReport(final int schemaCount, final int schemaFailureCount,
      final int validationTrialCount,
      final int falseFailures, final int falseSuccesses) {
    String output = String.format("schema count: %d\n"
        + "\tschema failure count: %d\n"
        + "\tvalidation trial count: %d\n"
        + "\tfalse failures: %d\n"
        + "\tfalse successes: %d", schemaCount, schemaFailureCount, validationTrialCount,
        falseFailures, falseSuccesses);
    System.out.println(output);
  }

  @Test
  public void test() {
    JSONArray arr = loadTests(IntegrationTest.class
        .getResourceAsStream("/org/everit/jsonvalidator/draft4/additionalItems.json"));
    int schemaCount = arr.length();
    int schemaFailureCount = 0, validationTrialCount = 0;
    int falseFailures = 0, falseSuccesses = 0;
    for (int i = 0; i < arr.length(); ++i) {
      JSONObject testcase = arr.getJSONObject(i);
      try {
        Schema schema = SchemaLoader.load(testcase.getJSONObject("schema"));
        JSONArray testcaseInputs = testcase.getJSONArray("tests");
        for (int j = 0; j < testcaseInputs.length(); ++j) {
          JSONObject inputDescr = testcaseInputs.getJSONObject(j);
          boolean expectedToBeValid = inputDescr.getBoolean("valid");
          try {
            ++validationTrialCount;
            schema.validate(inputDescr.get("data"));
            if (!expectedToBeValid) {
              falseSuccesses++;
            }
          } catch (ValidationException e) {
            if (expectedToBeValid) {
              falseFailures++;
            }
          }
        }
      } catch (SchemaException e) {
        System.out.println("schema loading failure: ");
        e.printStackTrace();
        ++schemaFailureCount;
      }
    }
    printReport(schemaCount, schemaFailureCount, validationTrialCount, falseFailures,
        falseSuccesses);
  }

}
