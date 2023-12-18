package citi;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import javafx.animation.AnimationTimer;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Iterator;
//import java.util.Timer;
//import java.util.TimerTask;
import java.util.function.Consumer;


public class App extends Application {

    private static final int MAX_DATA_POINTS = 50;
    private int xSeriesData = 0;
    private ConcurrentLinkedQueue<Number> pricels = new ConcurrentLinkedQueue<>();
    private XYChart.Series<Number, Number> series1 = new XYChart.Series<>();
    private ExecutorService executor;

    private NumberAxis xAxis;
    private NumberAxis yAxis;

    private void init(Stage primaryStage) {

        xAxis = new NumberAxis(0, MAX_DATA_POINTS, MAX_DATA_POINTS / 10);
        xAxis.setForceZeroInRange(false);
        xAxis.setAutoRanging(false);
        //xAxis.setTickLabelsVisible(false);
        //xAxis.setTickMarkVisible(false);
        //xAxis.setMinorTickVisible(false);

        yAxis = new NumberAxis();
        yAxis.setAutoRanging(false);
    

        // Create a LineChart
        final LineChart<Number, Number> lineChart = new LineChart<Number, Number>(xAxis, yAxis) {
            // Override to remove symbols on each data point
            @Override
            protected void dataItemAdded(Series<Number, Number> series, int itemIndex, Data<Number, Number> item) {
            }
        };

        lineChart.setAnimated(false);
        lineChart.setTitle("DJI stock price monitoring");
        lineChart.setHorizontalGridLinesVisible(true);
        lineChart.getXAxis().setLabel("Timestamp");
        lineChart.getYAxis().setLabel("Price");

        // Set Name for Series
        series1.setName("Price");
        // Add Chart Series
        lineChart.getData().addAll(series1);

        primaryStage.setScene(new Scene(lineChart));
    }

    private class AddToQueue implements Runnable {
        public void run() {
            try {
                // add a data point
                FetchPrice fp = new FetchPrice();
                fp.fetch();
                String raw = fp.getResult();
                System.out.println(raw);
                String s = raw.replace(",", "");
                String[] timeprice = s.split(" price:");
                String timestamp = timeprice[0];
                float price = Float.parseFloat(timeprice[1]);
                pricels.add(price);

                Thread.sleep(10000);
                executor.execute(this);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Stock Price Display");
        init(stage);
        stage.show();

        executor = Executors.newCachedThreadPool(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                return thread;
            }
        });

        AddToQueue addToQueue = new AddToQueue();
        executor.execute(addToQueue);
        //-- Prepare Timeline
        prepareTimeline();
    }

    //-- Timeline gets called in the JavaFX Main thread
    private void prepareTimeline() {
        // Every frame to take any data from queue and add to chart
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                addDataToSeries();
            }
        }.start();
    }

    private float[] getRangeY() {
        float mx = Float.MIN_VALUE;
        float mn = Float.MAX_VALUE;

        for (XYChart.Data<Number, Number> data : series1.getData()) {
            float price = (float) data.getYValue();
            mx = Math.max(mx, price);
            mn = Math.min(mn, price);
        }

        return new float[] {mx, mn};
    }

    private void addDataToSeries() {
        while (!(pricels.isEmpty())) {
            series1.getData().add(new XYChart.Data<>(xSeriesData++, pricels.remove()));
        }
        // remove points to keep us at no more than MAX_DATA_POINTS
        if (series1.getData().size() > MAX_DATA_POINTS) {
            series1.getData().remove(0, series1.getData().size() - MAX_DATA_POINTS);
        }
        // update
        xAxis.setLowerBound(xSeriesData - MAX_DATA_POINTS);
        xAxis.setUpperBound(xSeriesData - 1);
        float[] rangeY = getRangeY();
        yAxis.setLowerBound(rangeY[1] - 10);
        yAxis.setUpperBound(rangeY[0] + 10);
    }

    public static void main(String[] args) {
        System.out.println("Querying Dow Jones Industrial Average stock price every 5 seconds... Start now!");
        launch(args);
    }
}


