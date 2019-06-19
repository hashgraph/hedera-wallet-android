package opencrowd.hgc.hgcwallet.common;

public abstract class BaseTask {
    public String error;
    public Object result;
    public abstract void main();
}
