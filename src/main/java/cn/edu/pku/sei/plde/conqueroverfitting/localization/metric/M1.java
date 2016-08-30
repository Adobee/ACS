package cn.edu.pku.sei.plde.conqueroverfitting.localization.metric;

/**
 * Created by spirals on 24/07/15.
 */
public class M1 implements Metric {

    public double value(int ef, int ep, int nf, int np) {
        return (ef + np) / ((double) (nf + ep));
    }
}
