package by.bsu.fpmi.dnfp.main.util;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularMatrixException;

public final class AlgebraUtils {
    private AlgebraUtils() {
        throw new AssertionError();
    }

    public static double[] calcResult(double[][] A, double[] l, double[][] PreF, double[] v, double[] noSupportL,
                                      double[] intensities) {
        double[] rightPart = getRightPart(l, PreF, v, noSupportL, intensities);
        for (int i = 0; i < A.length; i++) {
            for (int j = 0; j < A[i].length; j++) {
                System.out.print(A[i][j] + " ");
            }
            System.out.println(rightPart[i]);
        }

        try {
            DecompositionSolver solver = new LUDecomposition(MatrixUtils.createRealMatrix(A)).getSolver();
            RealVector vector = solver.solve(new ArrayRealVector(rightPart));
            return vector.toArray();
        } catch (SingularMatrixException e) {
            System.out.println("SingularMatrixException happen - result is zero vector.");
            return new double[A.length];
        }
    }

    private static double[] getRightPart(double[] l, double[][] PreF, double[] v, double[] noSupportL,
                                         double[] intensities) {
        double[] result = new double[l.length];
        for (int i = 0; i < l.length; i++) {
            result[i] = calcRightElement(l, PreF, v, noSupportL, intensities, i);
        }
        return result;
    }

    private static double calcRightElement(double[] l, double[][] PreF, double[] v, double[] noSupportL,
                                           double[] intensities, int row) {
        double result = 0;
        for (int i = 0; i < PreF[row].length; i++) {
            result += PreF[row][i] * v[i];
        }
        return l[row] - result - noSupportL[row];// - intensities[row];
    }
}
