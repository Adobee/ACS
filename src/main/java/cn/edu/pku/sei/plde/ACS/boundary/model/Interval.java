package cn.edu.pku.sei.plde.ACS.boundary.model;

import cn.edu.pku.sei.plde.ACS.type.TypeEnum;
import cn.edu.pku.sei.plde.ACS.utils.MathUtils;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.internal.compiler.codegen.IntegerCache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by yjxxtd on 4/23/16.
 */

enum IntervalType{
    NumericInterval,BooleanInterval,ObjectInterval
}

class NumericBoundary{
    double boundary;
    boolean modifier;
    TypeEnum type = TypeEnum.DOUBLE;

    NumericBoundary(double boundary, boolean modifier){
        this.boundary = boundary;
        this.modifier = modifier;
    }

    NumericBoundary(double boundary, boolean modifier, TypeEnum type){
        this.boundary = boundary;
        this.modifier = modifier;
        this.type = type;
    }

    @Override
    public String toString(){
        return boundary+"["+(modifier?"+":"-")+"]";
    }

    @Override
    public boolean equals(Object obj){
        if (!(obj instanceof NumericBoundary))
            return false;
        NumericBoundary other = (NumericBoundary) obj;
        return (Double.compare(boundary, other.boundary) == 0 && modifier == other.modifier && type == other.type)
                || (!this.lessThat(other) && !this.greaterThat(other));
    }

    boolean lessThat(NumericBoundary another){
        if (boundary > another.boundary){
            return false;
        }
        if (boundary == another.boundary && modifier && !another.modifier){
            return false;
        }
        if (another.boundary == -Double.MAX_VALUE){
            return false;
        }
        if (type == TypeEnum.INT || type == TypeEnum.LONG || type == TypeEnum.SHORT){
            if (modifier && !another.modifier && Math.abs(boundary-another.boundary) == 1){
                return false;
            }
        }
        return true;
    }

    boolean greaterThat(NumericBoundary another){
        if (boundary < another.boundary){
            return false;
        }
        if (boundary == another.boundary && !modifier && another.modifier){
            return false;
        }
        if (boundary == -Double.MAX_VALUE){
            return false;
        }
        if (type == TypeEnum.INT || type == TypeEnum.LONG || type == TypeEnum.SHORT){
            if (!modifier && another.modifier){
                return false;
            }
        }
        return true;
    }

    static NumericBoundary max(NumericBoundary left, NumericBoundary right){
        return left.greaterThat(right) ? left : right;
    }

    static NumericBoundary max(Interval interval){
        return max(interval.left, interval.right);
    }

    static NumericBoundary min(NumericBoundary left, NumericBoundary right){
        return left.lessThat(right) ? left : right;
    }

    static NumericBoundary min(Interval interval){
        return min(interval.left, interval.right);
    }
}

public class Interval {
    protected NumericBoundary left;
    protected NumericBoundary right;
    protected Boolean booleanBoundary;
    protected String objectBoundary;
    private TypeEnum type = TypeEnum.DOUBLE;

    public double leftBoundary(){
        return left.boundary;
    }

    public double rightBoundary(){
        return right.boundary;
    }

    public boolean booleanBoundary(){
        return booleanBoundary;
    }

    public String objectBoundary(){
        return objectBoundary;
    }

    public boolean leftClose(){//true [,false (
        return !left.modifier;
    }

    public boolean rightClose(){//true ], false )
        return right.modifier;
    }

    public void setLeftBoundary(double number){
        left.boundary = number;
    }

    public void setRightBoundary(double number){
        right.boundary = number;
    }

    public void setLeftClose(boolean isClose){
        left.modifier = !isClose;
    }

    public void setRightClose(boolean isClose){
        right.modifier = isClose;
    }

