package com.artur.softwareproject;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
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
     * @param coordinates         Array of coordinates that should be surrounded by the 3D-Model
     * @param name               name of the obj-file that should be created
     * @return was the .obj file successfully created?
     */
    public Vector3D[] createModel( double[][] coordinates, String name)
    {
        Vector3D[] vectors = translateToVectors(coordinates);
        Log.d("translateToVectors", "done");

        vectors = getSurroundingPoints(vectors);
        Log.d("translateToVectors", "done");

        String s = generateString(vectors,name);
        //Create obj. file

        createFile(s,name);


        return vectors;
    }

    /**
     * get the vectors, that when connected enclose all passed vectors, in the right order
     *
     * @param vectors Set of Vectors to be surrounded
     * @return Set of Vectors surrounding the passed Vectors
     */
    private Vector3D[] getSurroundingPoints(Vector3D[] vectors)
    {
        Vector3D[] surroundingPoints = getOuterPoints(vectors);
        Log.d("getOuterPoints", "done " + surroundingPoints.length);

        sortVectors(surroundingPoints);
        Log.d("sortVectors", "done");

        expandOuterPoints(surroundingPoints);
        Log.d("expandOuterPoints", "done");

        for (int i = 0; i < surroundingPoints.length; i++)
        {
            Log.d("x,y", surroundingPoints[i].x + " , " + surroundingPoints[i].y);
        }

        return surroundingPoints;
    }

    /**
     * Translate two-dimensional double array to Vector3D array
     *
     * @param coordinates coordinates to be translated
     * @return passed coordinates as 2D-Vectors
     */
    private Vector3D[] translateToVectors(double[][] coordinates)
    {
        Vector3D[] vectors = new Vector3D[coordinates.length];
        for (int i = 0; i < coordinates.length; i++)
        {
            double x = coordinates[i][0];
            double y = coordinates[i][1];
            double z = coordinates[i][2];
            Vector3D v = new Vector3D(x,y,z);
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
    private Vector3D[] getOuterPoints(Vector3D[] vectors)
    {
        Vector3D[] initialOuterPoints = getInitialOuterPoints(vectors);

        sortVectors(initialOuterPoints);

        Vector3D avg = getAverage(initialOuterPoints);

        ArrayList<Vector3D> outerPoints = new ArrayList<Vector3D>(Arrays.asList(initialOuterPoints));

        boolean loop = true;

        while (loop) //do until all vectors are enclosed by the outerPoints
        {
            loop = false;
            int s = outerPoints.size();

            for (int i = 0; i < s; i++) //do for all edges of the surrounding graph
            {
                int j = i + 1;
                if (j == s) j = 0;

                Vector3D v1 = outerPoints.get(i);
                Vector3D v2 = outerPoints.get(j);

                Vector3D e = v2.sub(v1);
                Vector3D n = e.cross(new Vector3D(0,1,0));

                if (v1.sub(avg).dot(n) < 0) //find the outward facing normal vector to an edge
                {
                    n = n.scale(-1.0);
                }

                Vector3D max = v1;

                for (int k = 0; k < vectors.length; k++) //find point on the outside of the OuterPoint graph
                {
                    if (vectors[k].dot(n) > max.dot(n) && vectors[k].dot(e) > v1.dot(e) &&
                            vectors[k].dot(e) < v2.dot(e))
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

        Vector3D[] ret = new Vector3D[outerPoints.size()];
        ret = outerPoints.toArray(ret);

        return ret;

    }

    /**
     * get a few outer points from the passed Vectors
     *
     * @param vectors Set of Vectors
     * @return few outer points of the passed vectors
     */
    private Vector3D[] getInitialOuterPoints(Vector3D[] vectors)
    {
        ArrayList<Vector3D> outerPoints = new ArrayList<>();

        if(vectors.length <= 2)//if there are too few points return rectangle
        {
            double xMin = -3;
            double zMin = -3;
            double xMax = 3;
            double zMax = 3;

            for (int i = 0; i < vectors.length; i++)
            {
                if(vectors[i].x < xMin)
                    xMin = vectors[i].x;
                if(vectors[i].x > xMax)
                    xMax = vectors[i].x;
                if(vectors[i].z < zMin)
                    zMin = vectors[i].z;
                if(vectors[i].z > zMax)
                    zMax = vectors[i].z;
            }
            outerPoints.add(new Vector3D(xMin,0,zMin));
            outerPoints.add(new Vector3D(xMin,0,zMax));
            outerPoints.add(new Vector3D(xMax,0,zMin));
            outerPoints.add(new Vector3D(xMax,0,zMax));

            Vector3D[] ret = new Vector3D[outerPoints.size()];
            ret = outerPoints.toArray(ret);
            sortVectors(ret);
            return ret;
        }

        Vector3D avg = getAverage(vectors);
        Log.d("getAverage", "done");


        for (int i = 0; i < vectors.length; i++)
        {
            boolean isouterpoint = false;

            boolean temp = true;

            Vector3D a = vectors[i].sub(avg);
            a.y = 0;
            double al = a.dot(a);

            for (int j = 0; j < vectors.length; j++)
            {
                Vector3D b = vectors[j].sub(avg);
                b.y = 0;
                if (vectors[j].sub(avg).dot(a) > al)
                {
                    temp = false;
                    break;
                }
            }

            if (temp) isouterpoint = true;


            if (isouterpoint)
            {
                for (int j = 0; j < outerPoints.size(); j++)
                {
                    if (outerPoints.get(j).x == vectors[i].x && outerPoints.get(j).y == vectors[i].y && outerPoints.get(j).z == vectors[i].z )
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
        Vector3D[] ret = new Vector3D[outerPoints.size()];
        ret = outerPoints.toArray(ret);
        return ret;
    }

    /**
     * move all edges between the vectors outwards by a fixed value
     *
     * @param v surrounding Vectors
     */
    private void expandOuterPoints(Vector3D[] v)
    {
        double extraSpace = 0.5;

        Vector3D[] extraVectors = new Vector3D[v.length];

        for (int i = 0; i < extraVectors.length; i++)
        {
            extraVectors[i] = new Vector3D(0,0,0);
        }


        Vector3D v1 = null;
        Vector3D v2 = null;
        Vector3D e = null;
        Vector3D n = null;
        Vector3D avg = getAverage(v);

        for (int i = 0; i < v.length; i++)// find direction in which each vector should be moved
        {
            int j = i + 1;
            if (j == v.length)
            {
                j = 0;
            }

            v1 = v[i];
            v2 = v[j];
            v1.y = 0;
            v2.y = 0;
            e = v2.sub(v1);
            n = e.cross(new Vector3D(0,1,0));
            if (v1.sub(avg).dot(n) < 0)
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
            v1.y = 0;
            v2.y = 0;
            e = v2.sub(v1);
            n = e.cross(new Vector3D(0,1,0));
            if (v1.sub(avg).dot(n) < 0)
            {
                n = n.scale(-1.0);
            }

            double t = (extraSpace) / (extraVectors[i].dot(n));

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
    public Vector3D getAverage(Vector3D[] vectors)
    {
        Vector3D avg = new Vector3D(0,0,0);

        for (int i = 0; i < vectors.length; i++)
        {
            avg = avg.add(vectors[i]);
        }
        avg = avg.scale(1.0 / (double) vectors.length);

        Log.d("avg: ", avg.x + " , " + avg.y);

        return avg;
    }

    /**
     * sort vectors into the right order
     *
     * @param v set of vectors
     */
    private void sortVectors(Vector3D[] v)
    {
        Vector3D avg = getAverage(v);
        double deg;

        for (int i = 0; i < v.length; i++)
        {
            deg = acos(v[i].sub(avg).normalize().x);
            if (v[i].sub(avg).z < 0) deg = 2 * PI - deg;
            v[i].setDegree(deg);
        }
        Arrays.sort(v);
    }

    /**
     * sort vectors into the right order
     *
     * @param v set of vectors
     */
    private void sortVectors(ArrayList<Vector3D> v)
    {
        Vector3D[] vec = new Vector3D[v.size()];
        vec = v.toArray(vec);

        sortVectors(vec);

        v.clear();
        v.addAll(Arrays.asList(vec));
    }

    private String generateString(Vector3D[] surroundingPoints, String name)
    {
        ArrayList<Vector3D> vectors = new ArrayList<>();
        ArrayList<int[]> planes = new ArrayList<>();

        Vector3D avg = getAverage(surroundingPoints);

        for (int i = 0; i < surroundingPoints.length; i++)
        {
            int j = i+1;
            if(j==surroundingPoints.length)
                j=0;

            buildFence(vectors,planes,surroundingPoints[i],surroundingPoints[j],avg);
        }

        int i = planes.size();

        buildFloor(vectors,planes,surroundingPoints);

        Vector3D[] va = new Vector3D[vectors.size()];
        va = vectors.toArray(va);

        int[][] pa = new int[planes.size()][];
        pa = planes.toArray(pa);

        Log.d("planeNr.", " " + pa.length);

        String s = makeString(va,pa,i,name);

        return s;
    }

    private void buildFence(ArrayList<Vector3D> vectors, ArrayList<int[]> planes, Vector3D v1, Vector3D v2, Vector3D avg)
    {
        Vector3D n1 = avg.sub(v1).normalize();
        n1.y = 0;
        Vector3D nn1 = n1.cross(new Vector3D(0,1,0));
        if (v2.sub(v1).dot(nn1) < 0)
        {
            nn1 = nn1.scale(-1.0);
        }
        Vector3D n2 = avg.sub(v2).normalize();

        buildFencePost(vectors,planes,v1,n1,nn1);
        buildFenceBoards(vectors,planes,v1,n1,v2,n2);
    }

    private void buildFencePost(ArrayList<Vector3D> vectors, ArrayList<int[]> planes, Vector3D v1, Vector3D n1, Vector3D nn1)
    {
        double postWidth = 0.1;
        double postHeight = 1.0;

        vectors.add(v1.add((n1.add(nn1).normalize().scale(postWidth/2.0))));
        vectors.add(v1.sub((n1.sub(nn1).normalize().scale(postWidth/2.0))));
        vectors.add(v1.sub((n1.add(nn1).normalize().scale(postWidth/2.0))));
        vectors.add(v1.add((n1.sub(nn1).normalize().scale(postWidth/2.0))));

        vectors.add(v1.add((n1.add(nn1).normalize().scale(postWidth/2.0))).add(new Vector3D(0,postHeight,0)));
        vectors.add(v1.sub((n1.sub(nn1).normalize().scale(postWidth/2.0))).add(new Vector3D(0,postHeight,0)));
        vectors.add(v1.sub((n1.add(nn1).normalize().scale(postWidth/2.0))).add(new Vector3D(0,postHeight,0)));
        vectors.add(v1.add((n1.sub(nn1).normalize().scale(postWidth/2.0))).add(new Vector3D(0,postHeight,0)));

        int s = vectors.size()-1;
        int[] i;

        i = new int[]{s-3,s-2,s-1,s-0};
        planes.add(i);

        i = new int[]{s-4,s-5,s-6,s-7};
        planes.add(i);

        i = new int[]{s-7,s-4,s-0,s-3};
        planes.add(i);

        i = new int[]{s-4,s-5,s-1,s-0};
        planes.add(i);

        i = new int[]{s-5,s-6,s-2,s-1};
        planes.add(i);

        i = new int[]{s-6,s-7,s-3,s-2};
        planes.add(i);
    }

    private void buildFenceBoards(ArrayList<Vector3D> vectors, ArrayList<int[]> planes, Vector3D v1, Vector3D n1, Vector3D v2, Vector3D n2)
    {
        double boardSize = 0.3;
        double boardwidth = 0.05;
        double boardHeight1 = 0.3;
        double boardHeight2 = 0.65;

        //build board nr 1
        vectors.add(v1.add(n1.normalize().scale(boardwidth/2)).add(new Vector3D(0,boardHeight1,0)));
        vectors.add(v2.add(n2.normalize().scale(boardwidth/2)).add(new Vector3D(0,boardHeight1,0)));
        vectors.add(v2.add(n2.normalize().scale(-1 * boardwidth/2)).add(new Vector3D(0,boardHeight1,0)));
        vectors.add(v1.add(n1.normalize().scale(-1 * boardwidth/2)).add(new Vector3D(0,boardHeight1,0)));

        vectors.add(v1.add(n1.normalize().scale(boardwidth/2)).add(new Vector3D(0,boardHeight1 + boardSize,0)));
        vectors.add(v2.add(n2.normalize().scale(boardwidth/2)).add(new Vector3D(0,boardHeight1 + boardSize,0)));
        vectors.add(v2.add(n2.normalize().scale(-1 * boardwidth/2)).add(new Vector3D(0,boardHeight1 + boardSize,0)));
        vectors.add(v1.add(n1.normalize().scale(-1 * boardwidth/2)).add(new Vector3D(0,boardHeight1 + boardSize,0)));

        int s = vectors.size()-1;
        int[] i;

        i = new int[]{s-3,s-2,s-1,s-0};
        planes.add(i);

        i = new int[]{s-4,s-5,s-6,s-7};
        planes.add(i);

        i = new int[]{s-7,s-4,s-0,s-3};
        planes.add(i);

        i = new int[]{s-4,s-5,s-1,s-0};
        planes.add(i);

        i = new int[]{s-5,s-6,s-2,s-1};
        planes.add(i);

        i = new int[]{s-6,s-7,s-3,s-2};
        planes.add(i);


        //build board nr 2
        vectors.add(v1.add(n1.normalize().scale(boardwidth/2)).add(new Vector3D(0,boardHeight2,0)));
        vectors.add(v2.add(n2.normalize().scale(boardwidth/2)).add(new Vector3D(0,boardHeight2,0)));
        vectors.add(v2.add(n2.normalize().scale(-1 * boardwidth/2)).add(new Vector3D(0,boardHeight2,0)));
        vectors.add(v1.add(n1.normalize().scale(-1 * boardwidth/2)).add(new Vector3D(0,boardHeight2,0)));

        vectors.add(v1.add(n1.normalize().scale(boardwidth/2)).add(new Vector3D(0,boardHeight2 + boardSize,0)));
        vectors.add(v2.add(n2.normalize().scale(boardwidth/2)).add(new Vector3D(0,boardHeight2 + boardSize,0)));
        vectors.add(v2.add(n2.normalize().scale(-1 * boardwidth/2)).add(new Vector3D(0,boardHeight2 + boardSize,0)));
        vectors.add(v1.add(n1.normalize().scale(-1 * boardwidth/2)).add(new Vector3D(0,boardHeight2 + boardSize,0)));

        i = new int[]{s-3,s-2,s-1,s-0};
        planes.add(i);

        i = new int[]{s-4,s-5,s-6,s-7};
        planes.add(i);

        i = new int[]{s-7,s-4,s-0,s-3};
        planes.add(i);

        i = new int[]{s-4,s-5,s-1,s-0};
        planes.add(i);

        i = new int[]{s-5,s-6,s-2,s-1};
        planes.add(i);

        i = new int[]{s-6,s-7,s-3,s-2};
        planes.add(i);
    }

    public void buildFloor(ArrayList<Vector3D> vectors, ArrayList<int[]> planes, Vector3D[] surrouningPoints)
    {
        for (int i = 0; i < surrouningPoints.length; i++)
        {
            vectors.add(new Vector3D(surrouningPoints[i]));
        }

        int[] p = new int[surrouningPoints.length];

        for (int i = 0; i < surrouningPoints.length; i++)
        {
            p[i] = vectors.size()-(surrouningPoints.length-i);
        }

        planes.add(p);
    }

    private String makeString(Vector3D[] vectors, int[][] planes, int floorStartIndex, String name)
    {
        String s = "mtllib."+name+"\n";

        Vector3D v;

        for (int i = 0; i < vectors.length; i++)
        {
            v = vectors[i];
            String ts = "v " + v.x + " " + v.y + " " + v.z + "\n";
            Log.d("ts:", " " + ts);
            s += ts;
        }

        int[] p;

        s += "usemtl fence" + "\n";

        for (int i = 0; i < planes.length; i++)
        {
            if(i==floorStartIndex)
            {
                s += "usemtl floor\n";
            }

            p = planes[i];
            String ts = "f ";

            for (int j = 0; j < p.length; j++)
            {
                ts += p[j] + " ";
            }
            ts += "\n";
            Log.d("ts:", " " + ts);
            s += ts;
        }

        Log.d("String_s: ", s);

        return s;
    }

    private void createFile(String s, String name)
    {
        File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), name);

        try {
            f.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(f, false /*append*/));
            writer.write(s);
            writer.close();
            Log.d("saved","saved file successfully to: " + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()+name);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
