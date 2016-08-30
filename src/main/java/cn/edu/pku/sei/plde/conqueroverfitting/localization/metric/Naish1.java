package cn.edu.pku.sei.plde.conqueroverfitting.localization.metric;

/**
 * Created by spirals on 24/07/15.
 */
public class Naish1 implements Metric {

    public double value(int ef, int ep, int nf, int np) {
        // return np if ef == 0 else -1
        if(ef == 0)
            return np;
        return -1;
    }
}
