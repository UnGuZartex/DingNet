package iot;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import selfadaptation.adaptationgoals.AdaptationGoal;
import selfadaptation.adaptationgoals.IntervalAdaptationGoal;
import selfadaptation.adaptationgoals.ThresholdAdaptationGoal;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;

/**
 * A class representing an input profile for the simulator.
 */
public class InputProfile {

    /**
     * The default duration of the simulation.
     */
    private static final long DEFAULT_SIMULATION_DURATION = 2;
    /**
     * The default unit of measure of the duration.
     */
    private static final ChronoUnit DEFAULT_TIME_UNIT = ChronoUnit.HOURS;

    /**
     * The name of the input profile.
     */
    private String name;
    /**
     * A quality of service profile for this inputProfile
     */
    private QualityOfService qualityOfServiceProfile;

    /**
     * The number of runs in this inputProfile
     */
    private int numberOfRuns;

    /**
     * The duration of the simulation
     */
    private long simulationDuration;

    /**
     * The unit of measure of the duration.
     * We use a class instead a interface for deserialize problem.
     */
    private ChronoUnit timeUnit;

    /**
     * The probabilities for the motes to run a certain path.
     */
    private Map<Integer,Double> probabilitiesForMotes;

    /**
     * The probabilities for the gateways to work.
     */
    private Map<Integer,Double> probabilitiesForGateways;
    /**
     * Other probabilities chosen for the simulation
     */
    private Map<Integer,Double> regionProbabilities;
    /**
     * The source Document of the profile.
     */
    private Document xmlSource;

    /**
     * Generates InputProfile with a given qualityOfServiceProfile, numberOfRuns, probabilitiesForMotes, probabilitiesForGateways,
     * regionProbabilities, xmlSource and gui.
     * @param qualityOfServiceProfile The quality of service profile.
     * @param numberOfRuns The number of runs.
     * @param probabilitiesForMotes The probabilities for the motes.
     * @param probabilitiesForGateways The probabilities for the gateways.
     * @param regionProbabilities The probabilities for the regions.
     * @param xmlSource The source of the InputProfile.
     */
    public InputProfile(String name, QualityOfService qualityOfServiceProfile,
                        int numberOfRuns,
                        Map<Integer, Double> probabilitiesForMotes,
                        Map<Integer, Double> probabilitiesForGateways, Map<Integer, Double> regionProbabilities,
                        Element xmlSource) {
        this(name, qualityOfServiceProfile, numberOfRuns, probabilitiesForMotes, probabilitiesForGateways,
            regionProbabilities, xmlSource, DEFAULT_SIMULATION_DURATION, DEFAULT_TIME_UNIT);
    }


    /**
     * Generates InputProfile with a given qualityOfServiceProfile, numberOfRuns, probabilitiesForMotes, probabilitiesForGateways,
     * regionProbabilities, xmlSource and gui.
     * @param qualityOfServiceProfile The quality of service profile.
     * @param numberOfRuns The number of runs.
     * @param probabilitiesForMotes The probabilities for the motes.
     * @param probabilitiesForGateways The probabilities for the gateways.
     * @param regionProbabilities The probabilities for the regions.
     * @param xmlSource The source of the InputProfile.
     * @param simulationDuration The duration of the simulation.
     * @param timeUnit The unit of measure of the duration.
     */
    public InputProfile(String name, QualityOfService qualityOfServiceProfile,
                        int numberOfRuns,
                        Map<Integer, Double> probabilitiesForMotes,
                        Map<Integer, Double> probabilitiesForGateways, Map<Integer, Double> regionProbabilities,
                        Element xmlSource, long simulationDuration, ChronoUnit timeUnit) {
        this.name = name;
        this.qualityOfServiceProfile = qualityOfServiceProfile;
        this.numberOfRuns =numberOfRuns;
        this.probabilitiesForMotes = probabilitiesForMotes;
        this.regionProbabilities = regionProbabilities;
        this.probabilitiesForGateways = probabilitiesForGateways;
        Node node = xmlSource;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        Document newDocument = builder.newDocument();
        Node importedNode = newDocument.importNode(node, true);
        newDocument.appendChild(importedNode);
        this.xmlSource = newDocument;
        this.simulationDuration = simulationDuration;
        this.timeUnit = timeUnit;
    }

