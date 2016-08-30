package cn.edu.pku.sei.plde.conqueroverfitting.localization.metric;

/**
 * Created by spirals on 24/07/15.
 */
public class Gp13 implements Metric {

    public double value(int ef, int ep, int nf, int np) {
        // ef * (1 + 1 / (2* ep + ef))
        return ef * (1 + 1 / (double) (2*ep + ef));
    }
}
