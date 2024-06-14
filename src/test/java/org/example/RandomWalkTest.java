package org.example;

import org.junit.Test;
import org.junit.Before;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.example.TextProcessor.readFileToString;
import static org.junit.Assert.*;

public class RandomWalkTest {

    @Before
    public void setup() {
        String content = "";
        // 转换为小写
        content = content.toLowerCase();
        // 将标点符号替换为空格
        content = content.replaceAll("[.,!?;:]", " ");
        // 将换行符和回车符替换为空格
        content = content.replaceAll("\\r?\\n", " ");
        // 使用正则表达式将非字母字符替换为空格
        content = content.replaceAll("[^a-zA-Z ]", "");
        // 分割单词
        String[] words = content.split("\\s+");

        // Clear the graph before each test
        TextProcessor.graph.clear();
    }

    @Test
    public void testGraphIsEmpty() {
        TextProcessor.graph.clear();
        String result = TextProcessor.randomWalk();
        System.out.println(result);
        assertEquals("Graph is empty", result);
    }

    @Test
    public void testNoMoreNeighbors() {
        TextProcessor.graph.clear();
        TextProcessor.graph.put("A", Map.of("B", 1));
        TextProcessor.graph.put("B", Map.of());
        String result = TextProcessor.randomWalk();
        assertTrue(result.equals("A B") || result.equals("B")); // Depending on random start
    }

    @Test
    public void testUserStopsTraversal() {
        TextProcessor.graph.clear();
        TextProcessor.graph.put("A", Map.of("B", 1, "C", 1));
        TextProcessor.graph.put("B", Map.of("D", 1));
        TextProcessor.graph.put("C", Map.of("D", 1));
        TextProcessor.graph.put("D", Map.of());

        // Simulate user pressing Enter
        System.setIn(new java.io.ByteArrayInputStream("\n".getBytes(StandardCharsets.UTF_8)));

        String result = TextProcessor.randomWalk();
        assertNotNull(result);
    }

    @Test
    public void testAlreadyVisitedEdge() {
        TextProcessor.graph.clear();
        TextProcessor.graph.put("A", Map.of("B", 1));
        TextProcessor.graph.put("B", Map.of("A", 1));
        String result = TextProcessor.randomWalk();
        assertTrue(result.equals("A B A") || result.equals("B A B"));
    }
}