    /**
     * Returns the quality of service profile.
     * @returnthe The quality of service profile.
     */
    public QualityOfService getQualityOfServiceProfile() {
        return qualityOfServiceProfile;
    }

    /**
     * Sets the quality of service profile.
     * @param qualityOfServiceProfile The quality of service profile to set.
     */
    public void setQualityOfServiceProfile(QualityOfService qualityOfServiceProfile) {
        this.qualityOfServiceProfile = qualityOfServiceProfile;
        updateFile();
    }

    /**
     * Returns the number of runs.
     * @return The number of runs.
     */
    public int getNumberOfRuns() {
        return numberOfRuns;
    }

    /**
     * Sets the number of runs.
     * @param numberOfRuns The number of runs to set.
     */
    public void setNumberOfRuns(int numberOfRuns) {
        this.numberOfRuns = numberOfRuns;
        updateFile();
    }

    /**
     * Returns the probability for a given mote number.
     * @param moteNumber The number of the mote.
     * @return The probability for the mote.
     */
    public double getProbabilityForMote(int moteNumber) {
        if (probabilitiesForMotes.get(moteNumber) != null)
            return probabilitiesForMotes.get(moteNumber);
        else{
            return 0.0;
        }
    }

    /**
     * Returns he numbers of the motes where there are probabilities for.
     * @return The numbers of the motes where there are probabilities for.
     */
    public Set<Integer> getProbabilitiesForMotesKeys() {
        return probabilitiesForMotes.keySet();
    }

    /**
     * Puts a given probability with a given moteNumber in the map.
     * @param moteNumber The number of the mote.
     * @param probability The probability of the mote.
     */
    public void putProbabilityForMote(int moteNumber, double probability) {
        this.probabilitiesForMotes.put(moteNumber,probability);
        updateFile();
    }

    /**
     * Returns the probability for a given gateway number.
     * @param gatewayNumber The number of the gateway.
     * @return The probability for the gateway.
     */
    public double getProbabilityForGateway(int gatewayNumber) {
        if (probabilitiesForGateways.get(gatewayNumber) != null)
            return probabilitiesForGateways.get(gatewayNumber);
        else{
            return 0.0;
        }
    }

    /**
     * Returns he numbers of the gateways where there are probabilities for.
     * @return The numbers of the gateways where there are probabilities for.
     */
    public Set<Integer> getProbabilitiesForGatewayKeys() {
        return probabilitiesForGateways.keySet();
    }

    /**
     * Puts a given probability with a given gateway number in the map.
     * @param gatewayNumber The number of the gateway.
     * @param probability The probability of the gateway.
     */
    public void putProbabilitiyForGateway(int gatewayNumber, double probability) {
        this.probabilitiesForGateways.put(gatewayNumber,probability);
        updateFile();
    }

    /**
     * Returns the probability for a given region number.
     * @param regionNumber The number of the region.
     * @return The probability for the region.
     */
    public double getRegionProbability(int regionNumber) {

        if (regionProbabilities.get(regionNumber) != null)
            return regionProbabilities.get(regionNumber);
        else{
            return 0.0;
        }
    }

    /**
     * Returns he numbers of the regions where there are probabilities for.
     * @return The numbers of the regions where there are probabilities for.
     */
    public Set<Integer> getRegionProbabilitiesKeys() {
        return regionProbabilities.keySet();
    }

    /**
     * Puts a given probability with a given region number in the map.
     * @param regionNumber The number of the region.
     * @param probability The probability of the region.
     */
    public void putProbabilitiyForRegion(int regionNumber, double probability) {
        this.regionProbabilities.put(regionNumber,probability);
        updateFile();
    }

    /**
     * returns the xml source.
     * @return The xml source.
     */
    public Document getXmlSource() {
        return xmlSource;
    }

    /**
     * Returns the name of the InputProfile.
     * @return the name of the InputProfile.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the duration of the simulation
     * @return the duration of the simulation
     */
    public long getSimulationDuration() {
        return simulationDuration;
    }

    /**
     * Returns the unit of measure of the duration.
     * @return the unit of measure of the duration
     */
    public ChronoUnit getTimeUnit() {
        return timeUnit;
    }

    public InputProfile setSimulationDuration(long simulationDuration) {
        this.simulationDuration = simulationDuration;
        updateFile();
        return this;
    }

    public InputProfile setTimeUnit(ChronoUnit timeUnit) {
        this.timeUnit = timeUnit;
        updateFile();
        return this;
    }

