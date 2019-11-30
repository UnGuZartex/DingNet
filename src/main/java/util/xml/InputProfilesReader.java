package util.xml;

import gui.MainGUI;
import iot.InputProfile;
import iot.QualityOfService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import selfadaptation.adaptationgoals.AdaptationGoal;
import selfadaptation.adaptationgoals.IntervalAdaptationGoal;
import selfadaptation.adaptationgoals.ThresholdAdaptationGoal;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class InputProfilesReader {
    public static List<InputProfile> readInputProfiles() {
        List<InputProfile> inputProfiles = new LinkedList<>();

        try {
            var fileStream = MainGUI.class.getResourceAsStream("/inputProfiles/inputProfile.xml");

            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(fileStream);
            Element inputProfilesElement = doc.getDocumentElement();

            var inputProfilesList = inputProfilesElement.getElementsByTagName("inputProfile");
            for (int i = 0; i < inputProfilesList.getLength(); i++) {
                Element inputProfileElement = (Element) inputProfilesList.item(i);

                String name = XMLHelper.readChild(inputProfileElement, "name");
                int numberOfRuns = Integer.parseInt(XMLHelper.readChild(inputProfileElement, "numberOfRuns"));

                long simulationDuration = Long.parseLong(XMLHelper.readChild(inputProfileElement, "simulationDuration"));
                String timeUnitName = XMLHelper.readChild(inputProfileElement, "timeUnit");
                Optional<ChronoUnit> timeUnit = Arrays.stream(ChronoUnit.values())
                                            .filter(c -> c.toString().equals(timeUnitName))
                                            .findFirst();

                Element QoSElement = (Element) inputProfileElement.getElementsByTagName("QoS").item(0);
                HashMap<String, AdaptationGoal> adaptationGoalHashMap = new HashMap<>();
                var adaptationGoals = QoSElement.getElementsByTagName("adaptationGoal");

                for (int j = 0; j < adaptationGoals.getLength(); j++) {
                    Element adaptationGoalElement = (Element) adaptationGoals.item(j);

                    String goalName = XMLHelper.readChild(adaptationGoalElement, "name");
                    String goalType = adaptationGoalElement.getAttribute("type");
                    AdaptationGoal adaptationGoal;

                    if (goalType.equals("interval")) {
                        double upperValue = Double.parseDouble(XMLHelper.readChild(adaptationGoalElement, "upperValue"));
                        double lowerValue = Double.parseDouble(XMLHelper.readChild(adaptationGoalElement, "lowerValue"));
                        adaptationGoal = new IntervalAdaptationGoal(lowerValue, upperValue);
                    } else if (goalType.equals("threshold")) {
                        double threshold = Double.parseDouble(XMLHelper.readChild(adaptationGoalElement, "threshold"));
                        adaptationGoal = new ThresholdAdaptationGoal(threshold);
                    } else {
                        throw new RuntimeException(String.format("Unsupported type of adaptation goal: %s", goalType));
                    }
                    adaptationGoalHashMap.put(goalName, adaptationGoal);
                }


                Map<Integer, Double> moteProbabilities = new HashMap<>();
                var moteProbabilitiesList = inputProfileElement.getElementsByTagName("mote");

                for (int j = 0; j < moteProbabilitiesList.getLength(); j++) {
                    Element moteElement = (Element) moteProbabilitiesList.item(j);

                    int moteNumber = Integer.parseInt(XMLHelper.readChild(moteElement, "moteNumber"));
                    double activationProbability = Double.parseDouble(XMLHelper.readChild(moteElement, "activityProbability"));

                    moteProbabilities.put(moteNumber - 1, activationProbability);
                }

                InputProfile ip = timeUnit.map(chronoUnit ->
                    new InputProfile(
                        name,
                        new QualityOfService(adaptationGoalHashMap),
                        numberOfRuns,
                        moteProbabilities,
                        new HashMap<>(),
                        new HashMap<>(),
                        inputProfileElement,
                        simulationDuration,
                        chronoUnit))
                    .orElseGet(() -> new InputProfile(
                        name,
                        new QualityOfService(adaptationGoalHashMap),
                        numberOfRuns,
                        moteProbabilities,
                        new HashMap<>(),
                        new HashMap<>(),
                        inputProfileElement));
                inputProfiles.add(ip);
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }

        return inputProfiles;
    }
}
