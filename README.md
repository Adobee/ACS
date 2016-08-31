1. ACS Correct Patches

ACS generates correct patches for 18 defects. For each defect, we provide the urls that contain the developer patch and we either identify the ACS patch is semantically equivalent to the developer patch and provide a brief analysis for why the ACS patch is correct.

1.Chart14
The developer patches:
https://github.com/Adobee/ACS/blob/master/patch/Developer/Chart14/source/org/jfree/chart/plot/CategoryPlot.java
https://github.com/Adobee/ACS/blob/master/patch/Developer/Chart14/source/org/jfree/chart/plot/XYPlot.java
The ACS correct patches:
https://github.com/Adobee/ACS/blob/master/patch/ACS/Chart14/source/org/jfree/chart/plot/CategoryPlot.java
https://github.com/Adobee/ACS/blob/master/patch/ACS/Chart14/source/org/jfree/chart/plot/XYPlot.java

Analysis:The ACS generated patch is identical to the developer patch.

2.Chart19
The developer patches:
https://github.com/Adobee/ACS/blob/master/patch/Developer/Chart19/source/org/jfree/chart/plot/CategoryPlot.java
The ACS correct patches:
https://github.com/Adobee/ACS/blob/master/patch/ACS/Chart19/source/org/jfree/chart/plot/CategoryPlot.java

Analysis:The ACS generated patch is identical to the developer patch.

3.Math3
The developer patches:
https://github.com/Adobee/ACS/blob/master/patch/Developer/Math3/src/main/java/org/apache/commons/math3/util/MathArrays.java
The ACS correct patches:
https://github.com/Adobee/ACS/blob/master/patch/ACS/Math3/src/main/java/org/apache/commons/math3/util/MathArrays.java

Analysis:The ACS generated patch is identical to the developer patch.

4.Math4
The developer patches:
https://github.com/Adobee/ACS/blob/master/patch/Developer/Math4/src/main/java/org/apache/commons/math3/geometry/euclidean/threed/SubLine.java
https://github.com/Adobee/ACS/blob/master/patch/Developer/Math4/src/main/java/org/apache/commons/math3/geometry/euclidean/twod/SubLine.java
The ACS correct patches:
https://github.com/Adobee/ACS/blob/master/patch/ACS/Math4/src/main/java/org/apache/commons/math3/geometry/euclidean/threed/SubLine.java
https://github.com/Adobee/ACS/blob/master/patch/ACS/Math4/src/main/java/org/apache/commons/math3/geometry/euclidean/twod/SubLine.java

Analysis:The ACS generated patch is identical to the developer patch.

5.Math5 
The developer patches:
https://github.com/Adobee/ACS/blob/master/patch/Developer/Math5/src/main/java/org/apache/commons/math3/complex/Complex.java
The ACS correct patches:
https://github.com/Adobee/ACS/blob/master/patch/ACS/Math5/src/main/java/org/apache/commons/math3/complex/Complex.java

Analysis:The ACS generated patch is identical to the developer patch.

6.Math25
The developer patches:
https://github.com/Adobee/ACS/blob/master/patch/Developer/Math25/src/main/java/org/apache/commons/math3/optimization/fitting/HarmonicFitter.java
The ACS correct patches:
https://github.com/Adobee/ACS/blob/master/patch/ACS/Math25/src/main/java/org/apache/commons/math3/optimization/fitting/HarmonicFitter.java

Analysis:The ACS generated patch is identical to the developer patch.

7.Math35
The developer patches:
https://github.com/Adobee/ACS/blob/master/patch/Developer/Math35/src/main/java/org/apache/commons/math3/genetics/ElitisticListPopulation.java
The ACS correct patches:
https://github.com/Adobee/ACS/blob/master/patch/ACS/Math35/src/main/java/org/apache/commons/math3/genetics/ElitisticListPopulation.java

Analysis:The ACS generated patch is identical to the developer patch.

8.Math61
The developer patches:
https://github.com/Adobee/ACS/blob/master/patch/Developer/Math61/src/main/java/org/apache/commons/math/distribution/PoissonDistributionImpl.java
The ACS correct patches:
https://github.com/Adobee/ACS/blob/master/patch/ACS/Math61/src/main/java/org/apache/commons/math/distribution/PoissonDistributionImpl.java

Analysis:The ACS generated patch is identical to the developer patch.

