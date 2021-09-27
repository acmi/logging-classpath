package ru.dexsys.logging;

import io.github.classgraph.ClassGraph;
import junit.framework.AssertionFailedError;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ClassPathTest {
    @Test
    public void testClasspathForDuplicates()  {
        Map<String, List<String>> duplicates = new HashMap<>();
        for (var dup : new ClassGraph().scan().getAllResources().findDuplicatePaths()) {
            var path = dup.getKey();
            if (!isHarmlessDuplicate(path)) {
                duplicates.put(path, dup.getValue().stream()
                        .map(resource -> resource.getURL().toExternalForm()).collect(Collectors.toList()));
            }
        }

        if (!duplicates.isEmpty()) {
            throw new AssertionFailedError("Classpath duplicates detected: " + toString(duplicates));
        }
    }

    boolean isHarmlessDuplicate(String resourcePath) {
        return resourcePath.equals("__packageIndex__")
                || resourcePath.equals("module-info.class")
                || resourcePath.startsWith("META-INF")
                || resourcePath.startsWith("org/apache/maven")
                || resourcePath.toLowerCase().startsWith("license");
    }

    String toString(Map<String, List<String>> map) {
        var sb = new StringBuilder();
        for (var entry : map.entrySet()) {
            sb.append(entry.getKey());
            sb.append('\n');
            for (var location : entry.getValue()) {
                sb.append("    ");
                sb.append(location);
                sb.append('\n');
            }
        }
        return sb.toString();
    }
}
