package com.artur.softwareproject;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

import static java.lang.Math.*;

/**
 * Created by gabriel on 11.05.17.
 */

public class ModelConstructor
{
    /**
     * Create A 3D-Model of a Room surrounding the passed coordinates
     *
     * @param startingCoordinates
     * @param coordinates         Array of coordinates that should be surrounded by the 3D-Model
     * @param path                Path to the location where the .obj file should be saved
     * @return was the .obj file successfully created?
     */
    public boolean createModel(double[] startingCoordinates, double[][] coordinates, String path)
    {
        Vector2D[] vectors = translateToVectors(coordinates);
        Log.d("translateToVectors", "done");

        vectors = getSurroundingPoints(vectors);
        Log.d("translateToVectors", "done");


        //Create obj. file


        return true;
    }

    /**
     * get the vectors, that when connected enclose all passed vectors, in the right order
     *
     * @param vectors Set of Vectors to be surrounded
     * @return Set of Vectors surrounding the passed Vectors
     */
    private Vector2D[] getSurroundingPoints(Vector2D[] vectors)
    {
        Vector2D[] surroundingPoints = getOuterPoints(vectors);
        Log.d("getOuterPoints", "done " + surroundingPoints.length);

        sortVectors(surroundingPoints);
        Log.d("sortVectors", "done");

        expandOuterPoints(surroundingPoints);
        Log.d("expandOuterPoints", "done");

        for (int i = 0; i < surroundingPoints.length; i++)
        {
            Log.d("x,y", surroundingPoints[i].dX + " , " + surroundingPoints[i].dY);
        }

        return surroundingPoints;
    }

    /**
     * Translate two-dimensional double array to Vector2D array
     *
     * @param coordinates coordinates to be translated
     * @return passed coordinates as 2D-Vectors
     */
    private Vector2D[] translateToVectors(double[][] coordinates)
    {
        Vector2D[] vectors = new Vector2D[coordinates.length];
        for (int i = 0; i < coordinates.length; i++)
        {
            double x = coordinates[i][0];
            double y = coordinates[i][1];
            Vector2D v = new Vector2D(x, y);
            vectors[i] = v;
        }

        return vectors;
    }

    /**
     * get the outer points from a Set of Vectors
     *
     * @param vectors set of Vectors
     * @return all outer points of the passed vectors
     */
    private Vector2D[] getOuterPoints(Vector2D[] vectors)
    {
        Vector2D[] initialOuterPoints = getInitialOuterPoints(vectors);

        sortVectors(initialOuterPoints);

        Vector2D avg = getAverage(initialOuterPoints);

        ArrayList<Vector2D> outerPoints = new ArrayList<Vector2D>(Arrays.asList(initialOuterPoints));

        boolean loop = true;

        while (loop) //do until all vectors are enclosed by the outerPoints
        {
            loop = false;
            int s = outerPoints.size();

            for (int i = 0; i < s; i++) //do for all edges of the surrounding graph
            {
                int j = i + 1;
                if (j == s) j = 0;

                Vector2D v1 = outerPoints.get(i);
                Vector2D v2 = outerPoints.get(j);

                Vector2D e = v2.sub(v1);
                Vector2D n = e.getNormalVector();

                if (v1.sub(avg).dotProduct(n) < 0) //find the outward facing normal vector to an edge
                {
                    n = n.scale(-1.0);
                }

                Vector2D max = v1;

                for (int k = 0; k < vectors.length; k++) //find point on the outside of the OuterPoint graph
                {
                    if (vectors[k].dotProduct(n) > max.dotProduct(n) && vectors[k].dotProduct(e) > v1.dotProduct(e) &&
                            vectors[k].dotProduct(e) < v2.dotProduct(e))
                    {
                        max = vectors[k];
                    }
                }

                if (max != v1 && max != v2 && !outerPoints.contains(max)) //if point was found add to outerPoints
                {
                    loop = true;
                    outerPoints.add(max);
                }
            }

            if (loop)//sort outerPoints to represent the enclosing graph again
            {
                sortVectors(outerPoints);
            }

        }

        Vector2D[] ret = new Vector2D[outerPoints.size()];
        ret = outerPoints.toArray(ret);

        return ret;

    }

