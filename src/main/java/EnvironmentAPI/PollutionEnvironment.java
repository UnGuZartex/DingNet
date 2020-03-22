package EnvironmentAPI;

import EnvironmentAPI.GeneralSources.Source;
import iot.Environment;
import org.jxmapviewer.viewer.GeoPosition;
import util.MapHelper;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Based on
 *         https://github.com/NathanMacLeod/Fluid-Simulation/blob/master/src/fluidsimulation/FluidSimulation.java
 * Further based on:
 *          https://www.researchgate.net/publication/2560062_Real-Time_Fluid_Dynamics_for_Games
 */
public class PollutionEnvironment {

    private double[] velocX;
    private double[] velocY;
    private double[] density;
    private double[] densityOld;
    private double[] velocXOld;
    private double[] velocYOld;
    private double timeStep;
    private int nTiles;
    private int totalNumberTiles;
    private double diffuseFactor;
    private List<Source> sources;
    private int counter = 0;


    PollutionEnvironment(int nTiles, double diffuseFactor, double timeStep) {
        totalNumberTiles = nTiles * nTiles;
        //Area is a square with n by n tiles, tiles go in order from left to right, top to bottom, starting in top left corner
        velocX = new double[totalNumberTiles];
        velocY = new double[totalNumberTiles];
        density = new double[totalNumberTiles];
        densityOld = new double[totalNumberTiles];
        velocXOld = new double[totalNumberTiles];
        velocYOld = new double[totalNumberTiles];
        this.timeStep = timeStep;
        this.nTiles = nTiles;
        this.diffuseFactor = diffuseFactor;
        sources = new ArrayList<>();
    }


    /**
     * Add a source to the list of sources of this pollution environment
     * @param source the source to add.
     */
    public void addSource(Source source){
        sources.add(source);
    }

    /**
     * Returns the current pollution at this spot.
     * @param position position to search the pollution from
     * @param environment environment where the position is situated in
     * @param MaxValue MaxValue to return
     * @return The pollution if it doesn't exceed the maxvalue
     */
    public double getDensity(GeoPosition position, Environment environment, double MaxValue){
        Point XY = geoPositionToXY(position, environment);
        double thisDensity = density[getTileN(XY.x, XY.y)];
        if (thisDensity > MaxValue) {
            return MaxValue;
        }
        else if (thisDensity < 0) {
            return 0;
        }
        return thisDensity;
    }

    /**
     * Updates the environment every x calls to this function (can be changed if more precision is wanted)
     * @param currentTime Time to add pollution at.
     * @param environment Environment where the pollution is situated
     */
    public void doStep(double currentTime, Environment environment){
        if (counter % 500 == 0) {
            fluidUpdate(timeStep, currentTime, environment);
            counter = 0;
        }
        counter++;
    }

    /**
     * Update the environment following the Navier-Stokes equation. First add all pollution at this
     * time, then let it spread.
     * @param dt    Timesteps to simulate
     * @param currentTime   Time to get pollution value from
     * @param environment   Environment where the pollution is situated
     */
    private void fluidUpdate(double dt, double currentTime, Environment environment) {
        // Add inflow of density
        addSources(currentTime, environment);

        // Diffuse velocities and save result to the old version
        diffuseField(velocX, velocXOld, dt, 1);
        diffuseField(velocY, velocYOld, dt, 2);

        // Flip new and old velocity fields
        velocX = flip(velocXOld, velocXOld = velocX);
        velocY = flip(velocYOld, velocYOld = velocY);

        project();

        advect(velocX, velocXOld, velocXOld, velocYOld, dt, 1);
        advect(velocY, velocYOld, velocXOld, velocYOld, dt, 2);

        project();

        diffuseField(density, densityOld, timeStep, 0);
        advect(densityOld, density, velocX, velocY, dt, 0);
    }


    /**
     * Macro for flipping, hard to do otherwise, based on:
     * https://github.com/NathanMacLeod/Fluid-Simulation/blob/master/src/fluidsimulation/FluidSimulation.java
     */
    private static <T> T flip(T a, @SuppressWarnings("unused") T b) {
        return a;
    }

    /**
     * Given a i and j in matrix form, return the correct index in the array of this environment
     * @param i an index
     * @param j an index
     * @return correct value of index in array
     */
    private int getTileN(int i, int j) {
        if (i >= nTiles || j >= nTiles || i < 0 || j < 0)
            throw new IllegalArgumentException("Tile outside bounds: (" + i + ", " + j + ")");
        return nTiles * j + i;
    }


    /**
     * Simulate the diffusion of pollution coming from a source
     * @param src the source the pollution is coming from
     * @param dst the old sources
     * @param dt timestep to simulate
     * @param b bounds to simulate
     */
    private void diffuseField(double[] src, double[] dst, double dt, int b) {
        double diffuseRate = diffuseFactor * dt;

        for (int k = 0; k < 20; k++) {
            for (int i = 1; i < nTiles - 1; i++) {
                for (int j = 1; j < nTiles - 1; j++) {
                    dst[getTileN(i, j)] = (src[getTileN(i, j)] + diffuseRate * (
                        dst[getTileN(i + 1, j)] + dst[getTileN(i - 1, j)] +
                            dst[getTileN(i, j + 1)] + dst[getTileN(i, j - 1)])) / (1 + 4 * diffuseRate);
                }
            }
            setBounds(b, dst);
        }
    }

