package org.example;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class BridgeWordsTest {

  @Before
  public void setup() {
    String content = "a b c d a b a b a b a b a b c d a b a b c d a c,dad 1s a dad d d c d s a c a c a s d a w a s d a s d a s d a s d a";
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

    for (int i = 0; i < words.length - 1; i++) {
      String word1 = words[i];
      String word2 = words[i + 1];
      TextProcessor.updateGraph(word1, word2);
    }
  }

  @Test
  public void testBridgeWordsExist() {
    String result = TextProcessor.queryBridgeWords("a", "d");
    assertEquals("The bridge words from a to d are: c, s, dad.", result);
  }

  @Test
  public void testNoBridgeWords() {
    String result = TextProcessor.queryBridgeWords("a", "b");
    assertEquals("No bridge words from a to b!", result);
  }

  @Test
  public void testWordNotInGraph() {
    String result = TextProcessor.queryBridgeWords("a", "elephant");
    assertEquals("No word1 or word2 in the graph!", result);
  }

  @Test
  public void testMinimalGraph() {
    String result = TextProcessor.queryBridgeWords("b", "w");
    assertEquals("The bridge words from b to w are: a.", result);
  }

  @Test
  public void testLargeGraph() {
    String result = TextProcessor.queryBridgeWords("c", "s");
    assertEquals("The bridge words from c to s are: a, d, dad.", result);
  }

  @Test
  public void testInvalidInput() {
    String result = TextProcessor.queryBridgeWords("", "123");
    assertEquals("No word1 or word2 in the graph!", result);
  }
}
