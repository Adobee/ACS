package cn.edu.pku.sei.plde.ACS.localization.metric;

/**
 * Created by localization on 16/1/25.
 */
public class Fagge implements Metric {

    public double value(int ef, int ep, int nf, int np) {
        // ef * (1 + 1 / (2* ep + ef))
        return (ef ) / (ef + nf );
    }
}
