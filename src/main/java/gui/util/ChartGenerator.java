package gui.util;

import gui.mapviewer.SensorDataPainter;
import iot.Environment;
import iot.SimulationRunner;
import iot.lora.LoraTransmission;
import iot.networkentity.Gateway;
import iot.networkentity.Mote;
import iot.networkentity.MoteSensor;
import iot.networkentity.NetworkEntity;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.input.CenterMapListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactory;
import util.Pair;
import util.Statistics;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class ChartGenerator {

    // region HISTORY GRAPHS

    /**
     * Generates a received power graph for a specific mote for a specific run, the amount of packets sent and the amount lost.
     * NOTE: this also updates the fields {@code packetsSent} and {@code packetsLost} to the corresponding values of that mote.
     *
     * @param mote The mote to generate the graph of.
     * @param run  The run to generate the graph of
     * @return A Pair containing ChartPanel containing a received power graph and another pair containing 2 integers: the amount of packets sent and the amount lost.
     */
    public static ChartPanel generateReceivedPowerGraphForMotes(Mote mote, int run, Environment environment) {
        LinkedList<List<Pair<NetworkEntity, Pair<Integer, Double>>>> transmissionsMote = new LinkedList<>();

        Statistics statistics = Statistics.getInstance();

        for (Gateway gateway : environment.getGateways()) {
            transmissionsMote.add(new LinkedList<>());
            for (LoraTransmission transmission : statistics.getAllReceivedTransmissions(gateway.getEUI(), run)) {
                if (transmission.getSender() == mote.getEUI()) {
                    if (!transmission.isCollided())
                        transmissionsMote.getLast().add(
                            new Pair<>(environment.getNetworkEntityById(transmission.getReceiver()),
                                new Pair<>(transmission.getDepartureTime().toSecondOfDay(), transmission.getTransmissionPower())));
                    else {
                        transmissionsMote.getLast().add(
                            new Pair<>(environment.getNetworkEntityById(transmission.getReceiver()),
                                new Pair<>(transmission.getDepartureTime().toSecondOfDay(), (double) 20)));
                    }
                }
            }
            if (transmissionsMote.getLast().isEmpty()) {
                transmissionsMote.remove(transmissionsMote.size() - 1);
            }
        }
        XYSeriesCollection dataReceivedPowerMote = new XYSeriesCollection();

        for (List<Pair<NetworkEntity, Pair<Integer, Double>>> list : transmissionsMote) {
            NetworkEntity receiver = list.get(0).getLeft();

            //noinspection SuspiciousMethodCalls Here we know for certain that the receiver is a gateway (packets are only sent to gateways)
            XYSeries series = new XYSeries("gateway " + (environment.getGateways().indexOf(receiver) + 1));

            for (Pair<NetworkEntity, Pair<Integer, Double>> data : list) {
                series.add(data.getRight().getLeft(), data.getRight().getRight());
            }
            dataReceivedPowerMote.addSeries(series);
        }
        JFreeChart receivedPowerChartMote = ChartFactory.createScatterPlot(
            null, // chart title
            "Seconds", // x axis label
            "Received signal strength in dBm", // y axis label
            dataReceivedPowerMote, // data
            PlotOrientation.VERTICAL,
            true, // include legend
            true, // tooltips
            false // urls
        );
        Shape shape = new Ellipse2D.Double(0, 0, 3, 3);
        XYPlot plot = (XYPlot) receivedPowerChartMote.getPlot();
        XYItemRenderer renderer = plot.getRenderer();
        for (int i = 0; i < dataReceivedPowerMote.getSeriesCount(); i++) {
            renderer.setSeriesShape(i, shape);
        }
        return new ChartPanel(receivedPowerChartMote);
    }


    /**
     * Generates a spreading factor graph for a specific mote for a specific run.
     *
     * @param mote The mote to generate the graph of.
     * @param run  The run to generate the graph of
     * @return A ChartPanel containing a spreading factor graph.
     */
    public static ChartPanel generateSpreadingFactorGraph(NetworkEntity mote, int run) {
        XYSeriesCollection dataSpreadingFactorMote = new XYSeriesCollection();
        XYSeries seriesSpreadingFactorMote = new XYSeries("Spreading factor");
        Statistics statistics = Statistics.getInstance();

        int i = 0;
        for (int spreadingFactor : statistics.getSpreadingFactorHistory(mote.getEUI(), run)) {
            seriesSpreadingFactorMote.add(i, spreadingFactor);
            i++;
        }
        dataSpreadingFactorMote.addSeries(seriesSpreadingFactorMote);

        JFreeChart spreadingFactorChartMote = ChartFactory.createScatterPlot(
            null, // chart title
            "Transmissions", // x axis label
            "Spreading factor", // y axis label
            dataSpreadingFactorMote, // data
            PlotOrientation.VERTICAL,
            true, // include legend
            true, // tooltips
            false // urls
        );
        Shape shape = new Ellipse2D.Double(0, 0, 3, 3);
        XYPlot plot = (XYPlot) spreadingFactorChartMote.getPlot();
        NumberAxis range = (NumberAxis) plot.getRangeAxis();
        range.setRange(0.0, 15.0);
        range.setTickUnit(new NumberTickUnit(1.0));
        XYItemRenderer renderer = plot.getRenderer();

        XYLineAndShapeRenderer LineRenderer = new XYLineAndShapeRenderer();
        for (int series = 0; series < plot.getSeriesCount(); series++) {
            LineRenderer.setSeriesPaint(series, renderer.getSeriesPaint(series));
            LineRenderer.setSeriesStroke(series, new BasicStroke(1.0f));
            LineRenderer.setSeriesShape(series, shape);
        }
        plot.setRenderer(LineRenderer);

        return new ChartPanel(spreadingFactorChartMote);
    }


    /**
     * Generates a used energy graph and the total used energy for a specific mote for a specific run.
     * NOTE: this also updates the field {@code usedEnergy} to the corresponding value of that mote.
     *
     * @param mote The mote to generate the graph of.
     * @param run  The run to generate the graph of
     * @return A Pair withChartPanel containing a used energy graph and a double the total ued energy.
     */
    public static ChartPanel generateUsedEnergyGraph(NetworkEntity mote, int run) {
        XYSeriesCollection dataUsedEnergyEntity = new XYSeriesCollection();
        int i = 0;
        Statistics statistics = Statistics.getInstance();

        XYSeries seriesUsedEnergyEntity = new XYSeries("Used energy");
        for (double usedEnergy : statistics.getUsedEnergy(mote.getEUI(), run)) {
            seriesUsedEnergyEntity.add(i, usedEnergy);
            i = i + 1;
        }
        dataUsedEnergyEntity.addSeries(seriesUsedEnergyEntity);
        JFreeChart usedEnergyChartEntity = ChartFactory.createXYLineChart(
            null, // chart title
            "Transmissions", // x axis label
            "Used energy in mJoule", // y axis label
            dataUsedEnergyEntity, // data
            PlotOrientation.VERTICAL,
            true, // include legend
            true, // tooltips
            false // urls
        );
        Shape shape = new Ellipse2D.Double(0, 0, 3, 3);
        XYPlot plot = (XYPlot) usedEnergyChartEntity.getPlot();
        XYItemRenderer renderer = plot.getRenderer();

        XYLineAndShapeRenderer LineRenderer = new XYLineAndShapeRenderer();
        for (int series = 0; series < plot.getSeriesCount(); series++) {
            LineRenderer.setSeriesPaint(series, renderer.getSeriesPaint(series));
            LineRenderer.setSeriesStroke(series, new BasicStroke(1.0f));
            LineRenderer.setSeriesShape(series, shape);
        }
        plot.setRenderer(LineRenderer);

        return new ChartPanel(usedEnergyChartEntity);
    }


    /**
     * Generates a distance to gateway graph for a specific mote for a specific run.
     *
     * @param mote The mote to generate the graph of.
     * @param run  The run to generate the graph of
     * @return A ChartPanel containing a distance to gateway graph.
     */
    public static ChartPanel generateDistanceToGatewayGraph(Mote mote, int run, Environment environment) {
        LinkedList<LinkedList<LoraTransmission>> transmissionsMote = new LinkedList<>();
        Statistics statistics = Statistics.getInstance();

        for (Gateway gateway : environment.getGateways()) {
            transmissionsMote.add(new LinkedList<>());
            for (LoraTransmission transmission : statistics.getAllReceivedTransmissions(gateway.getEUI(), run)) {
                if (transmission.getSender() == mote.getEUI()) {
                    transmissionsMote.getLast().add(transmission);
                }
            }
            if (transmissionsMote.getLast().isEmpty()) {
                transmissionsMote.remove(transmissionsMote.size() - 1);
            }
        }
        XYSeriesCollection dataDistanceToGateway = new XYSeriesCollection();

        for (LinkedList<LoraTransmission> list : transmissionsMote) {
            NetworkEntity receiver = environment.getNetworkEntityById(list.get(0).getReceiver());

            //noinspection SuspiciousMethodCalls Here we know for certain that the receiver is a gateway (packets are only sent to gateways)
            XYSeries series = new XYSeries("gateway " + (environment.getGateways().indexOf(receiver) + 1));
            int i = 0;
            for (LoraTransmission transmission : list) {
                series.add(i, (Number) Math.sqrt(Math.pow(environment.getNetworkEntityById(transmission.getReceiver()).getYPosInt() - transmission.getYPos(), 2) +
                    Math.pow(environment.getNetworkEntityById(transmission.getReceiver()).getXPosInt() - transmission.getXPos(), 2)));
                i = i + 1;
            }
            dataDistanceToGateway.addSeries(series);
        }

        JFreeChart DistanceToGatewayChartMote = ChartFactory.createXYLineChart(
            null, // chart title
            "Transmissions", // x axis label
            "Distance to the gateway in  m", // y axis label
            dataDistanceToGateway, // data
            PlotOrientation.VERTICAL,
            true, // include legend
            true, // tooltips
            false // urls
        );
        Shape shape = new Ellipse2D.Double(0, 0, 3, 3);
        XYPlot plot = (XYPlot) DistanceToGatewayChartMote.getPlot();
        XYItemRenderer renderer = plot.getRenderer();

        XYLineAndShapeRenderer LineRenderer = new XYLineAndShapeRenderer();
        for (int series = 0; series < plot.getSeriesCount(); series++) {
            LineRenderer.setSeriesPaint(series, renderer.getSeriesPaint(series));
            LineRenderer.setSeriesStroke(series, new BasicStroke(1.0f));
            LineRenderer.setSeriesShape(series, shape);
        }
        plot.setRenderer(LineRenderer);
        return new ChartPanel(DistanceToGatewayChartMote);
    }


    /**
     * Generates a power setting graph for a specific mote for a specific run.
     *
     * @param mote The mote to generate the graph of.
     * @param run  The run to generate the graph of
     * @return A ChartPanel containing a power setting graph.
     */
    public static ChartPanel generatePowerSettingGraph(NetworkEntity mote, int run) {
        XYSeriesCollection dataPowerSettingMote = new XYSeriesCollection();
        XYSeries seriesPowerSettingMote = new XYSeries("Power setting");
        Statistics statistics = Statistics.getInstance();

        for (Pair<Integer, Integer> powerSetting : statistics.getPowerSettingHistory(mote.getEUI(), run)) {
            seriesPowerSettingMote.add(powerSetting.getLeft(), powerSetting.getRight());
        }
        dataPowerSettingMote.addSeries(seriesPowerSettingMote);

        JFreeChart powerSettingChartMote = ChartFactory.createXYLineChart(
            null, // chart title
            "Seconds", // x axis label
            "Power setting in dBm", // y axis label
            dataPowerSettingMote, // data
            PlotOrientation.VERTICAL,
            true, // include legend
            true, // tooltips
            false // urls
        );
        XYPlot plot = (XYPlot) powerSettingChartMote.getPlot();
        NumberAxis range = (NumberAxis) plot.getRangeAxis();
        range.setRange(0.0, 15.0);
        range.setTickUnit(new NumberTickUnit(1.0));
        return new ChartPanel(powerSettingChartMote);
    }

    // endregion


    // region POLLUTION GRAPHS

    public static ChartPanel generateParticulateMatterGraph(Mote mote) {
        return ChartGenerator.generateGraph(mote, MoteSensor.PARTICULATE_MATTER, "Particulate Matter");
    }

    public static ChartPanel generateCarbonDioxideGraph(Mote mote) {
        return ChartGenerator.generateGraph(mote, MoteSensor.CARBON_DIOXIDE, "Carbon Dioxide");
    }

    public static ChartPanel generateSootGraph(Mote mote) {
        return ChartGenerator.generateGraph(mote, MoteSensor.SOOT, "Soot");
    }

    public static ChartPanel generateOzoneGraph(Mote mote) {
        return ChartGenerator.generateGraph(mote, MoteSensor.OZONE, "Ozone");
    }


    public static Pair<JPanel, JComponent> generateParticulateMatterGraph(Environment environment, TileFactory tileFactory, JFrame frame) {
        return ChartGenerator.generateGraph(environment, MoteSensor.PARTICULATE_MATTER, "Particulate Matter", tileFactory, frame);
    }

    public static Pair<JPanel, JComponent> generateCarbonDioxideGraph(Environment environment, TileFactory tileFactory, JFrame frame) {
        return ChartGenerator.generateGraph(environment, MoteSensor.CARBON_DIOXIDE, "Carbon Dioxide", tileFactory, frame);
    }

    public static Pair<JPanel, JComponent> generateSootGraph(Environment environment, TileFactory tileFactory, JFrame frame) {
        return ChartGenerator.generateGraph(environment, MoteSensor.SOOT, "Soot", tileFactory, frame);
    }

    public static Pair<JPanel, JComponent> generateOzoneGraph(Environment environment, TileFactory tileFactory, JFrame frame) {
        return ChartGenerator.generateGraph(environment, MoteSensor.OZONE, "Ozone", tileFactory, frame);
    }



    @SuppressWarnings("SuspiciousNameCombination")
    private static Pair<JPanel, JComponent> generateGraph(Environment environment, MoteSensor moteSensor, String keyName, TileFactory tileFactory, JFrame frame) {
        DefaultXYZDataset data = new DefaultXYZDataset();
        HashMap<Pair<Integer, Integer>, LinkedList<Double>> seriesList = new HashMap<>();
        LinkedList<Pair<GeoPosition, Double>> dataSet = new LinkedList<>();
        Statistics statistics = Statistics.getInstance();

        for (Mote mote : environment.getMotes()) {
            if (mote.getSensors().contains(moteSensor)) {
                for (LoraTransmission transmission : statistics.getSentTransmissions(mote.getEUI(), environment.getNumberOfRuns() - 1)) {
                    int xPos = transmission.getXPos();
                    int yPos = transmission.getYPos();
                    for (Pair<Integer, Integer> key : seriesList.keySet()) {
                        if (Math.sqrt(Math.pow(key.getLeft() - xPos, 2) + Math.pow(key.getRight() - yPos, 2)) < 300) {
                            xPos = key.getLeft();
                            yPos = key.getRight();
                        }
                    }
                    if (seriesList.containsKey(new Pair<>(xPos, yPos))) {
                        seriesList.get(new Pair<>(xPos, yPos)).add(moteSensor.getValue(xPos, yPos));
                    } else {
                        LinkedList<Double> dataList = new LinkedList<>();
                        dataList.add(moteSensor.getValue(xPos, yPos));
                        seriesList.put(new Pair<>(xPos, yPos), dataList);
                    }
                }
            }
        }
        for (Pair<Integer, Integer> key : seriesList.keySet()) {
            double average = seriesList.get(key).stream()
                .mapToDouble(o -> o)
                .average()
                .orElse(0.0);

            seriesList.get(key).clear();
            seriesList.get(key).add(average);
            dataSet.add(new Pair<>(new GeoPosition(environment.getMapHelper().toLatitude(key.getRight()),
                environment.getMapHelper().toLongitude(key.getLeft())), average));
        }

        double[][] seriesParticulateMatter = new double[3][seriesList.size()];
        int i = 0;
        for (Pair<Integer, Integer> key : seriesList.keySet()) {
            seriesParticulateMatter[0][i] = key.getLeft();
            seriesParticulateMatter[1][i] = key.getRight();
            seriesParticulateMatter[2][i] = seriesList.get(key).get(0);
            i++;
        }
        data.addSeries(keyName, seriesParticulateMatter);
        return createHeatChart(dataSet, environment, tileFactory, frame);
    }


    private static Pair<JPanel, JComponent> createHeatChart(List<Pair<GeoPosition, Double>> dataSet, Environment environment, TileFactory tileFactory, JFrame frame) {
        // create a paint-scale and a legend showing it
        SpectrumPaintScale paintScale = new SpectrumPaintScale(80, 100);

        PaintScaleLegend psl = new PaintScaleLegend(paintScale, new NumberAxis());
        psl.setPosition(RectangleEdge.RIGHT);
        psl.setAxisLocation(AxisLocation.TOP_OR_RIGHT);
        psl.setMargin(50.0, 20.0, 80.0, 0.0);

        XYPlot plot = new XYPlot();
        JFreeChart chart = new JFreeChart(null, null, plot, false);
        chart.addSubtitle(psl);
        ImageIcon icon = new ImageIcon(chart.createBufferedImage(400, 300));
        Image image = icon.getImage();
        image = frame.createImage(new FilteredImageSource(image.getSource(),
            new CropImageFilter(350, 40, 50, 220)));
        ImageIcon newIcon = new ImageIcon(image);
        JLabel jLabel = new JLabel(newIcon);
        jLabel.setMinimumSize(new Dimension(0, 0));

        // finally a renderer and a plot
        JXMapViewer mapViewer = new JXMapViewer();
        mapViewer.setTileFactory(tileFactory);
        mapViewer.setAddressLocation(environment.getMapCenter());
        mapViewer.setZoom(5);
        MouseInputListener mia = new PanMouseInputListener(mapViewer);
        mapViewer.addMouseListener(mia);
        mapViewer.addMouseMotionListener(mia);
        mapViewer.addMouseListener(new CenterMapListener(mapViewer));
        mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mapViewer));
        CompoundPainter<JXMapViewer> compoundPainter = new CompoundPainter<>();
        for (Pair<GeoPosition, Double> data : dataSet) {
            compoundPainter.addPainter(new SensorDataPainter(data.getLeft(), paintScale.getPaint(data.getRight())));
        }
        mapViewer.setOverlayPainter(compoundPainter);

        return new Pair<>(mapViewer, jLabel);
    }


    private static ChartPanel generateGraph(Mote mote, MoteSensor moteSensor, String keyName) {
        Environment environment = SimulationRunner.getInstance().getEnvironment();
        Statistics statistics = Statistics.getInstance();

        XYSeriesCollection dataSoot = new XYSeriesCollection();
        int i = 0;
        XYSeries series = new XYSeries(keyName);

        if (mote.getSensors().contains(moteSensor)) {
            for (LoraTransmission transmission : statistics.getSentTransmissions(mote.getEUI(), environment.getNumberOfRuns() - 1)) {
                series.add(i * 10, moteSensor.getValue(transmission.getXPos(), transmission.getYPos()));
                i = i + 1;
            }
            dataSoot.addSeries(series);
        }

        JFreeChart chart = ChartFactory.createScatterPlot(
            null, // chart title
            "Distance traveled in meter", // x axis label
            keyName, // y axis label
            dataSoot, // data
            PlotOrientation.VERTICAL,
            true, // include legend
            true, // tooltips
            false // urls
        );
        Shape shape = new Ellipse2D.Double(0, 0, 3, 3);
        XYPlot plot = (XYPlot) chart.getPlot();
        XYItemRenderer renderer = plot.getRenderer();
        renderer.setSeriesShape(0, shape);
        return new ChartPanel(chart);
    }


    // endregion

}
