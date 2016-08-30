package cn.edu.pku.sei.plde.conqueroverfitting.localization.metric;

/**
 * Created by spirals on 24/07/15.
 */
public class Dice implements Metric {

    public double value(int ef, int ep, int nf, int np) {
        // ef / float(ef + ep + nf)
        return  2*ef / ((double) (ef + (ep + nf)));
    }
}
