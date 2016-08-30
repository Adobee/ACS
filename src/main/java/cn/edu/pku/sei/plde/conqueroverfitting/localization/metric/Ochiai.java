package cn.edu.pku.sei.plde.conqueroverfitting.localization.metric;

/**
 * Created by spirals on 24/07/15.
 */
public class Ochiai implements Metric {

    public double value(int ef, int ep, int nf, int np) {
        //if (ef == 1)
        return ef/Math.sqrt((ef+ep)*(ef+nf))*10;
            //return (double) ef / ((double) ep + (double)np ) * 10000;
        //else
        //    return 0;

    }
}
