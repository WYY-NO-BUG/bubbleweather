package com.bubbleweather.android.db;

import org.litepal.crud.LitePalSupport;

public class SaveDate extends LitePalSupport {
    public String csName;
    public String csCode;

    public String getCsName() {
        return csName;
    }

    public void setCsName(String csName) {
        this.csName = csName;
    }

    public String getCsCode() {
        return csCode;
    }

    public void setCsCode(String csCode) {
        this.csCode = csCode;
    }
}
