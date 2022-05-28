package com.example.grafjava;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.InputMismatchException;

public class Controller {

    @FXML
    private Label messages;
    @FXML
    private Label maxw;
    @FXML
    private TextField columnsNum;
    @FXML
    private TextField rowsNum;
    @FXML
    private TextField minWeight;
    @FXML
    private TextField maxWeight;
    @FXML
    private Label pathToFile;
    @FXML
    private AnchorPane graphPane;
    @FXML
    private Slider cohesionSlider;

    GridPane nodes;
    Graph graph;
    Node[] buttons;

    HashMap<GraphEdge, Edge> pickEdge;

    // Roboczo 0 - nic, 1 - dijsktra, 2 - BFS
    private int algorithm;
    private double min = 0,  max = 1;


    public void gen(ActionEvent e){
        int c = 6, w = 10;
        double cohesionLevel = cohesionSlider.getValue();
        try {
            c = Integer.parseInt(columnsNum.getText());
            w = Integer.parseInt(rowsNum.getText());
            if (c <= 0 || w <= 0) {
                throw new NumberFormatException();
            }
        }catch(NumberFormatException er){
            messages.setText("Nieprawidłowe wymiary");
            return;
        }
        try{
            min = Double.parseDouble(minWeight.getText());
            max = Double.parseDouble(maxWeight.getText());
            if (min < 0 || max < 0) {
                throw new NumberFormatException();
            }
        }catch(NumberFormatException er){
            messages.setText("Nieprawidłowe wagi");
            return;
        }
        //wywoluje generacje graf
        pickEdge = new HashMap<>();
        graph = new Graph(w, c);
        Generation.generate(graph, min, max, cohesionLevel);
        graph.printGraph();
        showGraph(w, c);
    }

    public void bfs(ActionEvent e){
        //wywoluje bfs
        BFS bfs = new BFS();
        messages.setText("Uruchamiam BFS");
        if(bfs.BFS(graph, 1, buttons)){
            messages.setText("Graf jest spójny");
        }
        else{
            messages.setText("Graf jest niespójny");
        }
    }

    public void dijsktra(ActionEvent e){
        //wywoluje dijkstre
        messages.setText("Wybierz wierzchołek początkowy");
        algorithm = 1;
        clearGraph();
    }

    public void save(ActionEvent e){
        FileChooser fileChooser = new FileChooser();
        Files f = new Files();
        try {
            File selectedFile = fileChooser.showOpenDialog(null);
            f.save(graph, selectedFile.getPath());
        }catch(IOException er){
            messages.setText("Wybierz plik na zapis");
        }
        messages.setText("Zapisuje graf");
    }

    public void select(ActionEvent e){
        //wywoluje wybiweranie
        pickEdge = new HashMap<>();
        FileChooser fileChooser = new FileChooser();
        Files f = new Files();
        try {
            File selectedFile = fileChooser.showOpenDialog(null);
            pathToFile.setText(selectedFile.getPath());
            graph = f.read(selectedFile.getPath());
            graph.printGraph();
            showGraph(graph.rows, graph.cols);
            messages.setText("");
        }catch(InputMismatchException er){
            messages.setText("Nieprawidłowy format grafu");
        }catch(FileNotFoundException er){
            messages.setText("Wybierz plik z grafem");
        }

    }

    public void showGraph(int rows, int cols) {
        GraphMenager gm = new GraphMenager();
        graphPane.getChildren().remove(nodes);
        nodes = new GridPane();
        nodes.setMaxSize(560, 560);

        double buttonSize;

        if (cols > rows) {
            buttonSize = graphPane.getMaxWidth() / (2 * cols - 1);
        }
        else {
            buttonSize = graphPane.getMaxHeight() / (2 * rows - 1);
        }

        double edgeWidth = buttonSize / 10;
        int index = 0;

        buttons = new Node[rows * cols];

        for (int i = 0; i < rows * 2 - 1; i += 2) {
            for (int j = 0; j < cols * 2 - 1; j += 2) {
                buttons[index] = new Node(buttonSize, index);
                buttons[index].setOnAction(this::buttonAction);
                buttons[index].setId("node");
                nodes.add(buttons[index], j, i);
                for (GraphEdge edge: graph.neighbours[index]) {
                    if (edge.node == index + 1 && cols != 1) {
                        pickEdge.put(edge, new Edge(buttonSize / 2, edgeWidth / 2));
                        for (GraphEdge innerEdge: graph.neighbours[index + 1]) {
                            if (innerEdge.node == index) {
                                pickEdge.put(innerEdge, pickEdge.get(edge));
                            }
                        }
                        pickEdge.get(edge).setId("edge");
                        nodes.add(pickEdge.get(edge), j + 1, i);
                        GridPane.setValignment(pickEdge.get(edge), VPos.CENTER);
                        gm.setColor(pickEdge.get(edge),edge.wage,max,min);
                    }
                    else if (edge.node == index + cols) {
                        pickEdge.put(edge, new Edge(edgeWidth / 2, buttonSize / 2));
                        for (GraphEdge innerEdge: graph.neighbours[index + cols]) {
                            if (innerEdge.node == index) {
                                pickEdge.put(innerEdge, pickEdge.get(edge));
                            }
                        }
                        gm.setColor(pickEdge.get(edge),edge.wage,max,min);
                        pickEdge.get(edge).setId("edge");
                        nodes.add(pickEdge.get(edge), j, i + 1);
                        GridPane.setHalignment(pickEdge.get(edge), HPos.CENTER);
                    }
                }
                index++;
                if (j == 0) {
                    nodes.getColumnConstraints().add(new ColumnConstraints(buttonSize));
                    nodes.getColumnConstraints().add(new ColumnConstraints(buttonSize / 2));
                }
            }
            nodes.getRowConstraints().add(new RowConstraints(buttonSize));
            nodes.getRowConstraints().add(new RowConstraints(buttonSize / 2));
        }
        graphPane.getChildren().addAll(nodes);
    }

    private void clearGraph() {

        for (Node node: buttons) {
            node.setId("Node");
            node.setStyle("");
        }
    }

    private void buttonAction(ActionEvent e) {
        // Wypisuje się każde połączenie z wierzchołka na który kliknęliśmy na konsolę
        for (GraphEdge edge: graph.neighbours[((Node)e.getSource()).number]) {
            System.out.print(edge.node + ": " + edge.wage + " ");
        }
        System.out.println();

        if (algorithm == 1) {
            algorithm = 2;
            graph.chosen = ((Node)e.getSource()).number;
            ((Node)e.getSource()).setId("chosen");
            Dijkstra.dijkstra(graph);
            double maximum =  Dijkstra.colorDistance(graph,buttons);
            maxw.setText(Double.toString((int)(Math.ceil(maximum))));
            messages.setText("Wybierz kolejny wierzchołek");
            return;
        }

        if (algorithm == 2) {
            try {
                Node currentNode = (Node) e.getSource();
                double currentDistance = Dijkstra.showPath(graph, currentNode.number, buttons, pickEdge);
                messages.setText("Droga z wierzchołka " + currentNode.number + " do " + graph.chosen + ": " + currentDistance);
            }
            catch(ArrayIndexOutOfBoundsException exception) {
                messages.setText("Brak połączenia pomiędzy wierzchołkami!");
            }
        }
    }

}