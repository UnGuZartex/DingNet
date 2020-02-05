package EnvironmentAPI.GeneralSensor.FunctionSensor;

import EnvironmentAPI.GeneralSensor.Sensor;
import datagenerator.iaqsensor.TimeUnit;
import org.jxmapviewer.viewer.GeoPosition;
import parsii.eval.Expression;
import parsii.eval.Parser;
import parsii.eval.Scope;
import parsii.eval.Variable;
import parsii.tokenizer.ParseException;



public class FunctionSensor extends Sensor {
    private TimeUnit timeUnit;
    private Expression function;
    private Variable t;
    private String functionString;

    public FunctionSensor(GeoPosition position, String function, double maxValue, TimeUnit unit) {
        super(position, maxValue);
        this.functionString = function;
        timeUnit = unit;
        Scope scope = new Scope();
        this.t = scope.getVariable("t");
        try {
            this.function = Parser.parse(function, scope);
        }
        catch (ParseException e){
            System.out.println("There was an error: " + e.getMessage());
        }

    }

    @Override
    public double generateData(long timeinNano) {
        double timeToEvaluate = timeUnit.convertFromNano(timeinNano);
        t.setValue(timeToEvaluate);
        double value = function.evaluate();
        if (value > maxValue) {
            value = maxValue;
        }
        if (value < 0){
            value = 0;
        }

        return value;
    }

    @Override
    public String getType() {
        return "FunctionSensor";
    }

    @Override
    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    @Override
    public Object getDefiningFeatures() {
        return functionString;
    }
}