    public IntervalType intervalType(){
        if (left != null && right != null){
            return IntervalType.NumericInterval;
        }
        if (objectBoundary != null){
            return IntervalType.ObjectInterval;
        }
        if (booleanBoundary != null){
            return IntervalType.BooleanInterval;
        }
        return null;
    }
    public Interval(double leftBoundary, double rightBoundary, boolean leftClose, boolean rightClose){
        this.left = new NumericBoundary(leftBoundary, !leftClose);
        this.right = new NumericBoundary(rightBoundary, rightClose);
    }

    public Interval(double leftBoundary, double rightBoundary, boolean leftClose, boolean rightClose, TypeEnum type){
        this.left = new NumericBoundary(leftBoundary, !leftClose, type);
        this.right = new NumericBoundary(rightBoundary, rightClose, type);
        this.type = type;
    }

    public Interval(String object){
        this.objectBoundary = object;
    }

    public Interval(boolean booleanBoundary){
        this.booleanBoundary = booleanBoundary;
    }

    public Interval(Interval interval){
        if (intervalType() == IntervalType.NumericInterval){
            this.left = new NumericBoundary(interval.leftBoundary(), !interval.leftClose(), interval.type);
            this.right = new NumericBoundary(interval.rightBoundary(), interval.rightClose(), interval.type);
            this.type = interval.type;
        }
        if (intervalType() == IntervalType.BooleanInterval){
            this.booleanBoundary = interval.booleanBoundary();
        }
        if (intervalType() == IntervalType.ObjectInterval){
            this.objectBoundary = interval.objectBoundary();
        }
    }

    private Interval(NumericBoundary left, NumericBoundary right){
        this.left = left;
        this.right = right;
        this.type = left.type;
    }


    public static Interval fromString(String interval, String type){
        if (interval.startsWith("(")){
            interval = interval.substring(1);
        }
        if (interval.endsWith(")")){
            interval = interval.substring(0, interval.length()-1);
        }
        NumericBoundary left = new NumericBoundary(-Double.MAX_VALUE, true, TypeEnum.getType(type));
        NumericBoundary right = new NumericBoundary(Double.MAX_VALUE, false, TypeEnum.getType(type));
        try {
            if (interval.contains("&&")){
                String[] intervals = interval.split("&&");
                for (String halfInterval: intervals){
                    if (halfInterval.contains(">=")){
                        left.boundary = MathUtils.parseStringValue(halfInterval.split(">=")[1]);
                        left.modifier = false;
                    }
                    else if (halfInterval.contains("<=")){
                        right.boundary = MathUtils.parseStringValue(halfInterval.split("<=")[1]);
                        right.modifier = true;
                    }
                    else if (halfInterval.contains(">")){
                        left.boundary = MathUtils.parseStringValue(halfInterval.split(">")[1]);
                    }
                    else if (halfInterval.contains("<")){
                        right.boundary = MathUtils.parseStringValue(halfInterval.split("<")[1]);
                    }
                }
                return new Interval(left, right);
            }
            if (interval.contains("||")){
                String[] intervals = interval.split("\\|\\|");
                for (String halfInterval: intervals){
                    if (halfInterval.contains(">=")){
                        left.boundary = MathUtils.parseStringValue(halfInterval.split(">=")[1]);
                        left.modifier = false;
                    }
                    else if (halfInterval.contains("<=")){
                        right.boundary = MathUtils.parseStringValue(halfInterval.split("<=")[1]);
                        right.modifier = true;
                    }
                    else if (halfInterval.contains(">")){
                        left.boundary = MathUtils.parseStringValue(halfInterval.split(">")[1]);
                    }
                    else if (halfInterval.contains("<")){
                        System.out.println(interval);
                        right.boundary = MathUtils.parseStringValue(halfInterval.split("<")[1]);
                    }
                }
                return new Interval(left, right);
            }

            if (interval.contains(">=")){
                left.boundary = MathUtils.parseStringValue(interval.split(">=")[1]);
                left.modifier = false;
            }
            else if (interval.contains("<=")){
                right.boundary = MathUtils.parseStringValue(interval.split("<=")[1]);
                right.modifier = true;
            }
            else if (interval.contains(">")){
                left.boundary = MathUtils.parseStringValue(interval.split(">")[1]);
            }
            else if (interval.contains("<")){
                right.boundary = MathUtils.parseStringValue(interval.split("<")[1]);
            }
        } catch (NumberFormatException e){
            return null;
        }
        return new Interval(left, right);
    }

