package org.example;

import com.mxgraph.layout.*;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;


/**
 * 实验一.
 */
public class TextProcessor {
  //Map是一个键值对集合，二维Map代表图，第一个String是节点名，第二个Map是邻接节点和权重集合
  public static final Map<String, Map<String, Integer>> graph = new HashMap<>();

  /**
   * 读取文件.
   */
  public static String readFileToString(String fileName) {
    File file = new File(fileName);
    long fileLength = file.length();
    byte[] fileContent = new byte[(int) fileLength];

    try (FileInputStream in = new FileInputStream(file)) {
      int bytesRead = in.read(fileContent);
      if (bytesRead != fileLength) {
        throw new IOException("File read error");
      }
      return new String(fileContent, StandardCharsets.UTF_8);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * 主程序.
   */
  public static void main(String[] args) throws IOException {
    // 假设文本文件的路径已经以参数的形式传递给程序
    //        if (args.length < 1) {
    //            System.out.println("Please provide the path to the text file.");
    //            return;
    //        }
    //String filePath = args[0];
    String filePath = "src/main/java/org/example/test1.txt";
    Path path = Paths.get(filePath);

    // 检查文件是否存在
    if (!path.toFile().exists()) {
      System.out.println("File does not exist.");
      return;
    }
    String content = readFileToString(filePath);
    if (content == null) {
      System.out.println("There is no content.");
      return;
    }
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

    // 构建图
    for (int i = 0; i < words.length - 1; i++) {
      String wordA = words[i];
      String wordB = words[i + 1];
      updateGraph(wordA, wordB);
    }
    // 输出处理后的行
    System.out.println(content.trim());

    Scanner scanner = new Scanner(new InputStreamReader(System.in, StandardCharsets.UTF_8));
    String choice;
    do {
      System.out.println("请选择功能：");
      System.out.println("1. 需求2展示生成图片");
      System.out.println("2. 需求3查询桥接词");
      System.out.println("3. 需求4生成新文本");
      System.out.println("4. 需求5最短路径");
      System.out.println("5. 需求5可选：展示全部最短路径");
      System.out.println("6. 需求6随机游走");

      choice = scanner.next();

      switch (choice) {
        case "1" -> showDirectedGraph();
        case "2" -> {
          System.out.print("Enter word1: ");
          String word1 = scanner.next().toLowerCase();
          System.out.print("Enter word2: ");
          String word2 = scanner.next().toLowerCase();
          long startTime = System.nanoTime();
          String result = queryBridgeWords(word1, word2);
          System.out.println(result);
          long endTime = System.nanoTime();
          System.out.println("Execution time: " + (endTime - startTime) + " ns");
        }
        case "3" -> {
          // 清空缓冲区
          scanner.nextLine();
          System.out.print("Enter a new text:");
          String Text = scanner.nextLine().toLowerCase();
          long startTime = System.nanoTime();
          String newText = generateNewText(Text);
          System.out.println(newText);
          long endTime = System.nanoTime();
          System.out.println("Execution time: " + (endTime - startTime) + " ns");
        }
        case "4" -> {
          System.out.print("Enter word3: ");
          String word3 = scanner.next().toLowerCase();
          System.out.print("Enter word4: ");
          String word4 = scanner.next().toLowerCase();
          long startTime = System.nanoTime();
          String shortestPath = calcShortestPath(word3, word4);
          System.out.println(shortestPath);
          long endTime = System.nanoTime();
          System.out.println("Execution time: " + (endTime - startTime) + " ns");
        }
        case "5" -> {
          System.out.print("Enter word5: ");
          String word5 = scanner.next().toLowerCase();
          long startTime = System.nanoTime();
          printAllShortestPaths(word5);
          long endTime = System.nanoTime();
          System.out.println("Execution time: " + (endTime - startTime) + " ns");
        }
        case "6" -> {
          long startTime = System.nanoTime();
          String walkPath = randomWalk();
          System.out.println(walkPath);

          try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream("output.txt"), StandardCharsets.UTF_8))) {
            writer.println(walkPath);
            System.out.println("Content has been written to the file.");
          } catch (IOException e) {
            e.printStackTrace();
          }

          long endTime = System.nanoTime();
          System.out.println("Execution time: " + (endTime - startTime) + " ns");
        }
        case "0" -> System.out.println("退出程序");
        default -> System.out.println("无效的选择，请重新选择");
      }
    } while (!choice.equals("0"));