    /**
     * get a few outer points from the passed Vectors
     *
     * @param vectors Set of Vectors
     * @return few outer points of the passed vectors
     */
    private Vector2D[] getInitialOuterPoints(Vector2D[] vectors)
    {
        ArrayList<Vector2D> outerPoints = new ArrayList<>();

        if(vectors.length <= 2)//if there are too few points return rectangle
        {
            double xMin = -3;
            double yMin = -3;
            double xMax = 3;
            double yMax = 3;

            for (int i = 0; i < vectors.length; i++)
            {
                if(vectors[i].dX < xMin)
                    xMin = vectors[i].dX;
                if(vectors[i].dX > xMax)
                    xMax = vectors[i].dX;
                if(vectors[i].dY < yMin)
                    yMin = vectors[i].dY;
                if(vectors[i].dY > yMax)
                    yMax = vectors[i].dY;
            }
            outerPoints.add(new Vector2D(xMin,yMin));
            outerPoints.add(new Vector2D(xMin,yMax));
            outerPoints.add(new Vector2D(xMax,yMin));
            outerPoints.add(new Vector2D(xMax,yMax));

            Vector2D[] ret = new Vector2D[outerPoints.size()];
            ret = outerPoints.toArray(ret);
            return ret;
        }

        Vector2D avg = getAverage(vectors);
        Vector2D middle = getMiddle(vectors);
        Vector2D[] midPoints = {avg, middle};
        Log.d("getAverage", "done");


        for (int i = 0; i < vectors.length; i++)
        {
            boolean isouterpoint = false;

            for (int k = 0; k < midPoints.length; k++)
            {
                boolean temp = true;

                Vector2D a = vectors[i].sub(midPoints[k]);
                double al = a.dotProduct(a);

                for (int j = 0; j < vectors.length; j++)
                {
                    if (vectors[j].sub(midPoints[k]).dotProduct(a) > al)
                    {
                        temp = false;
                        break;
                    }
                }

                if (temp) isouterpoint = true;
            }

            if (isouterpoint)
            {
                for (int j = 0; j < outerPoints.size(); j++)
                {
                    if (outerPoints.get(j).dX == vectors[i].dX && outerPoints.get(j).dY == vectors[i].dY)
                    {
                        isouterpoint = false;
                    }
                }
            }

            if (isouterpoint)
            {
                outerPoints.add(vectors[i]);
            }

        }
        Vector2D[] ret = new Vector2D[outerPoints.size()];
        ret = outerPoints.toArray(ret);
        return ret;
    }

    /**
     * move all edges between the vectors outwards by a fixed value
     *
     * @param v surrounding Vectors
     */
    private void expandOuterPoints(Vector2D[] v)
    {
        double extraSpace = 0.5;

        Vector2D[] extraVectors = new Vector2D[v.length];

        for (int i = 0; i < extraVectors.length; i++)
        {
            extraVectors[i] = new Vector2D(0, 0);
        }


        Vector2D v1 = null;
        Vector2D v2 = null;
        Vector2D e = null;
        Vector2D n = null;
        Vector2D avg = getAverage(v);

        for (int i = 0; i < v.length; i++)// find direction in which each vector should be moved
        {
            int j = i + 1;
            if (j == v.length)
            {
                j = 0;
            }

            v1 = v[i];
            v2 = v[j];
            e = v2.sub(v1);
            n = e.getNormalVector();
            if (v1.sub(avg).dotProduct(n) < 0)
            {
                n = n.scale(-1.0);
            }

            extraVectors[i] = extraVectors[i].add(n);
            extraVectors[j] = extraVectors[j].add(n);
        }

        for (int i = 0; i < v.length; i++)// find amount by which each vector should be moved
        {
            int j = i + 1;
            if (j == v.length)
                j = 0;

            v1 = v[i];
            v2 = v[j];
            e = v2.sub(v1);
            n = e.getNormalVector();
            if (v1.sub(avg).dotProduct(n) < 0)
            {
                n = n.scale(-1.0);
            }

            double t = (extraSpace) / (extraVectors[i].dotProduct(n));

            extraVectors[i] = extraVectors[i].scale(t);
        }

        for (int i = 0; i < v.length; i++)// move vectors
        {
            v[i] = v[i].add(extraVectors[i]);
        }
    }

    /**
     * get the average vector from a set of vectors
     *
     * @param vectors set of vectors
     * @return average vector
     */
    public Vector2D getAverage(Vector2D[] vectors)
    {
        Vector2D avg = new Vector2D(0, 0);

        for (int i = 0; i < vectors.length; i++)
        {
            avg = avg.add(vectors[i]);
        }
        avg = avg.scale(1.0 / (double) vectors.length);

        Log.d("avg: ", avg.dX + " , " + avg.dY);

        return avg;
    }

    /**
     * get the vector in the middle of the passed vectors
     *
     * @param v set of vectors
     * @return middle-vector
     */
    public Vector2D getMiddle(Vector2D[] v)
    {
        Vector2D leftmost = v[0];
        Vector2D rightmost = v[0];
        Vector2D upmost = v[0];
        Vector2D downmost = v[0];

        for (int i = 0; i < v.length; i++)
        {
            if (v[i].getdX() < leftmost.getdX())
            {
                leftmost = v[i];
            }
            if (v[i].getdX() > rightmost.getdX())
            {
                rightmost = v[i];
            }
            if (v[i].getdY() < downmost.getdY())
            {
                downmost = v[i];
            }
            if (v[i].getdY() > upmost.getdY())
            {
                upmost = v[i];
            }
        }

        double mx = leftmost.dX + (rightmost.dX - leftmost.dX) / 2.0;
        double my = downmost.dY + (upmost.dY - downmost.dY) / 2.0;

        return new Vector2D(mx, my);
    }

    /**
     * sort vectors into the right order
     *
     * @param v set of vectors
     */
    private void sortVectors(Vector2D[] v)
    {
        Vector2D avg = getAverage(v);
        double deg;

        for (int i = 0; i < v.length; i++)
        {
            deg = acos(v[i].sub(avg).normalize().getdX());
            if (v[i].sub(avg).getdY() < 0) deg = 2 * PI - deg;
            v[i].setDegree(deg);
        }
        Arrays.sort(v);
    }

    /**
     * sort vectors into the right order
     *
     * @param v set of vectors
     */
    private void sortVectors(ArrayList<Vector2D> v)
    {
        Vector2D[] vec = new Vector2D[v.size()];
        vec = v.toArray(vec);

        sortVectors(vec);

        v.clear();
        v.addAll(Arrays.asList(vec));
    }
}
