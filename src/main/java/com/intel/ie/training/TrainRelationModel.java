package com.intel.ie.training;

import java.io.IOException;

import static com.intel.ie.analytics.IntelKBPStatisticalExtractor.trainModel;

public class TrainRelationModel {
    public static void main(String[] args) {
        try {
            trainModel();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
