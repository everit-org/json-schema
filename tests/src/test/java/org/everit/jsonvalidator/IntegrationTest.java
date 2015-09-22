package org.everit.jsonvalidator;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.everit.jsonvalidator.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class IntegrationTest {

  @Parameters
  public static List<Object[]> params() {
    JSONArray arr = loadTests(IntegrationTest.class
        .getResourceAsStream("/org/everit/jsonvalidator/draft4/additionalItems.json"));
    List<Object[]> rval = new ArrayList<>();
    for (int i = 0; i < arr.length(); ++i) {
      JSONObject schemaTest = arr.getJSONObject(i);
      JSONArray testcaseInputs = schemaTest.getJSONArray("tests");
      for (int j = 0; j < testcaseInputs.length(); ++j) {
        JSONObject input = testcaseInputs.getJSONObject(j);
        Object[] params = new Object[5];
        params[0] = schemaTest.getString("description");
        params[1] = schemaTest.get("schema");
        params[2] = input.getString("description");
        params[3] = input.get("data");
        params[4] = input.getBoolean("valid");
        rval.add(params);
      }
    }
    return rval;
  }

  private static JSONArray loadTests(final InputStream input) {
    return new JSONArray(new JSONTokener(input));
  }

  private final String schemaDescription;

  private final JSONObject schemaJson;

  private final String inputDescription;

  private final Object input;

  private final boolean expectedToBeValid;

  public IntegrationTest(final String schemaDescription, final JSONObject schemaJson,
      final String inputDescription,
      final Object input, final Boolean expectedToBeValid) {
    this.schemaDescription = schemaDescription;
    this.schemaJson = schemaJson;
    this.inputDescription = inputDescription;
    this.input = input;
    this.expectedToBeValid = expectedToBeValid;
  }

  @Test
  public void test() {
    try {
      Schema schema = SchemaLoader.load(schemaJson);
      schema.validate(input);
      if (!expectedToBeValid) {
        throw new AssertionError("false success for " + inputDescription);
      }
    } catch (ValidationException e) {
      if (expectedToBeValid) {
        throw new AssertionError("false failure for " + inputDescription, e);
      }
    } catch (SchemaException e) {
      throw new AssertionError("schema loading failure for " + schemaDescription, e);
    }
  }

}
