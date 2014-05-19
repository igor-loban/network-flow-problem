package by.bsu.fpmi.dnfp.main.util;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealVector;

public final class AlgebraUtils {
    private AlgebraUtils() {
        throw new AssertionError();
    }

    public static double[] calcResult(double[][] A, double[] l, double[][] PreF, double[] v) {
        DecompositionSolver solver = new LUDecomposition(MatrixUtils.createRealMatrix(A)).getSolver();
        RealVector vector = solver.solve(new ArrayRealVector(getRightPart(l, PreF, v)));
        return vector.toArray();
    }

    private static double[] gaussianElimination(double[][] B) {
        int n = B.length;
        double[] result = new double[n];

        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                double c = B[j][i] / B[i][i];
                for (int k = i + 1; k < B[j].length; k++) {
                    B[j][k] -= B[i][k] * c;
                }
            }
        }

        result[n - 1] = B[n - 1][n] / B[n - 1][n - 1];
        for (int i = n - 2; i >= 0; i--) {
            for (int j = i + 1; j < n; j++) {
                B[i][n] -= result[j] * B[i][j];
            }
            result[i] = B[i][n] / B[i][i];
        }

        return result;
    }

    private static double[] getRightPart(double[] l, double[][] PreF, double[] v) {
        double[] result = new double[l.length];
        for (int i = 0; i < l.length; i++) {
            result[i] = calcRightElement(l, PreF, v, i);
        }
        return result;
    }

    private static double calcRightElement(double[] l, double[][] PreF, double[] v, int row) {
        double result = 0;
        for (int i = 0; i < PreF[row].length; i++) {
            result += PreF[row][i] * v[i];
        }
        return l[row] - result;
    }
}
