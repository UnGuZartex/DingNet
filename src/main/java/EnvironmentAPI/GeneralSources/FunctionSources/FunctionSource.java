package EnvironmentAPI.GeneralSources.FunctionSources;

import EnvironmentAPI.GeneralSources.Source;
import datagenerator.iaqsensor.TimeUnit;
import org.jxmapviewer.viewer.GeoPosition;
import parsii.eval.Expression;
import parsii.eval.Parser;
import parsii.eval.Scope;
import parsii.eval.Variable;
import parsii.tokenizer.ParseException;



public class FunctionSource extends Source {

    private Expression function;
    private Variable t;
    private String functionString;

    public FunctionSource(GeoPosition position, String function, TimeUnit unit) {
        super(position, unit);
        this.functionString = function;
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
    public double generateData(double timeinNano) {
        double timeToEvaluate = timeUnit.convertFromNano(timeinNano);
        t.setValue(timeToEvaluate);
        double value = function.evaluate();
        if (value < 0){
            value = 0;
        }

        return value ;
    }

    @Override
    public String getType() {
        return "FunctionSource";
    }

    @Override
    public Object getDefiningFeatures() {
        return functionString;
    }

    public void setFunction(String function) {
        Scope scope = new Scope();
        this.t = scope.getVariable("t");
        try {
            this.function = Parser.parse(function, scope);
        }
        catch (ParseException e){
            System.out.println("There was an error: " + e.getMessage());
        }
    }
}
