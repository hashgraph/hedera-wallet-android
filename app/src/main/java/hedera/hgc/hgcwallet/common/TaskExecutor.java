/*
 *
 *  Copyright 2019 Hedera Hashgraph LLC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package hedera.hgc.hgcwallet.common;

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
