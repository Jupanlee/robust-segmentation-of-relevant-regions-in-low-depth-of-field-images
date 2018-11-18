package basics.math;

import basics.Tools;
import java.io.PrintStream;

public class MMatrix
{
    private int width;
    private int height;
    private float[] linear;
    private float[][] table;

    public MMatrix(int width, int height)
    {
        this.width = width;
        this.height = height;
        this.linear = new float[width * height];

        for (int i = 0; i < width * height; i++) {
            this.linear[i] = 0.0F;
        }

        calculateTable();
    }

    public MMatrix(float stdValue, int widht, int height) {
        this.width = widht;
        this.height = height;

        this.linear = Tools.createArray(stdValue, this.width * this.height);
        calculateTable();
    }

    public MMatrix(float[] linear, int widht, int height) {
        this.linear = linear;
        this.width = widht;
        this.height = height;

        calculateTable();
    }

    private void calculateTable() {
        this.table = new float[this.width][this.height];

        int index = 0;
        for (int y = 0; y < this.height; y++)
            for (int x = 0; x < this.width; x++) {
                this.table[x][y] = this.linear[index];
                index++;
            }
    }

    private void calculateLinearValues()
    {
        this.linear = new float[this.width * this.height];
        int index = 0;
        for (int x = 0; x < this.width; x++)
            for (int y = 0; y < this.height; y++) {
                this.linear[index] = this.table[x][y];
                index++;
            }
    }

    public MMatrix(float[][] table)
    {
        this.table = table;
        this.width = table.length;
        this.height = table[0].length;

        calculateLinearValues();
    }

    public MMatrix(MMatrix m)
    {
        this.height = m.height;
        this.width = m.width;

        this.linear = new float[this.width * this.height];
        for (int i = 0; i < this.linear.length; i++) {
            this.linear[i] = m.linear[i];
        }

        calculateTable();
    }

    public boolean equals(MMatrix m)
    {
        if (m.linear.length != this.linear.length) {
            return false;
        }

        for (int i = 0; i < this.linear.length; i++) {
            if (this.linear[i] != m.linear[i]) {
                return false;
            }

        }

        return true;
    }

    public void print() {
        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                System.out.print(this.table[x][y] + " ");
            }

            System.out.println();
        }
    }

    public void rotate90()
    {
        float[][] rotatedMatrix = new float[this.height][this.width];

        for (int i = 0; i < this.height; i++) {
            for (int j = 0; j < this.width; j++) {
                rotatedMatrix[i][j] = this.table[(this.height - j - 1)][i];
            }

        }

        int tmp = this.width;
        this.width = this.height;
        this.height = tmp;

        this.table = rotatedMatrix;

        calculateLinearValues();
    }

    public void rotate90(int times)
    {
        for (int i = 0; i < times; i++)
            rotate90();
    }

    public float[] getLinearValues()
    {
        return this.linear;
    }

    public float[][] getTable() {
        return this.table;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }
}