package org.example;

import com.mxgraph.swing.mxGraphComponent;
import org.jgrapht.graph.*;
import org.jgrapht.ext.JGraphXAdapter;
import javax.imageio.ImageIO;
import javax.swing.*;

import com.mxgraph.layout.*;
import com.mxgraph.util.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

public class TextProcessor {
    public static Map<String, Map<String, Integer>> graph = new HashMap<>();

    public static String readFileToString(String fileName) {
        String encoding = "UTF-8";
        File file = new File(fileName);
        long filelength = file.length();
        byte[] filecontent = new byte[(int) filelength];
        FileInputStream in=null;
        try {
            in = new FileInputStream(file);
            in.read(filecontent);
            return new String(filecontent, encoding);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static <content> void main(String[] args) throws IOException {
        // 假设文本文件的路径已经以参数的形式传递给程序
//        if (args.length < 1) {
//            System.out.println("Please provide the path to the text file.");
//            return;
//        }
//        String filePath = args[0];
        String filePath = "D:\\Study\\软件工程\\Lab1\\src\\main\\java\\org\\example\\test1.txt";
        Path path = Paths.get(filePath);

        // 检查文件是否存在
        if (!path.toFile().exists()) {
            System.out.println("File does not exist.");
            return;
        }
        String content = readFileToString(filePath);
        if (content == null){
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

        Scanner scanner = new Scanner(System.in);
        String choice;
        do {
            System.out.println("请选择功能：");
            System.out.println("1. 需求2展示生成图片");
            System.out.println("2. 需求3查询桥接词");
            System.out.println("3. 需求4生成新文本");
            System.out.println("4. 需求5最短路径");
            System.out.println("5. 需求6随机游走");

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
                    long startTime = System.nanoTime();
                    String walkPath = randomWalk();
                    System.out.println(walkPath);

                    try (PrintWriter writer = new PrintWriter(new FileWriter("output.txt"))) {
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

    //辅助用于给有向图添加元素
    private static void updateGraph(String wordA, String wordB) {
        graph.putIfAbsent(wordA, new HashMap<>());
        graph.putIfAbsent(wordB, new HashMap<>());
        Map<String, Integer> neighbors = graph.get(wordA);
        neighbors.put(wordB, neighbors.getOrDefault(wordB, 0) + 1);
    }

    //辅助函数，创建带权有向图
    private static DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> createDirectedWeightedGraph(){
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
                    double x = jgraphtGraph.getEdgeWeight(e);
                    System.out.println(x);
                }
            }
        }
        return jgraphtGraph;
    }

    static void showDirectedGraph(){
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

    //辅助查找桥接词集合
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

    static String queryBridgeWords(String word1, String word2) {
        // 检查输入的单词是否存在于图中
        if (!graph.containsKey(word1) || !graph.containsKey(word2)) {
            return "No word1 or word2 in the graph!";
        }

        Set<String> bridgeWords;
        bridgeWords = findBridgeWords(word1, word2);

        // 根据桥接词的数量返回相应的结果
        if (bridgeWords.isEmpty()) {
            return "No bridge words from " + word1 + " to " + word2 + "!";
        } else {
            return "The bridge words from " + word1 + " to " + word2 + " are: " + String.join(", ", bridgeWords) + ".";
        }
    }

    static String generateNewText(String inputText) {
        StringBuilder result = new StringBuilder();
        String[] words = inputText.split("\\s+");

        for (int i = 0; i < words.length - 1; i++) {
            String word1 = words[i];
            String word2 = words[i + 1];
            result.append(word1).append(" ");

            if (graph.containsKey(word1) && graph.containsKey(word2)) {
                Set<String> bridgeWords = findBridgeWords(word1, word2);
                if (!bridgeWords.isEmpty()) {
                    List<String> bridgeList = new ArrayList<>(bridgeWords);
                    Random random = new Random();
                    String bridgeWord = bridgeList.get(random.nextInt(bridgeList.size()));
                    result.append(bridgeWord).append(" ");
                }
            }
        }
        //追加最后一个单词
        result.append(words[words.length - 1]);
        return result.toString();
    }

    static String calcShortestPath(String word1, String word2) {
        if (!graph.containsKey(word1) || !graph.containsKey(word2)) {
            return "No word1 or word2 in the graph!";
        }

        Map<String, Integer> distance = new HashMap<>();
        Map<String, String> previous = new HashMap<>();
        PriorityQueue<String> queue = new PriorityQueue<>(Comparator.comparingInt(distance::get));

        for (String word : graph.keySet()) {
            if (word.equals(word1)) {
                distance.put(word, 0);
            } else {
                distance.put(word, Integer.MAX_VALUE);
            }
            queue.add(word);
        }

        while (!queue.isEmpty()) {
            String currentWord = queue.poll();
            if (currentWord.equals(word2)) {
                break;
            }
            for (Map.Entry<String, Integer> neighbor : graph.get(currentWord).entrySet()) {
                String nextWord = neighbor.getKey();
                int weight = neighbor.getValue();
                int newDistance = distance.get(currentWord) + weight;
                if (newDistance < distance.get(nextWord)) {
                    distance.put(nextWord, newDistance);
                    previous.put(nextWord, currentWord);
                    queue.remove(nextWord);
                    queue.add(nextWord);
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

    static String randomWalk(){
        if (graph.isEmpty()) {
            return "Graph is empty";
        }

        Scanner scanner = new Scanner(System.in);
        System.out.println("Press Enter to stop the traversal at any time.");

        Random random = new Random();
        List<String> vertices = new ArrayList<>(graph.keySet());
        String startVertex = vertices.get(random.nextInt(vertices.size()));

        Set<String> visitedEdges = new HashSet<>();
        List<String> traversal = new ArrayList<>();
        traversal.add(startVertex);

        while (true) {
            //防止运行过快没有空终止
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            // 检查用户是否要终止遍历
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

        return result.toString();
    }

}