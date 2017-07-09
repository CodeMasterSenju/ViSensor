package com.artur.softwareproject;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import static android.os.SystemClock.sleep;
import static java.lang.Math.PI;
import static java.lang.Math.acos;


/**
 * Created by gabriel on 11.05.17.
 */
public class ModelConstructor
{
    /**
     * Create A 3D-Model of a Room surrounding the passed coordinates
     *
     * @param coordinates Array of coordinates that should be surrounded by the 3D-Model
     * @param name        name of the .obj file without the obj suffix
     * @param flatGround  should the ground be flat or have hills?
     * @return was the .obj file successfully created?
     */


    public static int createModel(double[][] coordinates, String name, boolean flatGround)
    {
        Vector3D[] vectors = translateToVectors(coordinates, flatGround);

        Vector3D[] surroundingPoints = translateToVectors(coordinates, flatGround);

        surroundingPoints = getSurroundingPoints(surroundingPoints);

        String s = generateString(surroundingPoints, name, vectors, flatGround);

        return createFile(s, name);

    }

    /**
     * get the vectors, that when connected enclose all passed vectors, in the right order
     *
     * @param vectors Set of Vectors to be surrounded
     * @return Set of Vectors surrounding the passed Vectors
     */
    private static Vector3D[] getSurroundingPoints(Vector3D[] vectors)
    {
        Vector3D[] surroundingPoints = getOuterPoints(vectors);

        sortVectors(surroundingPoints);

        expandOuterPoints(surroundingPoints);

        return surroundingPoints;
    }

    /**
     * Translate two-dimensional double array to Vector3D array with no y value exeeding 0
     *
     * @param coordinates coordinates to be translated
     * @param flatGround  should the ground be flat or have hills?
     * @return passed coordinates as 2D-Vectors
     */
    private static Vector3D[] translateToVectors(double[][] coordinates, boolean flatGround)
    {
        int size = coordinates.length;

        Vector3D[] vectors = new Vector3D[size];

        //Random r = new Random();

        double maxY = Double.MIN_VALUE;

        for (int i = 0; i < size; i++)
        {
            double x = coordinates[i][0];
            double y = coordinates[i][2];
            double z = coordinates[i][1];

            //y = r.nextDouble() * -5; //Test für die Höhenkoordinate

            Vector3D v = new Vector3D(x, y, z);
            vectors[i] = v;

            if (y > maxY)
                maxY = y;
        }

        Vector3D s = new Vector3D(0, maxY, 0);

        for (int i = 0; i < size; i++)
        {
            vectors[i] = vectors[i].sub(s);
            if (flatGround)
                vectors[i].y = 0;
        }

        return vectors;
    }