9.Math82
The developer patches:
https://github.com/Adobee/ACS/blob/master/patch/Developer/Math82/src/main/java/org/apache/commons/math/optimization/linear/SimplexSolver.java
The ACS correct patches:
https://github.com/Adobee/ACS/blob/master/patch/ACS/Math82/src/main/java/org/apache/commons/math/optimization/linear/SimplexSolver.java

Analysis:The ACS generated patch is identical to the developer patch.

10.Math85
The developer patches:
https://github.com/Adobee/ACS/blob/master/patch/Developer/Math85/src/java/org/apache/commons/math/analysis/solvers/UnivariateRealSolverUtils.java
The ACS correct patches:
https://github.com/Adobee/ACS/blob/master/patch/ACS/Math85/src/java/org/apache/commons/math/analysis/solvers/UnivariateRealSolverUtils.java

Analysis:The ACS generated patch is identical to the developer patch.

11.Math89
The developer patches:
https://github.com/Adobee/ACS/blob/master/patch/Developer/Math89/src/java/org/apache/commons/math/stat/Frequency.java
The ACS correct patches:
https://github.com/Adobee/ACS/blob/master/patch/ACS/Math89/src/java/org/apache/commons/math/stat/Frequency.java

Analysis:The ACS generated patch is identical to the developer patch.

12.Math90
The developer patches:
https://github.com/Adobee/ACS/tree/master/patch/Developer/Math90/src/java/org/apache/commons/math/stat
The ACS correct patches:
https://github.com/Adobee/ACS/blob/master/patch/ACS/Math90/src/java/org/apache/commons/math/stat/Frequency.java

Analysis:The ACS generated patch is identical to the developer patch.

13.Math93
The developer patches:
https://github.com/Adobee/ACS/blob/master/patch/Developer/Math93/src/java/org/apache/commons/math/util/MathUtils.java
The ACS correct patches:
https://github.com/Adobee/ACS/blob/master/patch/ACS/Math93/src/java/org/apache/commons/math/util/MathUtils.java

Analysis:There are three developer patches, but the third patch has nothing to do with the bug, and the other two developer patches are identical to ACS's patches. Note that, the predicate in ACS patch is < 20, which in developer patch is <= 20, , so we need not to consider the factorial of 20.

14.Math99
The developer patches:
https://github.com/Adobee/ACS/blob/master/patch/Developer/Math99/src/java/org/apache/commons/math/util/MathUtils.java
The ACS correct patches:
https://github.com/Adobee/ACS/blob/master/patch/ACS/Math99/src/java/org/apache/commons/math/util/MathUtils.java

Analysis:The ACS generated patch is identical to the developer patch.
 
15.Time15
The developer patches:
https://github.com/Adobee/ACS/blob/master/patch/Developer/Time15/src/main/java/org/joda/time/field/FieldUtils.java
The ACS correct patches:
https://github.com/Adobee/ACS/blob/master/patch/ACS/Time15/src/main/java/org/joda/time/field/FieldUtils.java

Analysis:The ACS generated patch is identical to the developer patch.

16.Lang7
The developer patches:
https://github.com/Adobee/ACS/blob/master/patch/Developer/Lang7/src/main/java/org/apache/commons/lang3/math/NumberUtils.java
The ACS correct patches:
https://github.com/Adobee/ACS/blob/master/patch/ACS/Lang7/src/main/java/org/apache/commons/lang3/math/NumberUtils.java

Analysis:The developer patch uses the trim() method, however the comment ¡°This method does not trim the input string, i.e., strings with leading
or trailing spaces will generate NumberFormatExceptions.¡± shows that the leading and trailing spaces have been checked, and there is no need to use the trim() method. So the ACS generated patch is identical to the developer patch.

17.Lang24
The developer patches:
https://github.com/Adobee/ACS/blob/master/patch/Developer/Lang24/src/main/java/org/apache/commons/lang3/math/NumberUtils.java
The ACS correct patches:
https://github.com/Adobee/ACS/blob/master/patch/ACS/Lang24/src/main/java/org/apache/commons/lang3/math/NumberUtils.java

Analysis:The ACS generated patch is identical to the developer patch.

18.Lang35
The developer patches:
https://github.com/Adobee/ACS/blob/master/patch/Developer/Lang35/src/main/java/org/apache/commons/lang3/ArrayUtils.java
The ACS correct patches:
https://github.com/Adobee/ACS/blob/master/patch/ACS/Lang35/src/main/java/org/apache/commons/lang3/ArrayUtils.java

Analysis:The ACS generated patch is identical to the developer patch.




