package org.everit.json.schema;

import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.List;

@RunWith(Parameterized.class)
public class TestSuiteTest {

    private static JettyWrapper server;

    @Parameters(name = "{1}")
    public static List<Object[]> params() {
        return TestCase.loadAsParamsFromPackage("org.everit.json.schema.draft4");
    }

    @BeforeClass
    public static void startJetty() throws Exception {
        (server = new JettyWrapper("/org/everit/json/schema/draft4/remotes")).start();
    }

    @AfterClass
    public static void stopJetty() throws Exception {
        if (server != null) {
            server.stop();
        }
    }

    private TestCase tc;

    public TestSuiteTest(TestCase testcase, String descr) {
        this.tc = testcase;
    }

    @Test
    public void test() {
        try {
            Schema schema = SchemaLoader.load(tc.schemaJson);
            schema.validate(tc.inputData);
            if (!tc.expectedToBeValid) {
                throw new AssertionError("false success for " + tc.inputDescription);
            }
        } catch (ValidationException e) {
            if (tc.expectedToBeValid) {
                throw new AssertionError("false failure for " + tc.inputDescription, e);
            }
        } catch (SchemaException e) {
            throw new AssertionError("schema loading failure for " + tc.schemaDescription, e);
        } catch (JSONException e) {
            throw new AssertionError("schema loading error for " + tc.schemaDescription, e);
        }
    }
}