    /**
     * get the outer points from a Set of Vectors
     *
     * @param vectors set of Vectors
     * @return all outer points of the passed vectors
     */
    private static Vector3D[] getOuterPoints(Vector3D[] vectors)
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
                Vector3D n = e.cross(new Vector3D(0, 1, 0)).normalize();

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
    private static Vector3D[] getInitialOuterPoints(Vector3D[] vectors)
    {
        ArrayList<Vector3D> outerPoints = new ArrayList<>();

            double xMin = -3;
            double zMin = -3;
            double xMax = 3;
            double zMax = 3;

            for (int i = 0; i < vectors.length; i++)
            {
                if (vectors[i].x < xMin)
                    xMin = vectors[i].x;
                if (vectors[i].x > xMax)
                    xMax = vectors[i].x;
                if (vectors[i].z < zMin)
                    zMin = vectors[i].z;
                if (vectors[i].z > zMax)
                    zMax = vectors[i].z;
            }

        if (vectors.length <= 2 || xMax - xMin < 1 || zMax - zMin < 1)//if there are too few points or they are to little apart from another return rectangle
        {
            outerPoints.add(new Vector3D(xMin, 0, zMin));
            outerPoints.add(new Vector3D(xMin, 0, zMax));
            outerPoints.add(new Vector3D(xMax, 0, zMin));
            outerPoints.add(new Vector3D(xMax, 0, zMax));

            Vector3D[] ret = new Vector3D[outerPoints.size()];
            ret = outerPoints.toArray(ret);
            sortVectors(ret);
            return ret;
        }

        Vector3D avg = getAverage(vectors);


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
                    if (outerPoints.get(j).x == vectors[i].x && outerPoints.get(j).y == vectors[i].y && outerPoints.get(j).z == vectors[i].z)
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
    private static void expandOuterPoints(Vector3D[] v)
    {
        double extraSpace = 3;

        Vector3D[] extraVectors = new Vector3D[v.length];

        for (int i = 0; i < extraVectors.length; i++)
        {
            extraVectors[i] = new Vector3D(0, 0, 0);
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

            v1 = new Vector3D(v[i]);
            v2 = new Vector3D(v[j]);
            v1.y = 0;
            v2.y = 0;
            e = v2.sub(v1);
            n = e.cross(new Vector3D(0, 1, 0)).normalize();
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

            v1 = new Vector3D(v[i]);
            v2 = new Vector3D(v[j]);
            v1.y = 0;
            v2.y = 0;
            e = v2.sub(v1);
            n = e.cross(new Vector3D(0, 1, 0)).normalize();
            if (v1.sub(avg).dot(n) < 0)
            {
                n = n.scale(-1.0);
            }

            double t;
            double tdot = (extraVectors[i].dot(n));

            if (tdot==0)
                t=extraSpace;
            else
                t = (extraSpace) / (extraVectors[i].dot(n));

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
    private static Vector3D getAverage(Vector3D[] vectors)
    {
        Vector3D avg = new Vector3D(0, 0, 0);

        for (int i = 0; i < vectors.length; i++)
        {
            avg = avg.add(vectors[i]);
        }

        if(vectors.length==0)
            return new Vector3D(1,0,0);

        avg = avg.scale(1.0 / (double) vectors.length);

        return avg;
    }

    /**
     * sort vectors into the right order
     *
     * @param v set of vectors
     */
    private static void sortVectors(Vector3D[] v)
    {
        Vector3D avg = getAverage(v);
        double deg;

        for (int i = 0; i < v.length; i++)
        {
            deg = acos(v[i].sub(avg).normalize().x);
            if (v[i].sub(avg).z < 0) deg = 2 * PI - deg;
            v[i].comp = deg;
        }
        Arrays.sort(v);
    }

    /**
     * sort vectors into the right order
     *
     * @param v set of vectors
     */
    private static void sortVectors(ArrayList<Vector3D> v)
    {
        Vector3D[] vec = new Vector3D[v.size()];
        vec = v.toArray(vec);

        sortVectors(vec);

        v.clear();
        v.addAll(Arrays.asList(vec));
    }

    /**
     * sort passed Vectors by the x value
     *
     * @param v
     */
    private static void sortVectorsAfterX(Vector3D[] v)
    {
        for (int i = 0; i < v.length; i++)
        {
            v[i].comp = v[i].x;
        }
        Arrays.sort(v);
    }

    /**
     * Generate the String needed for creating the obj-file from the parameters
     *
     * @param surroundingPoints points surrounding all points
     * @param name              name of the .obj file without the .obj suffix
     * @param vectors           all points
     * @param flatGround        should the ground be flat or have hills?
     * @return
     */
    private static String generateString(Vector3D[] surroundingPoints, String name, Vector3D[] vectors, boolean flatGround)
    {
        ArrayList<Vector3D> vetices = new ArrayList<>();
        ArrayList<Vector3D> normalvetices = new ArrayList<>();
        ArrayList<double[]> texturevetices = new ArrayList<>();
        ArrayList<int[]> planes = new ArrayList<>();

        Vector3D avg = getAverage(surroundingPoints);

        for (int i = 0; i < surroundingPoints.length; i++)
        {
            int j = i + 1;
            if (j == surroundingPoints.length)
                j = 0;

            buildFence(vetices, normalvetices, texturevetices, planes, surroundingPoints[i], surroundingPoints[j], avg);
        }

        int i = planes.size();

        buildFloor(vetices, normalvetices, texturevetices, planes, surroundingPoints, vectors, flatGround);

        Vector3D[] va = new Vector3D[vetices.size()];
        va = vetices.toArray(va);

        Vector3D[] nva = new Vector3D[normalvetices.size()];
        nva = normalvetices.toArray(nva);

        double[][] tva = new double[texturevetices.size()][];
        tva = texturevetices.toArray(tva);

        int[][] pa = new int[planes.size()][];
        pa = planes.toArray(pa);

        String s = makeString(va, nva, tva, pa, i, name);

        return s;
    }

    /**
     * Add vertices to the passed vertices-arraylists to create a fence model between two points
     *
     * @param vertices        arraylist of vertices for the obj file
     * @param normalvertices  arraylist of normalvertices for the obj file
     * @param texturevertices arraylist of texturevertices for the obj file
     * @param planes          arraylist of planes for the obj-file
     * @param v1              point 1
     * @param v2              point 2
     * @param avg             Average Vector of Vectors to be surrounded by the fences
     */
    private static void buildFence(ArrayList<Vector3D> vertices, ArrayList<Vector3D> normalvertices, ArrayList<double[]> texturevertices, ArrayList<int[]> planes, Vector3D v1, Vector3D v2, Vector3D avg)
    {
        Vector3D n1 = avg.sub(v1);
        n1.y = 0;
        n1 = n1.normalize();
        Vector3D nn1 = n1.cross(new Vector3D(0, 1, 0)).normalize();
        if (v2.sub(v1).dot(nn1) < 0)
        {
            nn1 = nn1.scale(-1.0);
        }
        Vector3D n2 = avg.sub(v2);
        n2.y = 0;
        n2 = n2.normalize();

        buildFencePost(vertices, normalvertices, texturevertices, planes, v1, n1, nn1);
        buildFenceBoards(vertices, normalvertices, texturevertices, planes, v1, n1, v2, n2);
    }

    /**
     * Add vertices to the passed vertices-arraylists to create a fencepost model at a passed point
     *
     * @param vertices        arraylist of vertices for the obj file
     * @param normalvertices  arraylist of normalvertices for the obj file
     * @param texturevertices arraylist of texturevertices for the obj file
     * @param planes          arraylist of planes for the obj-file
     * @param v1              point
     * @param n1              directional vector inside
     * @param nn1             directional vector to the left
     */
    private static void buildFencePost(ArrayList<Vector3D> vertices, ArrayList<Vector3D> normalvertices, ArrayList<double[]> texturevertices, ArrayList<int[]> planes, Vector3D v1, Vector3D n1, Vector3D nn1)
    {
        double postWidth = 0.25;
        double postHeight = 1.0;

        vertices.add(v1.add((n1.add(nn1).normalize().scale(postWidth / 2.0))));
        vertices.add(v1.sub((n1.sub(nn1).normalize().scale(postWidth / 2.0))));
        vertices.add(v1.sub((n1.add(nn1).normalize().scale(postWidth / 2.0))));
        vertices.add(v1.add((n1.sub(nn1).normalize().scale(postWidth / 2.0))));

        vertices.add(v1.add((n1.add(nn1).normalize().scale(postWidth / 2.0))).add(new Vector3D(0, postHeight, 0)));
        vertices.add(v1.sub((n1.sub(nn1).normalize().scale(postWidth / 2.0))).add(new Vector3D(0, postHeight, 0)));
        vertices.add(v1.sub((n1.add(nn1).normalize().scale(postWidth / 2.0))).add(new Vector3D(0, postHeight, 0)));
        vertices.add(v1.add((n1.sub(nn1).normalize().scale(postWidth / 2.0))).add(new Vector3D(0, postHeight, 0)));

        texturevertices.add(new double[]{0, 0});      //-9
        texturevertices.add(new double[]{0, 0.25});   //-8
        texturevertices.add(new double[]{0, 0.5});    //-7
        texturevertices.add(new double[]{0, 0.75});   //-6
        texturevertices.add(new double[]{0, 1});      //-5
        texturevertices.add(new double[]{1, 0});      //-4
        texturevertices.add(new double[]{1, 0.25});   //-3
        texturevertices.add(new double[]{1, 0.5});    //-2
        texturevertices.add(new double[]{1, 0.75});   //-1
        texturevertices.add(new double[]{1, 1});      //-0

        int s = vertices.size();
        int ts = texturevertices.size();
        int ns = normalvertices.size();
        int[] i;

        i = new int[]{s - 0, 1, ts - 9, s - 1, 1, ts - 5, s - 2, 1, ts - 0, s - 3, 1, ts - 4};
        planes.add(i);

        i = new int[]{s - 7, 1, ts - 9, s - 6, 1, ts - 5, s - 5, 1, ts - 0, s - 4, 1, ts - 4};
        planes.add(i);

        i = new int[]{s - 7, 1, ts - 9, s - 4, 1, ts - 8, s - 0, 1, ts - 3, s - 3, 1, ts - 4};
        planes.add(i);

        i = new int[]{s - 4, 1, ts - 8, s - 5, 1, ts - 7, s - 1, 1, ts - 2, s - 0, 1, ts - 3};
        planes.add(i);

        i = new int[]{s - 5, 1, ts - 7, s - 6, 1, ts - 6, s - 2, 1, ts - 1, s - 1, 1, ts - 2};
        planes.add(i);

        i = new int[]{s - 6, 1, ts - 6, s - 7, 1, ts - 5, s - 3, 1, ts - 0, s - 2, 1, ts - 1};
        planes.add(i);
    }

    /**
     * Add vertices to the passed vertices-arraylists to create fence-boards between two points
     *
     * @param vertices        arraylist of vertices for the obj file
     * @param normalvertices  arraylist of normalvertices for the obj file
     * @param texturevertices arraylist of texturevertices for the obj file
     * @param planes          arraylist of planes for the obj-file
     * @param v1              point 1
     * @param v2              point 2
     * @param n1              directional vector inside from v1
     * @param n2              directional vector inside from v1
     */
    private static void buildFenceBoards(ArrayList<Vector3D> vertices, ArrayList<Vector3D> normalvertices, ArrayList<double[]> texturevertices, ArrayList<int[]> planes, Vector3D v1, Vector3D n1, Vector3D v2, Vector3D n2)
    {
        double boardSize = 0.3;
        double boardwidth = 0.05;
        double boardHeight1 = 0.3;
        double boardHeight2 = 0.65;

        //build board nr 1
        vertices.add(v1.add(n1.normalize().scale(boardwidth / 2)).add(new Vector3D(0, boardHeight1, 0)));
        vertices.add(v2.add(n2.normalize().scale(boardwidth / 2)).add(new Vector3D(0, boardHeight1, 0)));
        vertices.add(v2.add(n2.normalize().scale(-1 * boardwidth / 2)).add(new Vector3D(0, boardHeight1, 0)));
        vertices.add(v1.add(n1.normalize().scale(-1 * boardwidth / 2)).add(new Vector3D(0, boardHeight1, 0)));

        vertices.add(v1.add(n1.normalize().scale(boardwidth / 2)).add(new Vector3D(0, boardHeight1 + boardSize, 0)));
        vertices.add(v2.add(n2.normalize().scale(boardwidth / 2)).add(new Vector3D(0, boardHeight1 + boardSize, 0)));
        vertices.add(v2.add(n2.normalize().scale(-1 * boardwidth / 2)).add(new Vector3D(0, boardHeight1 + boardSize, 0)));
        vertices.add(v1.add(n1.normalize().scale(-1 * boardwidth / 2)).add(new Vector3D(0, boardHeight1 + boardSize, 0)));

        double l = v1.sub(v2).getLength();

        texturevertices.add(new double[]{l, 0});       //-12
        texturevertices.add(new double[]{1, 0});       //-11
        texturevertices.add(new double[]{1, 0.05});    //-10
        texturevertices.add(new double[]{0, 0});       //-9
        texturevertices.add(new double[]{0, 0.05});    //-8
        texturevertices.add(new double[]{0, 0.35});    //-7
        texturevertices.add(new double[]{0, 0.4});     //-6
        texturevertices.add(new double[]{0, 0.7});     //-5
        texturevertices.add(new double[]{l, 0});       //-4
        texturevertices.add(new double[]{l, 0.05});    //-3
        texturevertices.add(new double[]{l, 0.35});    //-2
        texturevertices.add(new double[]{l, 0.4});     //-1
        texturevertices.add(new double[]{l, 0.7});     //-0

        int s = vertices.size();
        int ts = texturevertices.size();
        int ns = normalvertices.size();
        int[] i;

        i = new int[]{s - 3, 1, ts - 9, s - 2, 1, ts - 4, s - 1, 1, ts - 3, s - 0, 1, ts - 8};
        planes.add(i);

        i = new int[]{s - 4, 1, ts - 8, s - 5, 1, ts - 3, s - 6, 1, ts - 4, s - 7, 1, ts - 9};
        planes.add(i);

        i = new int[]{s - 3, 1, ts - 9, s - 0, 1, ts - 8, s - 4, 1, ts - 10, s - 7, 1, ts - 11};
        planes.add(i);

        i = new int[]{s - 0, 1, ts - 5, s - 1, 1, ts - 0, s - 5, 1, ts - 4, s - 4, 1, ts - 9};
        planes.add(i);

        i = new int[]{s - 1, 1, ts - 9, s - 2, 1, ts - 8, s - 6, 1, ts - 10, s - 5, 1, ts - 11};
        planes.add(i);

        i = new int[]{s - 2, 1, ts - 0, s - 3, 1, ts - 5, s - 7, 1, ts - 9, s - 6, 1, ts - 4};
        planes.add(i);


        //build board nr 2
        vertices.add(v1.add(n1.normalize().scale(boardwidth / 2)).add(new Vector3D(0, boardHeight2, 0)));
        vertices.add(v2.add(n2.normalize().scale(boardwidth / 2)).add(new Vector3D(0, boardHeight2, 0)));
        vertices.add(v2.add(n2.normalize().scale(-1 * boardwidth / 2)).add(new Vector3D(0, boardHeight2, 0)));
        vertices.add(v1.add(n1.normalize().scale(-1 * boardwidth / 2)).add(new Vector3D(0, boardHeight2, 0)));

        vertices.add(v1.add(n1.normalize().scale(boardwidth / 2)).add(new Vector3D(0, boardHeight2 + boardSize, 0)));
        vertices.add(v2.add(n2.normalize().scale(boardwidth / 2)).add(new Vector3D(0, boardHeight2 + boardSize, 0)));
        vertices.add(v2.add(n2.normalize().scale(-1 * boardwidth / 2)).add(new Vector3D(0, boardHeight2 + boardSize, 0)));
        vertices.add(v1.add(n1.normalize().scale(-1 * boardwidth / 2)).add(new Vector3D(0, boardHeight2 + boardSize, 0)));

        s = vertices.size();

        i = new int[]{s - 3, 1, ts - 9, s - 2, 1, ts - 4, s - 1, 1, ts - 3, s - 0, 1, ts - 8};
        planes.add(i);

        i = new int[]{s - 4, 1, ts - 8, s - 5, 1, ts - 3, s - 6, 1, ts - 4, s - 7, 1, ts - 9};
        planes.add(i);

        i = new int[]{s - 3, 1, ts - 9, s - 0, 1, ts - 8, s - 4, 1, ts - 10, s - 7, 1, ts - 11};
        planes.add(i);

        i = new int[]{s - 0, 1, ts - 5, s - 1, 1, ts - 0, s - 5, 1, ts - 4, s - 4, 1, ts - 9};
        planes.add(i);

        i = new int[]{s - 1, 1, ts - 9, s - 2, 1, ts - 8, s - 6, 1, ts - 10, s - 5, 1, ts - 11};
        planes.add(i);

        i = new int[]{s - 2, 1, ts - 0, s - 3, 1, ts - 5, s - 7, 1, ts - 9, s - 6, 1, ts - 4};
        planes.add(i);
    }

    /**
     * Add vertices to the passed vertices-arraylists to create a model of the ground
     *
     * @param vertices          arraylist of vertices for the obj file
     * @param normalvertices    arraylist of normalvertices for the obj file
     * @param texturevertices   arraylist of texturevertices for the obj file
     * @param planes            arraylist of planes for the obj-file
     * @param surroundingPoints points surrounding all points
     * @param vectors           all points
     * @param flatGround        should the ground be flat or have hills?
     */
    public static void buildFloor(ArrayList<Vector3D> vertices, ArrayList<Vector3D> normalvertices, ArrayList<double[]> texturevertices, ArrayList<int[]> planes, Vector3D[] surroundingPoints, Vector3D[] vectors, boolean flatGround)
    {
        double extraSpace = 5000;
        double textureconstant = 10;
        ArrayList<int[]> floorplanes = new ArrayList<>();

        Vector3D avg = getAverage(vectors);

        if (!flatGround)
        {
            ArrayList<Vector3D> floorPoints = new ArrayList<>();

            for (int i = 0; i < surroundingPoints.length; i++)
            {
                Vector3D ni = surroundingPoints[i].sub(avg);
                ni.y = 0;
                ni.normalize();
                floorPoints.add(surroundingPoints[i].add(ni.scale(extraSpace)));
                floorPoints.add(surroundingPoints[i]);
            }

            floorPoints.addAll(Arrays.asList(vectors));
            Vector3D[] points = new Vector3D[floorPoints.size()];
            points = floorPoints.toArray(points);

            sortVectorsAfterX(points);

            triangulate(vertices, texturevertices, floorplanes, points, textureconstant);
        } else
        {
            for (int i = 0; i < surroundingPoints.length; i++)
            {
                int j = i + 1;
                if (j == surroundingPoints.length)
                    j = 0;

                avg = getAverage(vectors);

                Vector3D ni = surroundingPoints[i].sub(avg);
                ni.y = 0;
                ni = ni.normalize();

                Vector3D nj = surroundingPoints[j].sub(avg);
                nj.y = 0;
                nj = nj.normalize();

                avg = vectors[0];

                vertices.add(avg);
                vertices.add(surroundingPoints[i]);
                vertices.add(surroundingPoints[j]);
                vertices.add(surroundingPoints[i].add(ni.scale(extraSpace)));
                vertices.add(surroundingPoints[j].add(nj.scale(extraSpace)));

                texturevertices.add(new double[]{avg.x / textureconstant, avg.z / textureconstant});
                texturevertices.add(new double[]{surroundingPoints[i].x / textureconstant, surroundingPoints[i].z / textureconstant});
                texturevertices.add(new double[]{surroundingPoints[j].x / textureconstant, surroundingPoints[j].z / textureconstant});
                texturevertices.add(new double[]{surroundingPoints[i].add(ni.scale(extraSpace)).x / textureconstant, surroundingPoints[i].add(ni.scale(extraSpace)).z / textureconstant});
                texturevertices.add(new double[]{surroundingPoints[j].add(nj.scale(extraSpace)).x / textureconstant, surroundingPoints[j].add(nj.scale(extraSpace)).z / textureconstant});


                int s = vertices.size();
                int ts = texturevertices.size();

                floorplanes.add(new int[]{s - 1, 1, ts - 1, s - 2, 1, ts - 2, s - 3, 1, ts - 3});
                floorplanes.add(new int[]{s - 1, 1, ts - 1, s - 0, 1, ts - 0, s - 2, 1, ts - 2});
                floorplanes.add(new int[]{s - 3, 1, ts - 3, s - 2, 1, ts - 2, s - 4, 1, ts - 4});
            }
        }

        planes.addAll(floorplanes);
    }

    /**
     * @param vertices        arraylist of vertices for the obj file
     * @param texturevertices arraylist of texturevertices for the obj file
     * @param floorplanes     arraylist of floor-planes for the obj-file
     * @param points          all points
     * @param textureconstant constant to calculate texture vertices
     */
    private static void triangulate(ArrayList<Vector3D> vertices, ArrayList<double[]> texturevertices, ArrayList<int[]> floorplanes, Vector3D[] points, double textureconstant)
    {
        Triangulator t = new Triangulator();
        Vector3D[][] r = t.triangulate(points);

        for (int i = 0; i < r.length; i++)
        {
            vertices.add(r[i][0]);
            vertices.add(r[i][1]);
            vertices.add(r[i][2]);

            texturevertices.add(new double[]{r[i][0].x / textureconstant, r[i][0].z / textureconstant});
            texturevertices.add(new double[]{r[i][1].x / textureconstant, r[i][1].z / textureconstant});
            texturevertices.add(new double[]{r[i][2].x / textureconstant, r[i][2].z / textureconstant});

            int s = vertices.size();
            int ts = texturevertices.size();

            floorplanes.add(new int[]{s - 0, 1, ts - 0, s - 1, 1, ts - 1, s - 2, 1, ts - 2});
        }
    }

    /**
     * Parse the String to be written to a obj-file from the params
     *
     * @param vertices        arraylist of vertices for the obj file
     * @param normalvertices  arraylist of normalvertices for the obj file
     * @param texturevertices arraylist of texturevertices for the obj file
     * @param planes          arraylist of planes for the obj-file
     * @param floorStartIndex which planes are part of the floor
     * @param name            name of the obj-file without the .obj suffix
     * @return
     */
    private static String makeString(Vector3D[] vertices, Vector3D[] normalvertices, double[][] texturevertices, int[][] planes, int floorStartIndex, String name)
    {
        String s = "mtllib env.mtl\n";

        Vector3D v;
        double[] tv;

        for (int i = 0; i < vertices.length; i++)//add vertices
        {
            v = vertices[i];
            String ts = "v " + v.x + " " + v.y + " " + -1 * v.z/*transform to right-handed coordinate system*/ + "\n";
            s += ts;
        }

        s += "vn 0 0 0\n"; //add normalvertex

        for (int i = 0; i < texturevertices.length; i++)//add texture-verices
        {
            tv = texturevertices[i];
            String ts = "vt " + tv[0] + " " + tv[1] + "\n";
            s += ts;
        }

        int[] p;

        s += "usemtl fence" + "\n";//fence planes from here

        for (int i = 0; i < planes.length; i++)//add planes
        {
            if (i == floorStartIndex)
            {
                s += "usemtl floor\n";//floor planes from here
            }

            //render front-side of every plane
            p = planes[i];
            String ts = "f ";

            for (int j = 0; j < p.length; j += 3)
            {
                ts += p[j] + "/" + p[j + 2] + "/" + p[j + 1] + " ";
            }
            ts += "\n";
            s += ts;



            /*
            //render back-side of every plane
            p = planes[i];
            ts = "f ";

            for (int j = p.length-1; j >= 2; j -= 3)
            {
                ts += p[j - 2] + "/" + p[j] + "/" + p[j - 1] + " ";
            }
            ts += "\n";
            s += ts;*/
        }

        return s;
    }

    /**
     * Create a .obj-File and write a String to it
     *
     * @param s    String to be written to the file
     * @param name name of the file without the .obj suffix
     */
    private static int createFile(String s, String name)
    {
        File f = new File(Environment.getExternalStorageDirectory() + "/ViSensor/Obj/", name + ".obj");

        try
        {
            f.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(f, false /*append*/));
            writer.write(s);
            writer.close();
        } catch (IOException e)
        {
            e.printStackTrace();
            return -1;//failure
        }

        return 1;//success

    }
}