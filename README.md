
ACS 
====
Accurate Condition Synthesis

I. Requirement
----------------
 - Ubuntu14.04
 - Java 1.7
 - Python 2.7(need to install psutil)
 - [Defects4J](https://github.com/rjust/defects4j)
 
II. How to run the ACS
---------------------
Two optional: A. run the virtual machine; B. Set up the environment. Choose the way you like.
### A. Run the virtual machine
  If you do not want to set up the environment, please download the [virtual machine](https://drive.google.com/file/d/0B60sl-8vpnfEcVZkUVZCYXBvZlE/view?usp=sharing), unzip and open it by VmWare12.5.2(password: hhxxttxs). There are 23 projects(18 correct and 5 incorrect) in the directory "/home/yjxxtd/software/defects4j-project/", the ACS is in the directory "/home/yjxxtd/software/ACS/ACS-master/". Please run the run.sh in the directory "/home/yjxxtd/software/ACS/ACS-master/" to generate the 23 patches for the 23 projects. If it does not work normaly,  please increase the timeout time.
### B. Set up the Environment
1. Clone the ACS:
  - `git clone https://github.com/Adobee/ACS.git`

2. Checkout a buggy source code version from Defects4j anc compile it:
  - `defects4j checkout -p Lang -v 1b -w /tmp/Lang_1`
  - `cd /tmp/Lang_1`
  - `defects4j compile`
 
 the forder name of checkout must format as '`ProjectName_buggyID`' because the ACS needs it's project name and buggy id to generate it's classpath, source path etc.

3. Run the Jar with the parent path of Defects4j buggy version project forder:
  - `java -jar ACS.jar /tmp/`
 
 and ACS will automatically find all formatted folder and run with it.

  Or run with specific buggy projects in `/tmp/` forder(Note the space between "/tmp/" and "Lang_1"):
  - `java -jar ACS.jar /tmp/ Lang_1`
  - `java -jar ACS.jar /tmp/ Lang_1;Lang_2;Lang_3`
  - `java -jar ACS.jar /tmp/ ban:Lang_1;Lang2`

  Or set a custom timeout seconds depends on your computer performance (default as 1800):
  - `java -jar ACS.jar /tmp/ Lang_1 timeout:3600`
  
4. Tips of Chart19: After checkout from Defects4J, There are two `itext` jar files in `Chart_19/lib` folder and one of which named "itext-2.0.6.jar" is a broken jar and leads to the error of ACS, delete it and ACS would get back to normal.

5. The results are in the dir "ACS/resultMessage", the detailed patch is in the file "ACS/resultMessage/patchSource/Project_BugId_Schema_PatchId.java"(If a bug has n patches, the "PatchId" will range from 0 to n - 1),  and is between "patch begin" and "patch end".

6. Please note that the patch file in the directory "ACS/resultMessage/patchSource/" does not mean it passes all the tests. If it fails to pass all the test, the patch file will be deleted if ACS has enough time. So if you want to know whether the patch file pass all the tests, please apply it in the source code, and test it manually by using the command "defects4j test".

7. There are some pictures in the dir "ACS/pictures" to show how to run the ACS.

8. If ACS didn't generate the patch or the patch is incomplete, it maybe cause by the performance of the computer, please increase the timeout time.




III. Evaluation
----------------

### 1.ACS Correct Patches

ACS generates correct patches for 18 defects. For each defect, we provide the urls that contain the developer patch and we either identify the ACS patch is semantically equivalent to the developer patch and provide a brief analysis for why the ACS patch is correct.

#### 1).Chart14

[The developer patches 1](https://github.com/Adobee/ACS/blob/master/patch/Developer/Chart14/source/org/jfree/chart/plot/CategoryPlot.java)

[The developer patches 2](https://github.com/Adobee/ACS/blob/master/patch/Developer/Chart14/source/org/jfree/chart/plot/XYPlot.java)

[The ACS correct patches 1](https://github.com/Adobee/ACS/blob/master/patch/ACS/Chart14/source/org/jfree/chart/plot/CategoryPlot.java)

[The ACS correct patches 2](https://github.com/Adobee/ACS/blob/master/patch/ACS/Chart14/source/org/jfree/chart/plot/XYPlot.java)

>Analysis:The ACS generated patch is identical to the developer patch.

#### 2).Chart19
[The developer patches](https://github.com/Adobee/ACS/blob/master/patch/Developer/Chart19/source/org/jfree/chart/plot/CategoryPlot.java)

[The ACS correct patches](https://github.com/Adobee/ACS/blob/master/patch/ACS/Chart19/source/org/jfree/chart/plot/CategoryPlot.java)

>Analysis:The ACS generated patch is identical to the developer patch.

>Tips: After checkout from Defects4J, There are two `itext` jar files in `Chart_19/lib` folder and one of which named "itext-2.0.6.jar" is a broken
jar and leads to the error of ACS, delete it and ACS would get back to normal.

#### 3).Math3
[The developer patches](https://github.com/Adobee/ACS/blob/master/patch/Developer/Math3/src/main/java/org/apache/commons/math3/util/MathArrays.java)

[The ACS correct patches](https://github.com/Adobee/ACS/blob/master/patch/ACS/Math3/src/main/java/org/apache/commons/math3/util/MathArrays.java)

>Analysis:The ACS generated patch is identical to the developer patch.

#### 4).Math4
[The developer patches 1](https://github.com/Adobee/ACS/blob/master/patch/Developer/Math4/src/main/java/org/apache/commons/math3/geometry/euclidean/threed/SubLine.java)

[The developer patches 2](https://github.com/Adobee/ACS/blob/master/patch/Developer/Math4/src/main/java/org/apache/commons/math3/geometry/euclidean/twod/SubLine.java)

[The ACS correct patches 1](https://github.com/Adobee/ACS/blob/master/patch/ACS/Math4/src/main/java/org/apache/commons/math3/geometry/euclidean/threed/SubLine.java)

[The ACS correct patches 2](https://github.com/Adobee/ACS/blob/master/patch/ACS/Math4/src/main/java/org/apache/commons/math3/geometry/euclidean/twod/SubLine.java)

>Analysis:The ACS generated patch is identical to the developer patch.

#### 5).Math5
[The developer patches](https://github.com/Adobee/ACS/blob/master/patch/Developer/Math5/src/main/java/org/apache/commons/math3/complex/Complex.java)

[The ACS correct patches](https://github.com/Adobee/ACS/blob/master/patch/ACS/Math5/src/main/java/org/apache/commons/math3/complex/Complex.java)

>Analysis:The ACS generated patch is identical to the developer patch.

#### 6).Math25
[The developer patches](https://github.com/Adobee/ACS/blob/master/patch/Developer/Math25/src/main/java/org/apache/commons/math3/optimization/fitting/HarmonicFitter.java)

[The ACS correct patches](https://github.com/Adobee/ACS/blob/master/patch/ACS/Math25/src/main/java/org/apache/commons/math3/optimization/fitting/HarmonicFitter.java)

>Analysis:The ACS generated patch is identical to the developer patch.

#### 7).Math35
[The developer patches](https://github.com/Adobee/ACS/blob/master/patch/Developer/Math35/src/main/java/org/apache/commons/math3/genetics/ElitisticListPopulation.java)

[The ACS correct patches](https://github.com/Adobee/ACS/blob/master/patch/ACS/Math35/src/main/java/org/apache/commons/math3/genetics/ElitisticListPopulation.java)

>Analysis:The ACS generated patch is identical to the developer patch.

#### 8).Math61
[The developer patches](https://github.com/Adobee/ACS/blob/master/patch/Developer/Math61/src/main/java/org/apache/commons/math/distribution/PoissonDistributionImpl.java)

[The ACS correct patches](https://github.com/Adobee/ACS/blob/master/patch/ACS/Math61/src/main/java/org/apache/commons/math/distribution/PoissonDistributionImpl.java)

>Analysis:The ACS generated patch is identical to the developer patch.

#### 9).Math82
[The developer patches](https://github.com/Adobee/ACS/blob/master/patch/Developer/Math82/src/main/java/org/apache/commons/math/optimization/linear/SimplexSolver.java)

[The ACS correct patches](https://github.com/Adobee/ACS/blob/master/patch/ACS/Math82/src/main/java/org/apache/commons/math/optimization/linear/SimplexSolver.java)

>Analysis:The ACS generated patch is identical to the developer patch.

#### 10).Math85
[The developer patches](https://github.com/Adobee/ACS/blob/master/patch/Developer/Math85/src/java/org/apache/commons/math/analysis/solvers/UnivariateRealSolverUtils.java)

[The ACS correct patches](https://github.com/Adobee/ACS/blob/master/patch/ACS/Math85/src/java/org/apache/commons/math/analysis/solvers/UnivariateRealSolverUtils.java)

>Analysis:The ACS generated patch is identical to the developer patch.

#### 11).Math89
[The developer patches](https://github.com/Adobee/ACS/blob/master/patch/Developer/Math89/src/java/org/apache/commons/math/stat/Frequency.java)

[The ACS correct patches](https://github.com/Adobee/ACS/blob/master/patch/ACS/Math89/src/java/org/apache/commons/math/stat/Frequency.java)

>Analysis:The ACS generated patch is identical to the developer patch.

#### 12).Math90
[The developer patches](https://github.com/Adobee/ACS/tree/master/patch/Developer/Math90/src/java/org/apache/commons/math/stat)

[The ACS correct patches](https://github.com/Adobee/ACS/blob/master/patch/ACS/Math90/src/java/org/apache/commons/math/stat/Frequency.java)

>Analysis:The ACS generated patch is identical to the developer patch.

#### 13).Math93
[The developer patches](https://github.com/Adobee/ACS/blob/master/patch/Developer/Math93/src/java/org/apache/commons/math/util/MathUtils.java)

[The ACS correct patches](https://github.com/Adobee/ACS/blob/master/patch/ACS/Math93/src/java/org/apache/commons/math/util/MathUtils.java)

>Analysis:The developer patch contains three parts, and the last part is not related to the defect. The first and second part are identical to ACS patch. The second part uses n < 21 while ACS patch uses n < 20. However, when n is equal to 20, either the true branch or the false branch produce exactly the same result.

#### 14).Math99
[The developer patches](https://github.com/Adobee/ACS/blob/master/patch/Developer/Math99/src/java/org/apache/commons/math/util/MathUtils.java)

[The ACS correct patches](https://github.com/Adobee/ACS/blob/master/patch/ACS/Math99/src/java/org/apache/commons/math/util/MathUtils.java)

>Analysis:The ACS generated patch is identical to the developer patch.
 
#### 15).Time15
[The developer patches](https://github.com/Adobee/ACS/blob/master/patch/Developer/Time15/src/main/java/org/joda/time/field/FieldUtils.java)

[The ACS correct patches](https://github.com/Adobee/ACS/blob/master/patch/ACS/Time15/src/main/java/org/joda/time/field/FieldUtils.java)

>Analysis:The ACS generated patch is identical to the developer patch.

#### 16).Lang7
[The developer patches](https://github.com/Adobee/ACS/blob/master/patch/Developer/Lang7/src/main/java/org/apache/commons/lang3/math/NumberUtils.java)

[The ACS correct patches](https://github.com/Adobee/ACS/blob/master/patch/ACS/Lang7/src/main/java/org/apache/commons/lang3/math/NumberUtils.java)

>Analysis:The developer patch uses the trim() method, while ACS patch does not. However, the method createBigDecimal() will only be called by createNumber(), which ensures that
the parameter "str" does not contain any space. So calling trim() is not necessary and the two patches are identical.

#### 17).Lang24
[The developer patches](https://github.com/Adobee/ACS/blob/master/patch/Developer/Lang24/src/main/java/org/apache/commons/lang3/math/NumberUtils.java)

[The ACS correct patches](https://github.com/Adobee/ACS/blob/master/patch/ACS/Lang24/src/main/java/org/apache/commons/lang3/math/NumberUtils.java)

>Analysis:The ACS generated patch is identical to the developer patch.

#### 18).Lang35
[The developer patches](https://github.com/Adobee/ACS/blob/master/patch/Developer/Lang35/src/main/java/org/apache/commons/lang3/ArrayUtils.java)

[The ACS correct patches](https://github.com/Adobee/ACS/blob/master/patch/ACS/Lang35/src/main/java/org/apache/commons/lang3/ArrayUtils.java)

>Analysis:The ACS generated patch is identical to the developer patch.

### 2.ACS Incorrect Patches

#### 1). Lang39
[The developer patches](https://github.com/Adobee/ACS/blob/master/patch/Developer/Lang39/src/main/java/org/apache/commons/lang3/StringUtils.java)

[The ACS incorrect patches](https://github.com/Adobee/ACS/blob/master/patch/ACS/Lang39/src/main/java/org/apache/commons/lang3/StringUtils.java)

#### 2). Math28
[The developer patches](https://github.com/Adobee/ACS/blob/master/patch/Developer/Math28/src/main/java/org/apache/commons/math3/optimization/linear/SimplexSolver.java)

[The ACS incorrect patches](https://github.com/Adobee/ACS/blob/master/patch/ACS/Math28/src/main/java/org/apache/commons/math3/optimization/linear/SimplexSolver.java)

#### 3). Math73
[The developer patches](https://github.com/Adobee/ACS/blob/master/patch/Developer/Math73/src/main/java/org/apache/commons/math/analysis/solvers/BrentSolver.java)

[The ACS incorrect patches](https://github.com/Adobee/ACS/blob/master/patch/ACS/Math73/src/main/java/org/apache/commons/math/analysis/solvers/BrentSolver.java)

#### 4). Math81
[The developer patches](https://github.com/Adobee/ACS/blob/master/patch/Developer/Math81/src/main/java/org/apache/commons/math/linear/EigenDecompositionImpl.java)

[The ACS incorrect patches](https://github.com/Adobee/ACS/blob/master/patch/ACS/Math81/src/main/java/org/apache/commons/math/linear/EigenDecompositionImpl.java)

#### 5). Math97
[The developer patches](https://github.com/Adobee/ACS/blob/master/patch/Developer/Math97/src/main/java/org/apache/commons/math/analysis/BrentSolver.java)

[The ACS incorrect patches](https://github.com/Adobee/ACS/blob/master/patch/ACS/Math97/src/main/java/org/apache/commons/math/analysis/BrentSolver.java)

### 3.Performance Data of the Three Techniques

[DEPENDENCY-BASED ORDERING PERFORMANCE LEVEL DATA](https://github.com/Adobee/ACS/blob/master/patch/evaluation/DEPENDENCY-BASED%20ORDERING%20PERFORMANCE%20LEVEL%20DATA.log)

>**Format:** Easy to understand


[DEPENDENCY-BASED ORDERING PERFORMANCE RANK DATA](https://github.com/Adobee/ACS/blob/master/patch/evaluation/DEPENDENCY-BASED%20ORDERING%20PERFORMANCE%20RANK%20DATA.log)

>**Format:** Variable rank / Total


[PREDICATE MINING PERFORMANCE DATA](https://github.com/Adobee/ACS/blob/master/patch/evaluation/PREDICATE%20MINING%20PERFORMANCE%20DATA.log)

>**Format:** Easy to understand


[DOCUMENT ANALYSIS PERFORMANCE DATA](https://github.com/Adobee/ACS/blob/master/patch/evaluation/DOCUMENT%20ANALYSIS%20PERFORMANCE%20DATA.log)

>**Format:** JavaDoc comment / condition expression / list of variable name 
