package cn.edu.pku.sei.plde.conqueroverfitting.localization.metric;

/**
 * Created by spirals on 24/07/15.
 */
public class Ample implements Metric {

    public double value(int ef, int ep, int nf, int np) {
        // abs((ef/float(ef+nf)) - (ep/float(ep+np)))
        return Math.abs((ef/((double) (ef + nf))) - (ep/((double) (ep + np))));
    }
}
