package cn.edu.pku.sei.plde.conqueroverfitting.localization.metric;

/**
 * Created by spirals on 24/07/15.
 */
public interface Metric {

    double value(int ef, int ep, int nf, int np);
}
