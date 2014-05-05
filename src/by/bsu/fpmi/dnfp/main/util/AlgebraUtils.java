package by.bsu.fpmi.dnfp.main.util;

public final class AlgebraUtils {
    private AlgebraUtils() {
        throw new AssertionError();
    }

    public static void main(String[] args) {
        double[][] A = { { 1, 2, 3 }, { -4, 5, -6 }, { 7, -8, 9 } };
        double[] l = { 1, 2, 3 };
        double[][] PreF = { { 1, 2 }, { -3, 4 }, { 5, -6 } };
        double[] v = { 1, -1 };
        double[] result = calcResult(A, l, PreF, v);
        // 7, 1, -3
        for (double value : result) {
            System.out.println(value);
        }
    }

    public static double[] calcResult(double[][] A, double[] l, double[][] PreF, double[] v) {
        double[][] B = getExpandMatrix(A, l, PreF, v);
        return gaussianElimination(B);
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

    private static double[][] getExpandMatrix(double[][] A, double[] l, double[][] PreF, double[] v) {
        double[][] B = new double[A.length][];
        for (int i = 0; i < A.length; i++) {
            B[i] = new double[A[i].length + 1];
            System.arraycopy(A[i], 0, B[i], 0, A[i].length);
            B[i][A[i].length] = calcRightElement(l, PreF, v, i);
        }
        return B;
    }

    private static double calcRightElement(double[] l, double[][] PreF, double[] v, int row) {
        double result = l[row];
        for (int i = 0; i < PreF[row].length; i++) {
            result += PreF[row][i] * v[i];
        }
        return result;
    }
}
