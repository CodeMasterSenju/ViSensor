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

    public boolean createModel(double[] startingCoordinates, double[][] coordinates, String path)
    {
        Vector2D[] vectors = new Vector2D[coordinates.length];
        for (int i = 0; i < coordinates.length; i++)
        {
            double x = coordinates[i][0];
            double y = coordinates[i][1];
            Vector2D v = new Vector2D(x,y);
            vectors[i] = v;
        }
        Log.d("translateToVectors","done");

        Vector2D[] outerPoints = getOuterPoints(vectors);
        Log.d("getOuterPoints","done "+ outerPoints.length);

        expandOuterPoints(outerPoints);
        Log.d("expandOuterPoints","done");

        sortVectors(outerPoints);
        Log.d("sortVectors","done");

        for (int i = 0; i < outerPoints.length; i++)
        {
            Log.d("x,y",outerPoints[i].dX + " , " + outerPoints[i].dY);
        }

        //Create obj. file


        return true;
    }

    private Vector2D[] getOuterPoints(Vector2D[] vectors)
    {
        ArrayList<Vector2D> outerPoints = new ArrayList<>();
        Vector2D middle = getMedian(vectors);
        Log.d("getMedian","done");

        for (int i = 0; i < vectors.length; i++)
        {
            boolean isouterpoint = true;
            Vector2D a = vectors[i].sub(middle);
            double al = a.dotProduct(a);

            for (int j = 0; j < vectors.length; j++)
            {
                if(vectors[j].sub(middle).dotProduct(a) >= al && i!=j)
                {
                    isouterpoint = false;
                    break;
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
            if(v[i].getdX() < leftmost.getdX())
            {
                leftmost = v[i];
            }
            if(v[i].getdX() > rightmost.getdX())
            {
                rightmost = v[i];
            }
            if(v[i].getdY() < downmost.getdY())
            {
                downmost = v[i];
            }
            if(v[i].getdY() > upmost.getdX())
            {
                upmost = v[i];
            }
        }

        double lx = rightmost.getdX() - leftmost.getdX();
        double ly = upmost.getdY() - downmost.getdY();
        double size = min(lx,ly);
        double factor = (size + 2*extraSpace)/size;

        for (int i = 0; i < v.length; i++)
        {
            v[i] = v[i].scale(factor);
        }
    }

    private Vector2D getMedian(Vector2D[] vectors)
    {
        Vector2D middle = new Vector2D(0,0);

        for (int i = 0; i < vectors.length; i++)
        {
            middle = middle.add(vectors[i]);
        }
        middle = middle.scale(1.0/(double)vectors.length);

        Log.d("Middle: ",middle.dX + " , " + middle.dY);

        return middle;
    }

    private void sortVectors(Vector2D[] v)
    {
        double deg;

        for (int i = 0; i < v.length; i++)
        {
            deg = acos(v[i].normalize().getdX());
            if(v[i].getdY() < 0) deg = 2*PI - deg;
            v[i].setDegree(deg);
        }
        Arrays.sort(v);
    }
}
