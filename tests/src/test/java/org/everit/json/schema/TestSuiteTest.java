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
        (server = new JettyWrapper("/org/everit/json/schema/remotes")).start();
    }

    @AfterClass
    public static void stopJetty() throws Exception {
        server.stop();
    }

    private TestCase tc;

    public TestSuiteTest(TestCase testcase, String descr) {
        this.tc = testcase;
    }

    @Test
    public void test() {
        tc.runTest(SchemaLoader.builder());
    }
}
