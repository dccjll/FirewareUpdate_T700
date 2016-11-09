package com.dsm.t700.update;

/**
 * Created by dessmann on 16/8/23.
 * 蓝牙消息接口
 */
public class OnBLEListener {
    //通用操作协议返回数据处理接口(不回数据，只判断写成功)
    public interface BLEDataWrittenListener{
        public void writeSuccess();
        public void writeFailure(String error);
    }
}