    @Override
    public String toString(){
        if (intervalType() == IntervalType.NumericInterval){
            return (leftClose()?"[":"(")+(leftBoundary() == -Double.MAX_VALUE ? "-Double.MAX_VALUE": leftBoundary())+", "+ (rightBoundary() == Double.MAX_VALUE ? "Double.MAX_VALUE": rightBoundary())+ (rightClose()?"]":")");
        }
        if (intervalType() == IntervalType.ObjectInterval){
            return objectBoundary();
        }
        if (intervalType() == IntervalType.BooleanInterval){
            return booleanBoundary ? "true" : "false";
        }
        return null;
    }

    public List<Interval> reverse(){
        if (intervalType() == IntervalType.NumericInterval){
            if (leftBoundary() == -Double.MAX_VALUE){
                return Arrays.asList(new Interval(rightBoundary(),Double.MAX_VALUE, !rightClose(), leftClose()));
            }
            else if (rightBoundary() == Double.MAX_VALUE){
                return Arrays.asList(new Interval(-Double.MAX_VALUE, leftBoundary(),rightClose(), !leftClose()));
            }
            else if (leftBoundary() == rightBoundary() && leftClose() && rightClose()){
                return Arrays.asList(new Interval(leftBoundary(), rightBoundary(), leftClose(), rightClose()));
            }
            else {
                List<Interval> list = new ArrayList<>();
                Interval left = new Interval(-Double.MAX_VALUE, leftBoundary(),false, !leftClose());
                Interval right = new Interval(rightBoundary(),Double.MAX_VALUE, !rightClose(), false);
                list.add(left);
                list.add(right);
                return list;
            }
        }
        else if (intervalType() == IntervalType.BooleanInterval){
            return booleanBoundary() ? Arrays.asList(new Interval(false)) : Arrays.asList(new Interval(true));
        }
        else if (intervalType() == IntervalType.ObjectInterval){
            return Arrays.asList(new Interval(this.objectBoundary()));
        }
        return null;
    }

    public String toString(String varName){
        if (intervalType() == IntervalType.NumericInterval){
            String typeString = MathUtils.getSimpleOfNumberType(type.toString());
            String left = "";
            if (leftBoundary()!=-Double.MAX_VALUE){
                left = varName+greaterSysbol()+"("+typeString+")"+leftBoundary();
            }
            String right = "";
            if (rightBoundary()!=Double.MAX_VALUE){
                right = varName+lessSymbol()+"("+typeString+")"+rightBoundary();
            }
            if (!left.equals("") && !right.equals("")){
                return left+"&&"+right;
            }
            return left+right;
        }
        else if (intervalType() == IntervalType.BooleanInterval){
            return booleanBoundary() ? varName : "!" + varName;
        }
        else if (intervalType() == IntervalType.ObjectInterval){
            return varName + ".equals(" + objectBoundary() + ")";
        }
        return null;
    }

    private String lessSymbol(){
        if (rightClose()){
            return "<=";
        }
        return "<";
    }

    private String greaterSysbol(){
        if (leftClose()){
            return ">=";
        }
        return ">";
    }

