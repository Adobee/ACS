package cn.edu.pku.sei.plde.conqueroverfitting.localization.metric;

/**
 * Created by spirals on 24/07/15.
 */
public class Sokal implements Metric {

    public double value(int ef, int ep, int nf, int np) {
        return 2 * (ef + np) / ( 2 * (ef + np) + nf + ep);
    }
}
