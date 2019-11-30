package gui.util;

import org.jfree.chart.renderer.PaintScale;

import java.awt.*;

public class SpectrumPaintScale implements PaintScale {

    private static final float H1 = 0f;
    private static final float H2 = 1f;
    private final double lowerBound;
    private final double upperBound;

    public SpectrumPaintScale(double lowerBound, double upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    @Override
    public double getLowerBound() {
        return lowerBound;
    }

    @Override
    public double getUpperBound() {
        return upperBound;
    }

    @Override
    public Paint getPaint(double value) {
        float scaledValue = (float) (value / (getUpperBound() - getLowerBound()));
        float scaledH = H1 + scaledValue * (H2 - H1);
        return Color.getHSBColor(scaledH, 1f, 1f);
    }
}

