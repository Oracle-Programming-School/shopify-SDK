package com.ordg.utl.collections;

import java.util.List;
import com.ordg.blueb.pos.sales.ItemVO;

public class DataLoader {

static List<ItemVO> ItemVOList;    

    public static List<ItemVO> getItemVOList() {
        return ItemVOList;
    }

    public static void setItemVOList(List<ItemVO> ItemVOList) {
        DataLoader.ItemVOList = ItemVOList;
    }
    
    public static ItemVO getItemByBarcode(String itemBarcode) {
    for(ItemVO item : ItemVOList) {
        if(item.getItemBarcode() .equals(itemBarcode)) {
            return item;
        }
    }
    return null; // return null if no matching item is found
}


}
