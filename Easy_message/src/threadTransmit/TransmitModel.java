package threadTransmit;

public interface TransmitModel extends Runnable{
    public abstract void setNum(int index);
    public abstract int getNum();
    public abstract void setBool(boolean ifit);
    public abstract  boolean getBool();
}
