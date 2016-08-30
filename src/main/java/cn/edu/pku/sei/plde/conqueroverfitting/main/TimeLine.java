package cn.edu.pku.sei.plde.conqueroverfitting.main;

/**
 * Created by yanrunfa on 5/4/16.
 */
public class TimeLine {
    private long startTime;
    private long timeLimit;
    private boolean timeoutNow = false;

    private long downloadTime = 0;

    public TimeLine(int timeLimit){
        this.timeLimit = timeLimit;
        startTime = System.currentTimeMillis();
    }

    public void addDownloadTime(long time){
        downloadTime +=time;
    }
    public boolean isTimeout(){
        if (timeoutNow){
            return true;
        }
        return (System.currentTimeMillis() - startTime - downloadTime)/1000 > timeLimit;
    }

    public void timeOutNow(){
        timeoutNow = true;
    }
}
