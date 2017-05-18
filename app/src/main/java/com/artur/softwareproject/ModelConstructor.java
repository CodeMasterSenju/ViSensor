package com.artur.softwareproject;

import android.util.Log;
import android.util.Pair;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

import static java.lang.Math.*;

/**
 * Created by gabriel on 11.05.17.
 */

public class ModelConstructor
{

    public boolean createModel(double[] startingCoordinates, double[][] coordinates, String path)
    {
        Vector2D[] vectors = translateToVectors(coordinates);
        Log.d("translateToVectors", "done");

        vectors = getSurroundingPoints(vectors);
        Log.d("translateToVectors", "done");


        //Create obj. file


        return true;
    }

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

    private Vector2D[] getOuterPoints(Vector2D[] vectors)
    {
        Vector2D[] initialOuterPoints = getInitialOuterPoints(vectors);

        sortVectors(initialOuterPoints);

        Vector2D avg = getAverage(initialOuterPoints);

        ArrayList<Vector2D> outerPoints = new ArrayList<Vector2D>(Arrays.asList(initialOuterPoints));

        boolean loop = true;

        while (loop)
        {
            loop = false;
            int s = outerPoints.size();

            for (int i = 0; i < s; i++)
            {
                int j = i + 1;
                if (j == s) j = 0;

                Vector2D v1 = outerPoints.get(i);
                Vector2D v2 = outerPoints.get(j);

                Vector2D e = v2.sub(v1);
                Vector2D n = e.getNormalVector();

                if (v1.sub(avg).dotProduct(n) < 0)
                {
                    n = n.scale(-1.0);
                }

                Vector2D max = v1;

                for (int k = 0; k < vectors.length; k++)
                {
                    if (vectors[k].sub(avg).dotProduct(n) > max.sub(avg).dotProduct(n) && vectors[k].sub(avg).dotProduct(e) > v1.sub(avg).dotProduct(e) &&
                            vectors[k].sub(avg).dotProduct(e) < v2.sub(avg).dotProduct(e))
                    {
                        max = vectors[k];
                    }
                }

                if (max != v1 && max != v2 && !outerPoints.contains(max))
                {
                    loop = true;
                    outerPoints.add(max);
                }
            }

            if (loop)
            {
                sortVectors(outerPoints);
            }

        }

        Vector2D[] ret = new Vector2D[outerPoints.size()];
        ret = outerPoints.toArray(ret);

        return ret;

    }

    private Vector2D[] getInitialOuterPoints(Vector2D[] vectors)
    {
        ArrayList<Vector2D> outerPoints = new ArrayList<>();
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

    private void expandOuterPoints(Vector2D[] v)
    {
        double extraSpace = 0.5;

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

        double lx = rightmost.getdX() - leftmost.getdX();
        double ly = upmost.getdY() - downmost.getdY();
        double size = min(lx, ly);
        double factor = (size + 2 * extraSpace) / size;

        for (int i = 0; i < v.length; i++)
        {
            v[i] = v[i].scale(factor);
        }
    }

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

    private void sortVectors(ArrayList<Vector2D> v)
    {
        Vector2D[] vec = new Vector2D[v.size()];
        vec = v.toArray(vec);

        sortVectors(vec);

        v = new ArrayList<>(Arrays.asList(vec));
    }
}