    /**
     * A function which updates the source file.
     */
    private void updateFile() {
        Document doc = getXmlSource();
        for (int i =0 ; i<doc.getChildNodes().getLength();) {
            doc.removeChild(doc.getChildNodes().item(0));
        }
        Element inputProfileElement = doc.createElement("inputProfile");
        doc.appendChild(inputProfileElement);

        Element name = doc.createElement("name");
        name.appendChild(doc.createTextNode(getName()));
        inputProfileElement.appendChild(name);

        Element numberOfRuns = doc.createElement("numberOfRuns");
        numberOfRuns.appendChild(doc.createTextNode(Double.toString(getNumberOfRuns())));
        inputProfileElement.appendChild(numberOfRuns);

        Element simulationDuration = doc.createElement("simulationDuration");
        simulationDuration.appendChild(doc.createTextNode(""+getSimulationDuration()));
        inputProfileElement.appendChild(simulationDuration);

        Element timeUnit = doc.createElement("timeUnit");
        timeUnit.appendChild(doc.createTextNode(getTimeUnit().toString()));
        inputProfileElement.appendChild(timeUnit);

        Element QoSElement = doc.createElement("QoS");
        QualityOfService QoS = getQualityOfServiceProfile();

        for (String goalName : getQualityOfServiceProfile().getNames()) {
            Element adaptationGoalElement = doc.createElement("adaptationGoal");

            Element goalNameElement = doc.createElement("name");
            goalNameElement.appendChild(doc.createTextNode(goalName));
            adaptationGoalElement.appendChild(goalNameElement);

            if (getQualityOfServiceProfile().getAdaptationGoal(goalName).getClass() == IntervalAdaptationGoal.class) {
                adaptationGoalElement.setAttribute("type", "interval");

                Element upperValue = doc.createElement("upperValue");
                double upperBoundary = ((IntervalAdaptationGoal) QoS.getAdaptationGoal(goalName)).getUpperBoundary();
                upperValue.appendChild(doc.createTextNode(Double.toString(upperBoundary)));
                adaptationGoalElement.appendChild(upperValue);

                Element lowerValue = doc.createElement("lowerValue");
                double lowerBoundary = ((IntervalAdaptationGoal) QoS.getAdaptationGoal(goalName)).getLowerBoundary();
                lowerValue.appendChild(doc.createTextNode(Double.toString(lowerBoundary)));
                adaptationGoalElement.appendChild(lowerValue);
            }

            if (getQualityOfServiceProfile().getAdaptationGoal(goalName).getClass() == ThresholdAdaptationGoal.class) {
                adaptationGoalElement.setAttribute("type", "threshold");
                Element thresholdElement = doc.createElement("threshold");
                double threshold = ((ThresholdAdaptationGoal) getQualityOfServiceProfile().getAdaptationGoal(goalName)).getThreshold();
                thresholdElement.appendChild(doc.createTextNode(Double.toString(threshold)));
                adaptationGoalElement.appendChild(thresholdElement);
            }
            QoSElement.appendChild(adaptationGoalElement);
        }


        inputProfileElement.appendChild(QoSElement);

        Element moteProbabilities = doc.createElement("moteProbabilaties");
        for (int moteNumber : getProbabilitiesForMotesKeys()) {
            Element moteElement = doc.createElement("mote");

            Element moteNumberElement = doc.createElement("moteNumber");
            moteNumberElement.appendChild(doc.createTextNode(Integer.toString(moteNumber+1)));
            moteElement.appendChild(moteNumberElement);

            Element activityProbability = doc.createElement("activityProbability");
            activityProbability.appendChild(doc.createTextNode(Double.toString(getProbabilityForMote(moteNumber))));
            moteElement.appendChild(activityProbability);

            moteProbabilities.appendChild(moteElement);
        }
        inputProfileElement.appendChild(moteProbabilities);

        SimulationRunner.getInstance().updateInputProfilesFile();
    }

    /**
     * Puts the adaptation goal for reliable communications with the name in the map of the QualityOfServiceProfile and updates the file.
     * @param name The name of the adaptationGoal.
     * @param adaptationGoal The AdaptationGoal to put in the map.
     */
    public void putAdaptationGoal(String name,AdaptationGoal adaptationGoal) {
        this.getQualityOfServiceProfile().putAdaptationGoal(name,adaptationGoal);
        updateFile();
    }
}
