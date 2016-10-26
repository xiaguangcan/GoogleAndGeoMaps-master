package com.gcxia.googlemap.domain;

import java.util.ArrayList;

/**
 * @author gcxia
 * @date 2016/8/31
 * @description
 */
public class NearbyResponse {
    public String status;
    public String next_page_token;
    public ArrayList<NearbyPoi> results;
}
