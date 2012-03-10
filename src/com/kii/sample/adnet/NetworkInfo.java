package com.kii.sample.adnet;

/**
 * network into
 * @author hirokazu.fukami@kii.com
 *
 */
class NetworkInfo {
    private String name;
    private int impression;
    private int click;

    public void countImpression() {
        ++impression;
    }
    
    public void countClick() {
        ++click;
    }
	
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
    public int getImpression() {
        return impression;
    }
    public void setImpression(int impression) {
        this.impression = impression;
    }

    public int getClick() {
        return click;
    }
    public void setClick(int click) {
        this.click = click;
    }

}
