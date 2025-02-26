package com.github.sonarperl.it;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.Collection;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.sonarqube.ws.client.HttpConnector;
import org.sonarqube.ws.client.WsClientFactories;

import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.build.SonarScanner;

@RunWith(Parameterized.class)
public class TestMetricsIntegrationJUnit2Test {

    private static final String PROJECT_KEY = "tap_junit2";

    @ClassRule
    public static TestRule RESOURCES = IntegrationTests.RESOURCES;

    @Parameters
    public static Collection<Orchestrator> orchestrators() {
        return IntegrationTests.orchestrators();
    }

    private static final SonarScanner build;

    static {
        build = SonarScanner.create()
                .setProjectDir(new File("projects/tap_junit2"))
                .setProjectKey(PROJECT_KEY)
                .setProjectName(PROJECT_KEY)
                .setProjectVersion("1.0-SNAPSHOT")
                .setProperty("sonar.perl.testHarness.junitPath", "junit_output.xml")
                .setSourceDirs("lib")
                .setTestDirs("t");
    }

    private static TestSonarClient wsClient;

    public TestMetricsIntegrationJUnit2Test(Orchestrator orchestrator) {
        orchestrator.executeBuild(build);
        wsClient = new TestSonarClient(
                WsClientFactories.getDefault().newClient(HttpConnector.newBuilder()
                .url(orchestrator.getServer().getUrl())
                .build()), PROJECT_KEY);
    }

    @Test
    public void file_level() {
      // test count
        assertThat(wsClient.getFileMeasure("t/Project.t","tests")).isEqualTo(2);
        assertThat(wsClient.getFileMeasure("t/Project.t", "test_failures")).isEqualTo(1);
    }

}
