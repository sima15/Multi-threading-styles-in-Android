package com.example.sima.mandelbrot.asynctask;

/**
 * Created by Sima on 12/3/2015.
 */
public class Extension {

//    static double[] Crb;
//    static double[] Cib;
static int count= 0;

    static void putLine(int y, byte[] line) {
        for (int xb = 0; xb < line.length; xb++) {
            line[xb] = (byte) getByte(xb * 8, y);
            System.out.println("Value of line[xb] is: "+String.valueOf(line[xb]));
        }
    }

    static int getByte(int x, int y) {
        int res = 0;
        for (int i = 0; i < 8; i += 2) {
            double Zr1 = MandelbrotAsynctask.Crb[x + i];
            double Zi1 = MandelbrotAsynctask.Cib[y];

            double Zr2 = MandelbrotAsynctask.Crb[x + i + 1];
            double Zi2 = MandelbrotAsynctask.Cib[y];

            int b = 0;
            int j = 49;
            do {
                double nZr1 = Zr1 * Zr1 - Zi1 * Zi1 + MandelbrotAsynctask.Crb[x + i];
                double nZi1 = Zr1 * Zi1 + Zr1 * Zi1 + MandelbrotAsynctask.Cib[y];
                Zr1 = nZr1;
                Zi1 = nZi1;

                double nZr2 = Zr2 * Zr2 - Zi2 * Zi2 + MandelbrotAsynctask.Crb[x + i + 1];
                double nZi2 = Zr2 * Zi2 + Zr2 * Zi2 + MandelbrotAsynctask.Cib[y];
                Zr2 = nZr2;
                Zi2 = nZi2;

                if (Zr1 * Zr1 + Zi1 * Zi1 > 4) {
                    b |= 2;
                    if (b == 3) break;
                }
                if (Zr2 * Zr2 + Zi2 * Zi2 > 4) {
                    b |= 1;
                    if (b == 3) break;
                }

                System.out.println("extension: "+ count + "running");
                count++;
            } while (--j > 0);
            res = (res << 2) + b;
            System.out.println("extension: "+ count + "running");
            count++;
        }
        return res ^ -1;
    }
}
