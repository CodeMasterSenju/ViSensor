/* Copyright 2017 Artur Baltabayev, Jean-Josef Büschel, Martin Kern, Gabriel Scheibler
 *
 * This file is part of ViSensor.
 *
 * ViSensor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ViSensor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ViSensor.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.artur.softwareproject;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import static java.lang.Math.PI;
import static java.lang.Math.acos;


/**
 * Created by gabriel on 11.05.17.
 * This class provides a method to create a 3D model from location data.
 */
class ModelConstructor
{
    private static final String TAG = ModelConstructor.class.getSimpleName();

    /**
     * Create A 3D-Model of a Room surrounding the passed coordinates
     *
     * @param coordinates Array of coordinates that should be surrounded by the 3D-Model
     * @param name        name of the .obj file without the obj suffix
     * @param flatGround  should the ground be flat or have hills?
     * @return was the .obj file successfully created?
     */


    static boolean createModel(ArrayList<double[]> coordinates, String name, boolean flatGround)
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
    private static Vector3D[] translateToVectors(ArrayList<double[]> coordinates, boolean flatGround)
    {
        int size = coordinates.size();

        Vector3D[] vectors = new Vector3D[size];

        //Random r = new Random();

        double maxY = Double.MIN_VALUE;

        for (int i = 0; i < size; i++)
        {
            double x = coordinates.get(i)[0];
            double y = coordinates.get(i)[2];
            double z = coordinates.get(i)[1];

            //y = r.nextDouble() * -5; //Test für die Höhenkoordinate

            Vector3D v = new Vector3D(x, y, z);
            vectors[i] = v;

            if (y > maxY)
                maxY = y;
        }

        Vector3D s = new Vector3D(0, maxY, 0);


        // Adjust vertical position of model.
        for (int i = 0; i < size; i++)
        {
            //vectors[i] = vectors[i].sub(s);
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

        ArrayList<Vector3D> outerPoints = new ArrayList<>(Arrays.asList(initialOuterPoints));

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

                for (Vector3D k : vectors) //find point on the outside of the OuterPoint graph
                {
                    if (k.dot(n) > max.dot(n) && k.dot(e) > v1.dot(e) &&
                            k.dot(e) < v2.dot(e))
                    {
                        max = k;
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

            for (Vector3D v : vectors)
            {
                if (v.x < xMin)
                    xMin = v.x;
                if (v.x > xMax)
                    xMax = v.x;
                if (v.z < zMin)
                    zMin = v.z;
                if (v.z > zMax)
                    zMax = v.z;
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


        for (Vector3D v : vectors)
        {
            boolean isouterpoint = false;

            boolean temp = true;

            Vector3D a = v.sub(avg);
            a.y = 0;
            double al = a.dot(a);

            for (Vector3D w : vectors)
            {
                Vector3D b = w.sub(avg);
                b.y = 0;
                if (w.sub(avg).dot(a) > al)
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
                    if (outerPoints.get(j).x == v.x && outerPoints.get(j).y == v.y && outerPoints.get(j).z == v.z)
                    {
                        isouterpoint = false;
                    }
                }
            }

            if (isouterpoint)
            {
                outerPoints.add(v);
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



        Vector3D v1;
        Vector3D v2;
        Vector3D e;
        Vector3D n;
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

        for (Vector3D v : vectors)
        {
            avg = avg.add(v);
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

        for (Vector3D i : v)
        {
            deg = acos(i.sub(avg).normalize().x);
            if (i.sub(avg).z < 0) deg = 2 * PI - deg;
            i.comp = deg;
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
     * @param v set of vectors
     */
    private static void sortVectorsAfterX(Vector3D[] v)
    {
        for (Vector3D i : v)
        {
            i.comp = i.x;
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
     * @return                  A String that represents a 3D model in wavefront format.
     */
    private static String generateString(Vector3D[] surroundingPoints, String name, Vector3D[] vectors, boolean flatGround)
    {
        ArrayList<Vector3D> vertices = new ArrayList<>();
        ArrayList<Vector3D> normalVertices = new ArrayList<>();
        ArrayList<double[]> textureVertices = new ArrayList<>();
        ArrayList<int[]> planes = new ArrayList<>();

        Vector3D avg = getAverage(surroundingPoints);

        for (int i = 0; i < surroundingPoints.length; i++)
        {
            int j = i + 1;
            if (j == surroundingPoints.length)
                j = 0;

            buildFence(vertices, normalVertices, textureVertices, planes, surroundingPoints[i], surroundingPoints[j], avg);
        }

        int i = planes.size();

        buildFloor(vertices, normalVertices, textureVertices, planes, surroundingPoints, vectors, flatGround);

        Vector3D[] va = new Vector3D[vertices.size()];
        va = vertices.toArray(va);

        Vector3D[] nva = new Vector3D[normalVertices.size()];
        nva = normalVertices.toArray(nva);

        double[][] tva = new double[textureVertices.size()][];
        tva = textureVertices.toArray(tva);

        int[][] pa = new int[planes.size()][];
        pa = planes.toArray(pa);

        return makeString(va, nva, tva, pa, i, name);
    }

    /**
     * Add vertices to the passed vertices-arraylists to create a fence model between two points
     *
     * @param vertices        ArrayList of vertices for the obj file
     * @param normalVertices  ArrayList of normalVertices for the obj file
     * @param textureVertices ArrayList of textureVertices for the obj file
     * @param planes          ArrayList of planes for the obj-file
     * @param v1              point 1
     * @param v2              point 2
     * @param avg             Average Vector of Vectors to be surrounded by the fences
     */
    private static void buildFence(ArrayList<Vector3D> vertices, ArrayList<Vector3D> normalVertices, ArrayList<double[]> textureVertices, ArrayList<int[]> planes, Vector3D v1, Vector3D v2, Vector3D avg)
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

        buildFencePost(vertices, normalVertices, textureVertices, planes, v1, n1, nn1);
        buildFenceBoards(vertices, normalVertices, textureVertices, planes, v1, n1, v2, n2);
    }

    /**
     * Add vertices to the passed vertices-ArrayLists to create a fencepost model at a passed point
     *
     * @param vertices        ArrayList of vertices for the obj file
     * @param normalVertices  ArrayList of normalVertices for the obj file
     * @param textureVertices ArrayList of textureVertices for the obj file
     * @param planes          ArrayList of planes for the obj-file
     * @param v1              point
     * @param n1              directional vector inside
     * @param nn1             directional vector to the left
     */
    private static void buildFencePost(ArrayList<Vector3D> vertices, ArrayList<Vector3D> normalVertices, ArrayList<double[]> textureVertices, ArrayList<int[]> planes, Vector3D v1, Vector3D n1, Vector3D nn1)
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

        textureVertices.add(new double[]{0, 0});      //-9
        textureVertices.add(new double[]{0, 0.25});   //-8
        textureVertices.add(new double[]{0, 0.5});    //-7
        textureVertices.add(new double[]{0, 0.75});   //-6
        textureVertices.add(new double[]{0, 1});      //-5
        textureVertices.add(new double[]{1, 0});      //-4
        textureVertices.add(new double[]{1, 0.25});   //-3
        textureVertices.add(new double[]{1, 0.5});    //-2
        textureVertices.add(new double[]{1, 0.75});   //-1
        textureVertices.add(new double[]{1, 1});      //-0

        int s = vertices.size();
        int ts = textureVertices.size();
        int ns = normalVertices.size();
        int[] i;

        i = new int[]{s    , 1, ts - 9, s - 1, 1, ts - 5, s - 2, 1, ts    , s - 3, 1, ts - 4};
        planes.add(i);

        i = new int[]{s - 7, 1, ts - 9, s - 6, 1, ts - 5, s - 5, 1, ts    , s - 4, 1, ts - 4};
        planes.add(i);

        i = new int[]{s - 7, 1, ts - 9, s - 4, 1, ts - 8, s    , 1, ts - 3, s - 3, 1, ts - 4};
        planes.add(i);

        i = new int[]{s - 4, 1, ts - 8, s - 5, 1, ts - 7, s - 1, 1, ts - 2, s    , 1, ts - 3};
        planes.add(i);

        i = new int[]{s - 5, 1, ts - 7, s - 6, 1, ts - 6, s - 2, 1, ts - 1, s - 1, 1, ts - 2};
        planes.add(i);

        i = new int[]{s - 6, 1, ts - 6, s - 7, 1, ts - 5, s - 3, 1, ts    , s - 2, 1, ts - 1};
        planes.add(i);
    }

    /**
     * Add vertices to the passed vertices-ArrayLists to create fence-boards between two points
     *
     * @param vertices        ArrayList of vertices for the obj file
     * @param normalVertices  ArrayList of normalVertices for the obj file
     * @param textureVertices ArrayList of textureVertices for the obj file
     * @param planes          ArrayList of planes for the obj-file
     * @param v1              point 1
     * @param v2              point 2
     * @param n1              directional vector inside from v1
     * @param n2              directional vector inside from v1
     */
    private static void buildFenceBoards(ArrayList<Vector3D> vertices,
                                         ArrayList<Vector3D> normalVertices,
                                         ArrayList<double[]> textureVertices,
                                         ArrayList<int[]> planes,
                                         Vector3D v1,
                                         Vector3D n1,
                                         Vector3D v2,
                                         Vector3D n2)
    {
        double boardSize = 0.3;
        double boardWidth = 0.05;
        double boardHeight1 = 0.3;
        double boardHeight2 = 0.65;

        //build board nr 1
        vertices.add(v1.add(n1.normalize().scale(boardWidth / 2)).add(new Vector3D(0, boardHeight1, 0)));
        vertices.add(v2.add(n2.normalize().scale(boardWidth / 2)).add(new Vector3D(0, boardHeight1, 0)));
        vertices.add(v2.add(n2.normalize().scale(-1 * boardWidth / 2)).add(new Vector3D(0, boardHeight1, 0)));
        vertices.add(v1.add(n1.normalize().scale(-1 * boardWidth / 2)).add(new Vector3D(0, boardHeight1, 0)));

        vertices.add(v1.add(n1.normalize().scale(boardWidth / 2)).add(new Vector3D(0, boardHeight1 + boardSize, 0)));
        vertices.add(v2.add(n2.normalize().scale(boardWidth / 2)).add(new Vector3D(0, boardHeight1 + boardSize, 0)));
        vertices.add(v2.add(n2.normalize().scale(-1 * boardWidth / 2)).add(new Vector3D(0, boardHeight1 + boardSize, 0)));
        vertices.add(v1.add(n1.normalize().scale(-1 * boardWidth / 2)).add(new Vector3D(0, boardHeight1 + boardSize, 0)));

        double l = v1.sub(v2).getLength();

        textureVertices.add(new double[]{l, 0});       //-12
        textureVertices.add(new double[]{1, 0});       //-11
        textureVertices.add(new double[]{1, 0.05});    //-10
        textureVertices.add(new double[]{0, 0});       //-9
        textureVertices.add(new double[]{0, 0.05});    //-8
        textureVertices.add(new double[]{0, 0.35});    //-7
        textureVertices.add(new double[]{0, 0.4});     //-6
        textureVertices.add(new double[]{0, 0.7});     //-5
        textureVertices.add(new double[]{l, 0});       //-4
        textureVertices.add(new double[]{l, 0.05});    //-3
        textureVertices.add(new double[]{l, 0.35});    //-2
        textureVertices.add(new double[]{l, 0.4});     //-1
        textureVertices.add(new double[]{l, 0.7});     //-0

        int s = vertices.size();
        int ts = textureVertices.size();
        int ns = normalVertices.size();
        int[] i;

        i = new int[]{s - 3, 1, ts - 9, s - 2, 1, ts - 4, s - 1, 1, ts - 3, s    , 1, ts - 8};
        planes.add(i);

        i = new int[]{s - 4, 1, ts - 8, s - 5, 1, ts - 3, s - 6, 1, ts - 4, s - 7, 1, ts - 9};
        planes.add(i);

        i = new int[]{s - 3, 1, ts - 9, s    , 1, ts - 8, s - 4, 1, ts - 10, s - 7, 1, ts - 11};
        planes.add(i);

        i = new int[]{s    , 1, ts - 5, s - 1, 1, ts    , s - 5, 1, ts - 4, s - 4, 1, ts - 9};
        planes.add(i);

        i = new int[]{s - 1, 1, ts - 9, s - 2, 1, ts - 8, s - 6, 1, ts - 10, s - 5, 1, ts - 11};
        planes.add(i);

        i = new int[]{s - 2, 1, ts    , s - 3, 1, ts - 5, s - 7, 1, ts - 9, s - 6, 1, ts - 4};
        planes.add(i);


        //build board nr 2
        vertices.add(v1.add(n1.normalize().scale(boardWidth / 2)).add(new Vector3D(0, boardHeight2, 0)));
        vertices.add(v2.add(n2.normalize().scale(boardWidth / 2)).add(new Vector3D(0, boardHeight2, 0)));
        vertices.add(v2.add(n2.normalize().scale(-1 * boardWidth / 2)).add(new Vector3D(0, boardHeight2, 0)));
        vertices.add(v1.add(n1.normalize().scale(-1 * boardWidth / 2)).add(new Vector3D(0, boardHeight2, 0)));

        vertices.add(v1.add(n1.normalize().scale(boardWidth / 2)).add(new Vector3D(0, boardHeight2 + boardSize, 0)));
        vertices.add(v2.add(n2.normalize().scale(boardWidth / 2)).add(new Vector3D(0, boardHeight2 + boardSize, 0)));
        vertices.add(v2.add(n2.normalize().scale(-1 * boardWidth / 2)).add(new Vector3D(0, boardHeight2 + boardSize, 0)));
        vertices.add(v1.add(n1.normalize().scale(-1 * boardWidth / 2)).add(new Vector3D(0, boardHeight2 + boardSize, 0)));

        s = vertices.size();

        i = new int[]{s - 3, 1, ts - 9, s - 2, 1, ts - 4, s - 1, 1, ts - 3,  s    , 1, ts - 8};
        planes.add(i);

        i = new int[]{s - 4, 1, ts - 8, s - 5, 1, ts - 3, s - 6, 1, ts - 4,  s - 7, 1, ts - 9};
        planes.add(i);

        i = new int[]{s - 3, 1, ts - 9, s    , 1, ts - 8, s - 4, 1, ts - 10, s - 7, 1, ts - 11};
        planes.add(i);

        i = new int[]{s    , 1, ts - 5, s - 1, 1, ts    , s - 5, 1, ts - 4,  s - 4, 1, ts - 9};
        planes.add(i);

        i = new int[]{s - 1, 1, ts - 9, s - 2, 1, ts - 8, s - 6, 1, ts - 10, s - 5, 1, ts - 11};
        planes.add(i);

        i = new int[]{s - 2, 1, ts    , s - 3, 1, ts - 5, s - 7, 1, ts - 9,  s - 6, 1, ts - 4};
        planes.add(i);
    }

    /**
     * Add vertices to the passed vertices-ArrayLists to create a model of the ground
     *
     * @param vertices          ArrayList of vertices for the obj file
     * @param normalVertices    ArrayList of normalVertices for the obj file
     * @param textureVertices   ArrayList of textureVertices for the obj file
     * @param planes            ArrayList of planes for the obj-file
     * @param surroundingPoints points surrounding all points
     * @param vectors           all points
     * @param flatGround        should the ground be flat or have hills?
     */
    private static void buildFloor(ArrayList<Vector3D> vertices,
                                   ArrayList<Vector3D> normalVertices,
                                   ArrayList<double[]> textureVertices,
                                   ArrayList<int[]> planes,
                                   Vector3D[] surroundingPoints,
                                   Vector3D[] vectors,
                                   boolean flatGround)
    {
      
        double extraSpace = 2000;
        double extraSpaceNear = 10;
        double textureConstant = 2;
        ArrayList<int[]> floorPlanes = new ArrayList<>();

        Vector3D avg = getAverage(vectors);

        if (!flatGround)
        {
            ArrayList<Vector3D> floorPoints = new ArrayList<>();

            for (Vector3D sur : surroundingPoints)
            {
                Vector3D ni = sur.sub(avg);
                ni.y = 0;
                ni.normalize();
                floorPoints.add(sur.add(ni.scale(extraSpace)));
                floorPoints.add(sur.add(ni.scale(extraSpaceNear)));
                floorPoints.add(sur);
            }

            floorPoints.addAll(Arrays.asList(vectors));
            Vector3D[] points = new Vector3D[floorPoints.size()];
            points = floorPoints.toArray(points);

            sortVectorsAfterX(points);

            triangulate(vertices, textureVertices, floorPlanes, points, textureConstant);
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

                textureVertices.add(new double[]{avg.x / textureConstant, avg.z / textureConstant});
                textureVertices.add(new double[]{surroundingPoints[i].x / textureConstant, surroundingPoints[i].z / textureConstant});
                textureVertices.add(new double[]{surroundingPoints[j].x / textureConstant, surroundingPoints[j].z / textureConstant});
                textureVertices.add(new double[]{surroundingPoints[i].add(ni.scale(extraSpace)).x / textureConstant, surroundingPoints[i].add(ni.scale(extraSpace)).z / textureConstant});
                textureVertices.add(new double[]{surroundingPoints[j].add(nj.scale(extraSpace)).x / textureConstant, surroundingPoints[j].add(nj.scale(extraSpace)).z / textureConstant});


                int s = vertices.size();
                int ts = textureVertices.size();

                floorPlanes.add(new int[]{s - 1, 1, ts - 1, s - 2, 1, ts - 2, s - 3, 1, ts - 3});
                floorPlanes.add(new int[]{s - 1, 1, ts - 1, s    , 1, ts    , s - 2, 1, ts - 2});
                floorPlanes.add(new int[]{s - 3, 1, ts - 3, s - 2, 1, ts - 2, s - 4, 1, ts - 4});
            }
        }

        planes.addAll(floorPlanes);
    }

    /**
     * @param vertices        ArrayList of vertices for the obj file
     * @param textureVertices ArrayList of textureVertices for the obj file
     * @param floorPlanes     ArrayList of floor-planes for the obj-file
     * @param points          all points
     * @param textureConstant constant to calculate texture vertices
     */
    private static void triangulate(ArrayList<Vector3D> vertices,
                                    ArrayList<double[]> textureVertices,
                                    ArrayList<int[]> floorPlanes,
                                    Vector3D[] points,
                                    double textureConstant)
    {
        Triangulator t = new Triangulator();
        Vector3D[][] r = t.triangulate(points);

        for (Vector3D[] v : r)
        {
            vertices.add(v[0]);
            vertices.add(v[1]);
            vertices.add(v[2]);

            textureVertices.add(new double[]{v[0].x / textureConstant, v[0].z / textureConstant});
            textureVertices.add(new double[]{v[1].x / textureConstant, v[1].z / textureConstant});
            textureVertices.add(new double[]{v[2].x / textureConstant, v[2].z / textureConstant});

            int s = vertices.size();
            int ts = textureVertices.size();

            floorPlanes.add(new int[]{s, 1, ts, s - 1, 1, ts - 1, s - 2, 1, ts - 2});
        }
    }

    /**
     * Parse the String to be written to a obj-file from the params
     *
     * @param vertices        ArrayList of vertices for the obj file
     * @param normalVertices  ArrayList of normalVertices for the obj file
     * @param textureVertices ArrayList of textureVertices for the obj file
     * @param planes          ArrayList of planes for the obj-file
     * @param floorStartIndex which planes are part of the floor
     * @param name            name of the obj-file without the .obj suffix
     * @return                A String that represents a 3D model in wavefront format.
     */
    private static String makeString(Vector3D[] vertices, Vector3D[] normalVertices, double[][] textureVertices, int[][] planes, int floorStartIndex, String name)
    {
        String s = "mtllib env.mtl\n";

        Vector3D v;
        double[] tv;

        for (Vector3D u : vertices)//add vertices
        {
            v = u;
            String ts = "v " + v.x + " " + v.y + " " + -1 * v.z/*transform to right-handed coordinate system*/ + "\n";
            s += ts;
        }

        s += "vn 0 0 0\n"; //add normalVertex

        for (double[] vt : textureVertices)//add texture-vertices
        {
            tv = vt;
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




            //render back-side of every plane
            p = planes[i];
            ts = "f ";

            for (int j = p.length-1; j >= 2; j -= 3)
            {
                ts += p[j - 2] + "/" + p[j] + "/" + p[j - 1] + " ";
            }
            ts += "\n";
            s += ts;
        }

        return s;
    }

    /**
     * Create a .obj-File and write a String to it
     *
     * @param s    String to be written to the file
     * @param name name of the file without the .obj suffix
     */
    private static boolean createFile(String s, String name)
    {
        File f = new File(Environment.getExternalStorageDirectory() + "/ViSensor/Obj/", name + ".obj");

        try
        {
            if (!f.createNewFile())
                Log.d(TAG, "Failed to create a new file.");

            BufferedWriter writer = new BufferedWriter(new FileWriter(f, false /*append*/));
            writer.write(s);
            writer.close();
        } catch (IOException e)
        {
            e.printStackTrace();
            return false;//failure
        }

        return true;//success

    }
}