    public boolean containsValue(Object value){
        if (intervalType() == IntervalType.NumericInterval){
            Double doubleValue;
            try {
                doubleValue = (double) value;
            } catch (ClassCastException e){
                try {
                    doubleValue = MathUtils.parseStringValue((String) value);
                } catch (NumberFormatException ee){
                    return false;
                }
            }
            if (doubleValue > leftBoundary() && doubleValue < rightBoundary()){
                return true;
            }
            if (doubleValue == leftBoundary() && leftClose()){
                return true;
            }
            if (doubleValue == rightBoundary() && rightClose()){
                return true;
            }
            return false;
        }
        if (intervalType() == IntervalType.BooleanInterval){
            if (value instanceof Boolean){
                return value == intervalType();
            }
            if (value instanceof String){
                if (((String) value).equalsIgnoreCase("true") && booleanBoundary()){
                    return true;
                }
                if (((String) value).equalsIgnoreCase("false") && !booleanBoundary()){
                    return true;
                }
            }
            return false;

        }
        else if (intervalType() == IntervalType.ObjectInterval){
            return objectBoundary().equals(value);
        }
        return false;
    }


    @Override
    public boolean equals(Object obj){
        if (!(obj instanceof Interval))
            return false;
        Interval other = (Interval) obj;
        if (intervalType()!= other.intervalType()){
            return false;
        }
        if (intervalType() == IntervalType.NumericInterval){
            return left.equals(other.left) && right.equals(other.right);
        }
        else if (intervalType() == IntervalType.BooleanInterval){
            return booleanBoundary() == other.booleanBoundary();
        }
        else if (intervalType() == IntervalType.ObjectInterval){
            return objectBoundary().equals(other.objectBoundary());
        }
        return false;
    }

    public boolean isValue(){
        if (intervalType() == IntervalType.NumericInterval){
            if (leftBoundary() == rightBoundary()){
                if (leftClose() && rightClose()){
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    public static boolean hasCommon(Interval left, Interval right){
        if (left.intervalType() != right.intervalType()){
            return false;
        }
        if (left.intervalType() == IntervalType.NumericInterval) {
            if (!NumericBoundary.max(left).lessThat(NumericBoundary.min(right))
                    && !NumericBoundary.max(right).lessThat(NumericBoundary.min(left))) {
                return true;
            }
            return false;
        }
        return left.equals(right);
    }

    public static Interval intersection(Interval left, Interval right){
        if (!hasCommon(left, right)){
            return null;
        }
        if (left.intervalType() == IntervalType.NumericInterval){
            return new Interval(NumericBoundary.max(NumericBoundary.min(left), NumericBoundary.min(right)),
                    NumericBoundary.min(NumericBoundary.max(left), NumericBoundary.max(right)));
        }
        return left;
    }


    public static void simplify(List<Interval> intervals){
        int lastNum = intervals.size();
        for (;;){
            if (intervals.size() < 2){
                return;
            }
            List<List<Interval>> subsets = subsets(intervals);
            for (List<Interval> couple: subsets){
                boolean jumpFlag = false;
                for (Interval interval: couple){
                    if (!intervals.contains(interval)){
                        jumpFlag = true;
                        break;
                    }
                }
                if (jumpFlag){
                    continue;
                }
                Interval newInterval = Interval.intersection(couple.get(0), couple.get(1));
                if (newInterval != null){
                    intervals.remove(couple.get(0));
                    intervals.remove(couple.get(1));
                    intervals.add(newInterval);
                }
            }
            if (intervals.size() == lastNum){
                break;
            }
            else {
                lastNum = intervals.size();
            }
        }

    }

    private static <T> List<List<T>> subsets(List<T> nums) {
        List<List<T>> res = new ArrayList<>();
        List<T> each = new ArrayList<>();
        helper(res, each, 0, nums);
        return res;
    }

    private static <T> void helper(List<List<T>> res, List<T> each, int pos, List<T> n) {
        if (pos <= n.size() && each.size()==2) {
            res.add(each);
        }
        for (int i = pos; i < n.size(); i++) {
            each.add(n.get(i));
            if (each.size() < 3){
                helper(res, new ArrayList<>(each), i + 1, n);
            }
            each.remove(each.size() - 1);
        }
        return;
    }


}
