package com.railops;

import static org.assertj.core.api.Assertions.assertThat;
import java.nio.file.*;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.*;
import org.yaml.snakeyaml.constructor.SafeConstructor;

class InfrastructureConfigTest {
    @Test void composeAndMonitoringYamlParseAndKeepBackendPortsPrivate() throws Exception {
        var yaml = new Yaml(new SafeConstructor(new LoaderOptions()));
        Path root = Path.of("..").toAbsolutePath().normalize();
        Map<String, Object> compose = yaml.load(Files.readString(root.resolve("docker-compose.yml")));
        Map<?, ?> services = (Map<?, ?>) compose.get("services");
        assertThat(services.keySet().toString()).contains("postgres", "backend", "frontend", "prometheus", "grafana", "k6");
        assertThat(((Map<?, ?>) services.get("backend")).get("ports")).isNull();
        for (String file : new String[] {".github/workflows/ci.yml", "infra/prometheus/prometheus.yml", "infra/grafana/provisioning/datasources/prometheus.yml"}) {
            assertThat((Object) yaml.load(Files.readString(root.resolve(file)))).isInstanceOf(Map.class);
        }
    }
}
