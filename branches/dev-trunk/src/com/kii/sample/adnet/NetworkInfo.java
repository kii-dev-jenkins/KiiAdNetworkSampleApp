//
//
//  Copyright 2012 Kii Corporation
//  http://kii.com
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//  
//

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
