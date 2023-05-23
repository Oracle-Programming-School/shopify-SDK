
package com.ordg.utl.collections;

public class AppThread extends Thread {
    
    public AppThread (String name)
    {
        this.setName(name);
    }

    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();
        
        if (threadName.equals("loadItemsDetailsFromDatabase"))
        {
            AppThreadController.loadItemsDetailsFromDatabase();
        }
    }
}
