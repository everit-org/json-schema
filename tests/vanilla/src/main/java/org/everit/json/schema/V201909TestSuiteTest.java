package org.everit.json.schema;

import org.everit.json.schema.loader.SchemaLoader;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.List;

@RunWith(Parameterized.class)
public class V201909TestSuiteTest {


        private static JettyWrapper server;

        @Parameterized.Parameters(name = "{1}")
        public static List<Object[]> params() {
            return TestCase.loadAsParamsFromPackage("org.everit.json.schema.draft201909");
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

        public V201909TestSuiteTest(TestCase testcase, String descr) {
            this.tc = testcase;
            tc.loadSchema(SchemaLoader.builder().draftV201909Support());
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
