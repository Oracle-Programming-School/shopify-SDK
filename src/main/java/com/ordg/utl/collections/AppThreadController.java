package com.ordg.utl.collections;

import com.ordg.utl.collections.DataLoader;
import com.ordg.blueb.pos.sales.ItemVO;
import com.ordg.blueb.pos.sales.ItemVOLoader;

public class AppThreadController {
    
public static void loadItemsDetailsFromDatabase()
{
    DataLoader.setItemVOList(ItemVOLoader.loadItemsFromQuery());
    
}

    
    
}