    scanner.close();

  }


  //    创建wordA和wordB节点，并连接它们生成边
  public static void updateGraph(String wordA, String wordB) {
    //      如果图中不包含节点 wordA，则添加一个新的节点
    graph.putIfAbsent(wordA, new HashMap<>());
    graph.putIfAbsent(wordB, new HashMap<>());
    //        获取A的邻居节点集合和权重
    Map<String, Integer> neighbors = graph.get(wordA);
    //        在neighbors中更新B的权重
    neighbors.put(wordB, neighbors.getOrDefault(wordB, 0) + 1);
  }

  //辅助函数，创建带权有向图
  private static DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> createDirectedWeightedGraph() {
    //创建一个有向图
    DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> jgraphtGraph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
    // 添加顶点
    for (String vertex : graph.keySet()) {
      jgraphtGraph.addVertex(vertex);
    }

    // 添加边和权重
    for (Map.Entry<String, Map<String, Integer>> entry : graph.entrySet()) {
      String source = entry.getKey();
      for (Map.Entry<String, Integer> edge : entry.getValue().entrySet()) {
        String target = edge.getKey();
        DefaultWeightedEdge e = jgraphtGraph.addEdge(source, target);
        if (e != null) { // 避免重复添加边
          Integer a = edge.getValue();
          double weight = a.doubleValue();
          jgraphtGraph.setEdgeWeight(e, weight);
      //      double x = jgraphtGraph.getEdgeWeight(e);
      //      System.out.println(x);
        }
      }
    }
    return jgraphtGraph;
  }

  static void showDirectedGraph() {
    DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> jgraphtGraph = createDirectedWeightedGraph();
    JGraphXAdapter<String, DefaultWeightedEdge> jGraphXAdapter = new JGraphXAdapter<>(jgraphtGraph);
    jGraphXAdapter.setEdgeLabelsMovable(true); // 允许移动标签

    // 添加权重标签
    for (DefaultWeightedEdge edge : jgraphtGraph.edgeSet()) {
      String weightLabel = String.valueOf(jgraphtGraph.getEdgeWeight(edge));
      jGraphXAdapter.getEdgeToCellMap().get(edge).setValue(weightLabel);
    }

    mxIGraphLayout mxIGraphLayout = new mxCircleLayout(jGraphXAdapter);
    mxIGraphLayout.execute(jGraphXAdapter.getDefaultParent());

    BufferedImage bufferedImage =
        mxCellRenderer.createBufferedImage(jGraphXAdapter, null, 3, Color.WHITE, true, null);

    // 创建Swing窗口并添加JGraphX组件
    JFrame frame = new JFrame();
    frame.getContentPane().add(new mxGraphComponent(jGraphXAdapter));
    frame.setTitle("Directed Weighted Graph");
    frame.setPreferredSize(new Dimension(600, 400));
    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    frame.pack();
    frame.setVisible(true);

    File newFIle = new File("graph.png");
    try {
      ImageIO.write(bufferedImage, "PNG", newFIle);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  private static final SecureRandom random = new SecureRandom();

  //查找桥接词集合
  public static Set<String> findBridgeWords(String word1, String word2) {
    Set<String> bridgeWords = new HashSet<>();
    // 遍历 word1 的邻接词
    Map<String, Integer> word1Neighbors = graph.get(word1);
    for (String bridgeWord : word1Neighbors.keySet()) {
      // 如果 word2 也是 bridgeWord 的邻接词，则 bridgeWord 是一个桥接词
      if (graph.containsKey(bridgeWord) && graph.get(bridgeWord).containsKey(word2)) {
        bridgeWords.add(bridgeWord);
      }
    }
    return bridgeWords;
  }

  //    打印桥接词集合
  static String queryBridgeWords(String word1, String word2) {
    // 检查输入的单词是否存在于图中
    if (!graph.containsKey(word1) || !graph.containsKey(word2)) {
      return "No word1 or word2 in the graph!";
    }

    Set<String> bridgeWords = findBridgeWords(word1, word2);

    // 根据桥接词的数量返回相应的结果
    if (bridgeWords.isEmpty()) {
      return "No bridge words from " + word1 + " to " + word2 + "!";
    } else {
      return "The bridge words from " + word1 + " to " + word2 + " are: " + String.join(", ", bridgeWords) + ".";
    }
  }

  //  根据桥接词生成新文本
  static String generateNewText(String inputText) {
    StringBuilder result = new StringBuilder();
    String[] words = inputText.split("\\s+");
    //前后两个词循环找桥接词
    for (int i = 0; i < words.length - 1; i++) {
      String word1 = words[i];
      String word2 = words[i + 1];
      result.append(word1).append(" ");

      if (graph.containsKey(word1) && graph.containsKey(word2)) {
        Set<String> bridgeWords = findBridgeWords(word1, word2);
        if (!bridgeWords.isEmpty()) {
          //转换成列表（为了随机选择）
          List<String> bridgeList = new ArrayList<>(bridgeWords);
          String bridgeWord = bridgeList.get(random.nextInt(bridgeList.size()));
          result.append(bridgeWord).append(" ");
        }
      }
    }
    //追加最后一个单词
    result.append(words[words.length - 1]);
    return result.toString();
  }

  //    用 Dijkstra 算法实现最短路径（局部拓展）
  static String calcShortestPath(String word1, String word2) {
    // 检查图中是否包含 word1 和 word2，如果没有则返回相应的提示信息
    if (!graph.containsKey(word1) || !graph.containsKey(word2)) {
      return "No word1 or word2 in the graph!";
    }

    // 初始化 distance 映射，用于存储从 word1 到每个节点的最短距离
    Map<String, Integer> distance = new HashMap<>();
    // 初始化 previous 映射，用于存储每个节点的前一个节点，以便于路径回溯
    Map<String, String> previous = new HashMap<>();
    // 初始化优先队列，根据节点的距离进行排序
    PriorityQueue<String> queue = new PriorityQueue<>(Comparator.comparingInt(distance::get));

    // 遍历图中的所有节点，初始化每个节点的距离
    for (String word : graph.keySet()) {
      if (word.equals(word1)) {
        distance.put(word, 0); // 起点的距离为 0
      } else {
        distance.put(word, Integer.MAX_VALUE); // 其他节点的距离为正无穷大
      }
      queue.add(word); // 将节点添加到优先队列中
    }

    // 当优先队列不为空时，执行循环
    while (!queue.isEmpty()) {
      // 取出队列中距离最小的节点
      String currentWord = queue.poll();
      // 如果当前节点是目标节点，跳出循环
      if (currentWord.equals(word2)) {
        break;
      }
      // 遍历当前节点的所有邻居节点
      for (Map.Entry<String, Integer> neighbor : graph.get(currentWord).entrySet()) {
        String nextWord = neighbor.getKey(); // 邻居节点的名称
        int weight = neighbor.getValue(); // 邻居节点的边权重
        int newDistance = distance.get(currentWord) + weight; // 计算从起点到邻居节点的新距离
        // 如果新距离小于已知的到邻居节点的距离，更新距离和前驱节点
        if (newDistance < distance.get(nextWord)) {
          distance.put(nextWord, newDistance); // 更新邻居节点的距离
          previous.put(nextWord, currentWord); // 更新邻居节点的前驱节点
          queue.remove(nextWord); // 从队列中移除邻居节点
          queue.add(nextWord); // 重新将邻居节点添加到队列中，以更新其位置
        }
      }
    }

    List<String> path = new ArrayList<>();
    String currentWord = word2;
    while (previous.containsKey(currentWord)) {
      path.add(0, currentWord);
      currentWord = previous.get(currentWord);
    }
    if (!currentWord.equals(word1)) {
      return "No path exists between " + word1 + " and " + word2 + "!"; // 不存在路径
    }
    path.add(0, word1);

    //标注
    DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> jgraphtGraph = createDirectedWeightedGraph();
    // 创建JGraphXAdapter
    JGraphXAdapter<String, DefaultWeightedEdge> jgxAdapter = new JGraphXAdapter<>(jgraphtGraph);
    // 将最短路径上的边加粗
    if (path.size() > 1) {
      for (int i = 0; i < path.size() - 1; i++) {
        String sourceVertex = path.get(i);
        String targetVertex = path.get(i + 1);
        DefaultWeightedEdge edge = jgraphtGraph.getEdge(sourceVertex, targetVertex);
        if (edge != null) {
          jgxAdapter.setCellStyle("strokeColor=red;strokeWidth=3", new Object[]{jgxAdapter.getEdgeToCellMap().get(edge)});
        }
      }
    }
    // 设置布局
    mxIGraphLayout layout = new mxCircleLayout(jgxAdapter);
    layout.execute(jgxAdapter.getDefaultParent());

    // 创建Swing窗口并添加JGraphX组件
    JFrame frame = new JFrame();
    mxGraphComponent graphComponent = new mxGraphComponent(jgxAdapter);
    frame.getContentPane().add(graphComponent);
    frame.setTitle("Directed Weighted Graph with Shortest Path");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(400, 400);
    frame.setVisible(true);

    return "Shortest path between " + word1 + " and " + word2 + ": " + String.join("→", path);
  }

  //    只输入一个单词，则程序计算出该单词到图中其他任一单词的最短路径，并逐项展示出来
  static void printAllShortestPaths(String word1) {
    // 检查图中是否包含 word1，如果没有则返回相应的提示信息
    if (!graph.containsKey(word1)) {
      System.out.println("The word \"" + word1 + "\" is not in the graph!");
      return;
    }

    // 遍历图中的所有节点
    for (String word2 : graph.keySet()) {
      // 如果目标节点与起点相同，跳过
      if (word1.equals(word2)) {
        continue;
      }

      // 计算从 word1 到 word2 的最短路径
      String shortestPath = calcShortestPath(word1, word2);

      // 展示最短路径
      System.out.println(shortestPath);
    }
  }

  //    随机游走
  static String randomWalk() {
    if (graph.isEmpty()) {
      return "Graph is empty";
    }

    Scanner scanner = new Scanner(new InputStreamReader(System.in, StandardCharsets.UTF_8));
    System.out.println("Press Enter to stop the traversal at any time.");

    SecureRandom random = new SecureRandom();
    List<String> vertices = new ArrayList<>(graph.keySet());
    String startVertex = vertices.get(random.nextInt(vertices.size()));

    Set<String> visitedEdges = new HashSet<>();
    List<String> traversal = new ArrayList<>();
    traversal.add(startVertex);

    while (true) {
      // 防止运行过快没有空终止
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }

      // 检查用户是否按下回车键以终止遍历
      try {
        if (System.in.available() > 0 && scanner.nextLine().isEmpty()) {
          System.out.println("Traversal stopped by user.");
          break;
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

      Map<String, Integer> neighbors = graph.get(startVertex);
      if (neighbors == null || neighbors.isEmpty()) {
        break; // 如果没有邻接点则结束遍历
      }

      // 获取所有邻接点
      List<String> allNeighbors = new ArrayList<>(neighbors.keySet());
      if (allNeighbors.isEmpty()) {
        break; // 如果没有邻接点则结束遍历
      }

      // 随机选择一个邻接点
      String nextVertex = allNeighbors.get(random.nextInt(allNeighbors.size()));
      String edge = startVertex + " -> " + nextVertex;

      // 如果该边已被访问，则结束遍历
      if (visitedEdges.contains(edge)) {
        System.out.println("Edge " + edge + " already visited. Stopping traversal.");
        break;
      }

      traversal.add(nextVertex);
      visitedEdges.add(edge); // 记录该边已被访问
      startVertex = nextVertex; // 更新当前顶点

      // 输出当前的遍历路径
      System.out.println(traversal);
    }

    // 将遍历结果输出为字符串
    StringBuilder result = new StringBuilder();
    for (String vertex : traversal) {
      result.append(vertex).append(" ");
    }

    return result.toString().trim(); // 返回遍历路径的字符串表示
  }

}