    private void advect(double[] src, double[] dst, double[] velX, double[] velY, double dt, int b) {
        for (int ix = 1; ix < nTiles - 1; ix++) {
            for (int iy = 1; iy < nTiles - 1; iy++) {
                //center of tile
                double x = ix - dt * velX[getTileN(ix, iy)];
                double y = iy - dt * velY[getTileN(ix, iy)];

                x = Math.min(Math.max(x, 1.5), nTiles - 0.5);
                y = Math.min(Math.max(y, 0.5), nTiles - 0.5);

                int i0 = (int) Math.min(Math.max(x, 1), nTiles - 2);
                int j0 = (int) Math.min(Math.max(y, 1), nTiles - 2);

                double t1 = y - j0;
                double t0 = 1 - t1;

                dst[getTileN(ix, iy)] = (1 + i0 - x) * (t0 * src[getTileN(i0, j0)] + t1 * src[getTileN(i0, j0 + 1)]) +
                    (x - i0) * (t0 * src[getTileN(i0 + 1, j0)] + t1 * src[getTileN(i0 + 1, j0 + 1)]);
            }
        }
        setBounds(b, dst);
    }

    /**
     * adjusts velocty field to make sure that the flow into a tile equals the flow out
     */
    private void project() {
        double[] divergance = new double[totalNumberTiles];
        double[] pressure = new double[totalNumberTiles];
        for (int i = 1; i < nTiles - 1; i++) {
            for (int j = 1; j < nTiles - 1; j++) {
                divergance[getTileN(i, j)] = (float) -(0.5 * (velocX[getTileN(i + 1, j)]
                    - velocX[getTileN(i - 1, j)] + velocY[getTileN(i, j + 1)] - velocY[getTileN(i, j - 1)]));
            }
        }
        setBounds(0, divergance);
        setBounds(0, pressure);
        for (int k = 0; k < 20; k++) {
            for (int i = 1; i < nTiles - 1; i++) {
                for (int j = 1; j < nTiles - 1; j++) {
                    pressure[getTileN(i, j)] = ((divergance[getTileN(i, j)] + pressure[getTileN(i + 1, j)] +
                        pressure[getTileN(i - 1, j)] + pressure[getTileN(i, j + 1)] + pressure[getTileN(i, j - 1)]) / 4.0);
                }
            }
            setBounds(0, pressure);
        }
        for (int i = 1; i < nTiles - 1; i++) {
            for (int j = 1; j < nTiles - 1; j++) {
                velocX[getTileN(i, j)] -= 0.5 * (pressure[getTileN(i + 1, j)] - pressure[getTileN(i - 1, j)]);
                velocY[getTileN(i, j)] -= 0.5 * (pressure[getTileN(i, j + 1)] - pressure[getTileN(i, j - 1)]);
            }
        }
        setBounds(1, velocX);
        setBounds(2, velocY);
    }

    /**
     * Simulate correct behaviour near the boundaries of the grid
     */
    private void setBounds(int b, double[] arr) {
        for (int i = 1; i < nTiles - 1; i++) {
            arr[getTileN(0, i)] = (b == 1) ? -arr[getTileN(1, i)] : arr[getTileN(1, i)];
            arr[getTileN(nTiles - 1, i)] = (b == 1) ? -arr[getTileN(nTiles - 2, i)] : arr[getTileN(nTiles - 2, i)];
            arr[getTileN(i, 0)] = (b == 2) ? -arr[getTileN(i, 1)] : arr[getTileN(i, 1)];
            arr[getTileN(i, nTiles - 1)] = (b == 2) ? -arr[getTileN(i, nTiles - 2)] : arr[getTileN(i, nTiles - 2)];
        }

        arr[getTileN(0, 0)] = (0.5 * (arr[getTileN(1, 0)] + arr[getTileN(0, 1)]));
        arr[getTileN(0, nTiles - 1)] =  (0.5 * (arr[getTileN(1, nTiles - 1)] + arr[getTileN(0, nTiles - 2)]));
        arr[getTileN(nTiles - 1, 0)] =  (0.5 * (arr[getTileN(nTiles - 2, 0)] + arr[getTileN(nTiles - 1, 1)]));
        arr[getTileN(nTiles - 1, nTiles - 1)] =  (0.5 * (arr[getTileN(nTiles - 2, nTiles - 1)] + arr[getTileN(nTiles - 1, nTiles - 2)]));
    }

    /**
     * Converts geoposition to the correct x and y in the grid.
     * @param position position to convert
     * @param environment environment the pollution is situated in
     * @return correct coordinates in the grid
     */
    private Point geoPositionToXY(GeoPosition position, Environment environment){
        MapHelper mapHelper = environment.getMapHelper();
        int sourceX = mapHelper.toMapXCoordinate(position);
        int sourceY = mapHelper.toMapYCoordinate(position);
        int maxX = environment.getMaxXpos() + 1;
        int maxY = environment.getMaxYpos() + 1;
        sourceX = sourceX*nTiles / maxX;
        sourceY = sourceY*nTiles / maxY;

        return new Point(Math.abs(sourceX), Math.abs(sourceY));
    }

    /**
     * Add pollution from all sources in this environment!
     * @param currentTime time to add pollution from
     * @param environment environment the pollution is situated in
     */
    private void addSources(double currentTime, Environment environment) {

        for(Source source:sources){
            Point XY = geoPositionToXY(source.getPosition(), environment);
            density[getTileN(XY.x, XY.y)] += source.generateData(currentTime);
        }

    }

    public List<Source> getSources() {
        return sources;
    }


    /**
     * Resets the environment
     */
    public void reset(){
        clear();
        sources = new ArrayList<>();
    }

    /**
     * Clears all values!
     */
    public void clear(){
        velocX = new double[totalNumberTiles];
        velocY = new double[totalNumberTiles];
        density = new double[totalNumberTiles];
        densityOld = new double[totalNumberTiles];
        velocXOld = new double[totalNumberTiles];
        velocYOld = new double[totalNumberTiles];
    }
}

