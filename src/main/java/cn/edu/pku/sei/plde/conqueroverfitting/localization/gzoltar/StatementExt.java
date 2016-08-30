package cn.edu.pku.sei.plde.conqueroverfitting.localization.gzoltar;

import com.gzoltar.core.components.Component;
import com.gzoltar.core.components.Statement;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.metric.Metric;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.metric.Ochiai;
import com.gzoltar.core.instr.testing.TestResult;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by spirals on 24/07/15.
 */
public class StatementExt extends Statement  {
    private int ep;
    private int ef;
    private int np;
    private int nf;
    private Metric defaultMetric;
    private float suspiciousWeight = 1;
    private List<String> tests = new ArrayList<String>();
    private List<String> failTests = new ArrayList<>();

    public StatementExt(Component c, int lN) {
        super(c, lN);
    }
    public StatementExt(Statement s) {
        this(s, new Ochiai());
    }

    public StatementExt(Statement s, Metric defaultMetric) {
        super(s.getParent(), s.getLineNumber());
        this.defaultMetric = defaultMetric;
        this.setLabel(s.getLabel());
        this.setSuspiciousness(s.getSuspiciousness());
        this.setLineNumber(s.getLineNumber());
    }

    public void addFailTest(String test){
        failTests.add(test);
    }
    public void addTest(String test){
        tests.add(test);
    }

    public List<String> getTests(){
        return tests;
    }
    public List<String> getFailTests(){
        return failTests;
    }


    public int getEf() {
        return ef;
    }

    public int getEp() {
        return ep;
    }

    public int getNf() {
        return nf;
    }

    public int getNp() {
        return np;
    }

    public void setEf(int ef) {
        this.ef = ef;
    }

    public void setEp(int ep) {
        this.ep = ep;
    }

    public void setNf(int nf) {
        this.nf = nf;
    }

    public void setNp(int np) {
        this.np = np;
    }

    @Override
    public double getSuspiciousness() {
        if (this.getLabel().contains("getOffsetFromLocal")){
            return getSuspiciousness(this.defaultMetric)*10;
        }
        return getSuspiciousness(this.defaultMetric);
    }

    public void setSuspiciousWeight(float weight){
        suspiciousWeight = weight;
    }


    public double getSuspiciousness(Metric metric) {
        if (getLabel().contains("(") && getLabel().contains(")")){
            if (StringUtils.isNumeric(getLabel().substring(getLabel().lastIndexOf("(")+1,getLabel().lastIndexOf(")")))){
                return metric.value(ef, ep, nf, np)/4*suspiciousWeight;
            }
        }

        return metric.value(ef, ep, nf, np)*suspiciousWeight;
    }

    @Override
    public int compareTo(Component s) {
        if(s instanceof StatementExt) {
            return (int) Math.floor(s.getSuspiciousness() - getSuspiciousness());
        }
        return super.compareTo(s);
    }

    @Override
    public String toString() {
        return super.getName() + ":" + getLineNumber() + " " + getSuspiciousness();
    }
}
