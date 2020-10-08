package org.everit.json.schema;

import java.util.List;

import org.everit.json.schema.loader.SchemaLoader;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * @author erosb
 */
public class V6TestSuiteTest {

    private static JettyWrapper server;

    public static List<Arguments> params() {
        return TestCase.loadAsParamsFromPackage("org.everit.json.schema.draft6");
    }

    @BeforeAll
    public static void startJetty() throws Exception {
        (server = new JettyWrapper("/org/everit/json/schema/remotes")).start();
    }

    @AfterAll
    public static void stopJetty() throws Exception {
        server.stop();
    }

    @ParameterizedTest
    @MethodSource("params")
    public void testInCollectingMode(TestCase tc) {
        tc.loadSchema(SchemaLoader.builder().draftV6Support());
        tc.runTestInCollectingMode();
    }

    @ParameterizedTest
    @MethodSource("params")
    public void testInEarlyFailingMode(TestCase tc) {
        tc.loadSchema(SchemaLoader.builder().draftV6Support());
        tc.runTestInEarlyFailureMode();
    }

}
