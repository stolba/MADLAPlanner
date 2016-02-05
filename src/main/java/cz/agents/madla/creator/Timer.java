package cz.agents.madla.creator;

/**
 * Created with IntelliJ IDEA.
 * User: durkokar
 * Date: 7/4/13
 * Time: 5:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class Timer {

    private static long translationTime=0,preprocessingTime=0,creatEntetiesTime=0,runEntetiesTime=0;

    static void init(){
        Timer.setTranslationTime(0);
        Timer.setRunEntetiesTime(0);
        Timer.setCreatEntetiesTime(0);
        Timer.setPreprocessingTime(0);
    }

    static public long getTranslationTime() {
        return translationTime;
    }

    static public void setTranslationTime(long translationTime) {
        Timer.translationTime = translationTime;
    }

    static public long getPreprocessingTime() {
        return preprocessingTime;
    }

    static public void setPreprocessingTime(long preprocessingTime) {
        Timer.preprocessingTime = preprocessingTime;
    }

    static public long getCreatEntetiesTime() {
        return creatEntetiesTime;
    }

    static public void setCreatEntetiesTime(long creatEntetiesTime) {
        Timer.creatEntetiesTime = creatEntetiesTime;
    }

    static public long getRunEntetiesTime() {
        return runEntetiesTime;
    }

    static public void setRunEntetiesTime(long runEntetiesTime) {
        Timer.runEntetiesTime = runEntetiesTime;
    }

    public static String print(){
        String str = "TIMES: "+Timer.getTranslationTime()+" "+Timer.getPreprocessingTime()+" "+Timer.getCreatEntetiesTime()+" "+Timer.getRunEntetiesTime();
        return str;
    }
}
