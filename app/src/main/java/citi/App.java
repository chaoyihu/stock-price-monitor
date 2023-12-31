package citi;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.CategoryAxis;
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
import java.util.function.Consumer;


public class App extends Application {

    private static final int MAX_DATA_POINTS = 20;
    private int xSeriesData = 0;
    private ConcurrentLinkedQueue<Number> pricels = new ConcurrentLinkedQueue<>();
    private ConcurrentLinkedQueue<String> timestampls = new ConcurrentLinkedQueue<>();
    private XYChart.Series<String, Number> series1 = new XYChart.Series<>();
    private ExecutorService executor;
    final CategoryAxis xAxis = new CategoryAxis();
    final NumberAxis yAxis = new NumberAxis();
    final LineChart<String,Number> lineChart = new LineChart<String,Number>(xAxis,yAxis);
    private static String stockname = "None";
    private String ticker = "None";


    private void init(Stage primaryStage) {

        lineChart.setAnimated(false);
        lineChart.setTitle("Stock price monitor");
        lineChart.setHorizontalGridLinesVisible(true);
        xAxis.setLabel("Timestamp");
        xAxis.setAutoRanging(true);
        yAxis.setLabel("Price");
        yAxis.setAutoRanging(false);

        FetchTicker ft = new FetchTicker();
        System.out.println(stockname);
        ft.fetch(stockname);
        ticker = ft.getResult();
        // Set Name for Series
        series1.setName(stockname + "(" + ticker + ")");
        // Add Chart Series
        lineChart.getData().addAll(series1);

        primaryStage.setScene(new Scene(lineChart));
    }

    private class AddToQueue implements Runnable {

        private String ticker;
        
        public AddToQueue(String tkr) {
            this.ticker = tkr;
        }

        public void run() {
            try {
                // add a data point
                FetchPrice fp = new FetchPrice();
                fp.fetch(this.ticker);
                String[] res = fp.getResult();
                String timestamp = res[0];
                float price = Float.parseFloat(res[1]);
                String unit = res[2];
                timestampls.add(timestamp);
                pricels.add(price);

                Thread.sleep(5000);
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

        AddToQueue addToQueue = new AddToQueue(ticker);
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
	float avg = 0;
	float cnt = 0;

        for (XYChart.Data<String, Number> data : series1.getData()) {
            float price = (float) data.getYValue();
	    avg += price;
	    cnt += 1;
        }
	avg = avg / cnt;

        return new float[] {(float)(avg + avg * 0.0005), (float)(avg - avg * 0.0005), avg};
    }

    private void addDataToSeries() {
        while (!(pricels.isEmpty())) {
            series1.getData().add(new XYChart.Data<>(timestampls.remove(), pricels.remove()));
        }
        // remove points to keep us at no more than MAX_DATA_POINTS
        if (series1.getData().size() > MAX_DATA_POINTS) {
            series1.getData().remove(0, series1.getData().size() - MAX_DATA_POINTS);
        }
        // update
        float[] rangeY = getRangeY();
        yAxis.setUpperBound(rangeY[0]);
        yAxis.setLowerBound(rangeY[1]);
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            stockname = args[0];
        }
        System.out.println("   _    _                                          ");
        System.out.println("  | \\  / |    _       _   #   |      _        _    ");
        System.out.println("  |  \\/  |  /   \\  |/  |  |  -|--  /   \\   | /     ");
        System.out.println("  |      | |     | |   |  |   |   |     |  |       ");
        System.out.println("  |      |  \\ _ /  |   |  |   |/   \\ _ /   |       ");
        System.out.println("                                                   ");
        System.out.println("###################################################");
        System.out.println("Querying" + stockname + "stock price every 5 seconds... Start now!");
        System.out.println("###################################################");
        launch(args);
    }
}
