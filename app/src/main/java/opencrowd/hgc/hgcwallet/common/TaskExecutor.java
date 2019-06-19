package opencrowd.hgc.hgcwallet.common;

import android.os.AsyncTask;

public class TaskExecutor extends AsyncTask<BaseTask,Float,BaseTask> {
    public interface TaskListner {
        void onResult(BaseTask task);
    }

    private TaskListner listner;

    public void setListner(TaskListner listner) {
        this.listner = listner;
    }

    @Override
    protected BaseTask doInBackground(BaseTask... tasks) {
        BaseTask task = tasks[0];
        task.main();
        return  task;
    }

    @Override
    protected void onPostExecute(BaseTask task) {
        super.onPostExecute(task);
        listner.onResult(task);
    }
}
