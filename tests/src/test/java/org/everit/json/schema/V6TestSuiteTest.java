package org.everit.json.schema;

import java.util.List;

import org.everit.json.schema.loader.SchemaLoader;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * @author erosb
 */
@RunWith(Parameterized.class)
public class V6TestSuiteTest {

    private static JettyWrapper server;

    @Parameterized.Parameters(name = "{1}")
    public static List<Object[]> params() {
        return TestCase.loadAsParamsFromPackage("org.everit.json.schema.draft6");
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

    public V6TestSuiteTest(TestCase testcase, String descr) {
        this.tc = testcase;
        tc.loadSchema(SchemaLoader.builder().draftV6Support());
    }

    @Test
    public void testInCollectingMode() {
        tc.runTestInCollectingMode();
    }

    @Test
    public void testInEarlyFailingMode() {
        tc.runTestInEarlyFailureMode();
    }

}
